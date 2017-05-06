/*
 * Copyright 2017 RedRoma, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.aroma.service.operations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.service.UnfollowApplicationRequest;
import tech.aroma.thrift.service.UnfollowApplicationResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.equalTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.epochNowWithinDelta;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class UnfollowApplicationOperationTest
{
    @Mock
    private ActivityRepository activityRepo;
    
    @Mock
    private ApplicationRepository appRepo;
    
    @Mock
    private FollowerRepository followerRepo;
    
    @Mock
    private UserRepository userRepo;

    @GeneratePojo
    private UnfollowApplicationRequest request;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String userId;
    
    @GeneratePojo
    private User user;

    @GenerateString(ALPHABETIC)
    private String badId;

    @GeneratePojo
    private Application app;
    
    private UnfollowApplicationOperation instance;
    
    @Captor
    private ArgumentCaptor<Event> captor;

    @Before
    public void setUp() throws Exception
    {
        instance = new UnfollowApplicationOperation(activityRepo, appRepo, followerRepo, userRepo);

        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {
        request.token.setUserId(userId);
        request.setApplicationId(appId);

        app.applicationId = appId;
        
        user.userId = userId;
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getById(appId)).thenReturn(app);
        when(userRepo.getUser(userId)).thenReturn(user);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new UnfollowApplicationOperation(null, appRepo, followerRepo, userRepo));
        assertThrows(() -> new UnfollowApplicationOperation(activityRepo, null, followerRepo, userRepo));
        assertThrows(() -> new UnfollowApplicationOperation(activityRepo, appRepo, null, userRepo));
        assertThrows(() -> new UnfollowApplicationOperation(activityRepo, appRepo, followerRepo, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        UnfollowApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(followerRepo).deleteFollowing(userId, appId);
        
        for (String ownerId : app.owners)
        {
            User owner = new User().setUserId(ownerId);
            
            verify(activityRepo).saveEvent(captor.capture(), eq(owner));
            
            Event event = captor.getValue();
            checkEvent(event);
        }
    }

    @Test
    public void testWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);

        UnfollowApplicationRequest emptyRequest = new UnfollowApplicationRequest();
        assertThrows(() -> instance.process(emptyRequest))
            .isInstanceOf(InvalidArgumentException.class);

        UnfollowApplicationRequest requestMissingToken = new UnfollowApplicationRequest(request);
        requestMissingToken.unsetToken();
        assertThrows(() -> instance.process(requestMissingToken))
            .isInstanceOf(InvalidArgumentException.class);

        UnfollowApplicationRequest requestWithBadAppId = new UnfollowApplicationRequest(request)
            .setApplicationId(badId);
        assertThrows(() -> instance.process(requestWithBadAppId))
            .isInstanceOf(InvalidArgumentException.class);
        
        UserToken badToken = new UserToken(request.token).setUserId(badId);
        UnfollowApplicationRequest requestWithBadToken = new UnfollowApplicationRequest(request)
            .setToken(badToken);
        assertThrows(() -> instance.process(requestWithBadToken))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testWhenUserDoesNotExist() throws Exception
    {
        when(userRepo.getUser(userId))
            .thenThrow(UserDoesNotExistException.class);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UserDoesNotExistException.class);
        verifyZeroInteractions(followerRepo, activityRepo);
        
    }
    
    @Test
    public void testWhenApplicationDoesNotExist() throws Exception
    {
        when(appRepo.getById(appId))
            .thenThrow(new ApplicationDoesNotExistException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(ApplicationDoesNotExistException.class);
            
        verifyZeroInteractions(followerRepo, activityRepo);
    }

    private void checkEvent(Event event)
    {
        assertThat(event, notNullValue());
        checkThat(event.eventId).is(validUUID());
        checkThat(event.actor).is(equalTo(user));
        checkThat(event.userIdOfActor).is(equalTo(userId));
        checkThat(event.application).is(equalTo(app));
        checkThat(event.applicationId).is(equalTo(appId));
        checkThat(event.timestamp).is(epochNowWithinDelta(5_000));
    }

}
