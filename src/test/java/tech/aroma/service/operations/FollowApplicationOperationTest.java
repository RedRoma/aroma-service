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

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import tech.aroma.data.*;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.service.FollowApplicationRequest;
import tech.aroma.thrift.service.FollowApplicationResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.equalTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class FollowApplicationOperationTest
{

    @Mock
    private ActivityRepository activityRepo;

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private FollowerRepository followRepo;

    @Mock
    private UserRepository userRepo;

    private FollowApplicationOperation instance;

    @GeneratePojo
    private FollowApplicationRequest request;

    @GeneratePojo
    private Application app;

    @GeneratePojo
    private User user;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String userId;

    @Captor
    private ArgumentCaptor<Event> captor;

    @Before
    public void setUp() throws Exception
    {
        instance = new FollowApplicationOperation(activityRepo, appRepo, followRepo, userRepo);
        verifyZeroInteractions(activityRepo, appRepo, followRepo, userRepo);

        setupData();
        setupMocks();
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new FollowApplicationOperation(null, appRepo, followRepo, userRepo));
        assertThrows(() -> new FollowApplicationOperation(activityRepo, null, followRepo, userRepo));
        assertThrows(() -> new FollowApplicationOperation(activityRepo, appRepo, null, userRepo));
        assertThrows(() -> new FollowApplicationOperation(activityRepo, appRepo, followRepo, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        FollowApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());

        verify(followRepo).saveFollowing(user, app);

        for (String ownerId : app.owners)
        {
            User owner = new User().setUserId(ownerId);

            verify(activityRepo).saveEvent(captor.capture(), eq(owner));

            Event event = captor.getValue();
            checkEvent(event);
        }
    }

    @Test
    public void testProcessWhenAppDoesNotExist() throws Exception
    {
        when(appRepo.getById(appId))
                .thenThrow(new ApplicationDoesNotExistException());

        assertThrows(() -> instance.process(request))
                .isInstanceOf(ApplicationDoesNotExistException.class);

        verifyZeroInteractions(followRepo);
    }

    @DontRepeat
    @Test
    public void testProcessWhenAppRepoFails() throws Exception
    {
        when(appRepo.getById(appId))
                .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.process(request))
                .isInstanceOf(OperationFailedException.class);

        verifyZeroInteractions(followRepo);
    }

    @Test
    public void testProcessWhenUserDoesNotExist() throws Exception
    {
        when(userRepo.getUser(userId))
                .thenThrow(new UserDoesNotExistException());

        assertThrows(() -> instance.process(request))
                .isInstanceOf(UserDoesNotExistException.class);

        verifyZeroInteractions(followRepo);
    }

    @DontRepeat
    @Test
    public void testProcessWhenUserRepoFails() throws Exception
    {
        when(userRepo.getUser(userId))
                .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.process(request))
                .isInstanceOf(OperationFailedException.class);

        verifyZeroInteractions(followRepo);
    }

    @Test
    public void testProcessWhenFollowRepoFails() throws Exception
    {
        doThrow(new OperationFailedException())
                .when(followRepo)
                .saveFollowing(user, app);

        assertThrows(() -> instance.process(request))
                .isInstanceOf(OperationFailedException.class);
    }

    private void setupMocks() throws TException
    {
        when(appRepo.getById(appId)).thenReturn(app);
        when(userRepo.getUser(userId)).thenReturn(user);
    }

    private void setupData()
    {
        request.token.userId = userId;
        request.applicationId = appId;

        app.applicationId = appId;

        user.userId = userId;
    }

    private void checkEvent(Event event)
    {
        assertThat(event, notNullValue());
        checkThat(event.eventId).is(validUUID());
        checkThat(event.actor).is(equalTo(user));
        checkThat(event.userIdOfActor).is(equalTo(userId));
        checkThat(event.application).is(equalTo(app));
        checkThat(event.applicationId).is(equalTo(appId));
    }

}
