/*
 * Copyright 2016 RedRoma.
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

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.MessageRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.service.DeleteApplicationRequest;
import tech.aroma.thrift.service.DeleteApplicationResponse;
import tech.sirwellington.alchemy.annotations.testing.TimeSensitive;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.epochNowWithinDelta;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class DeleteApplicationOperationTest
{

    @Mock
    private ActivityRepository activityRepo;
    
    @Mock
    private ApplicationRepository appRepo;
    
    @Mock
    private FollowerRepository followerRepo;
    
    @Mock
    private MediaRepository mediaRepo;
    
    @Mock
    private MessageRepository messageRepo;
    
    @Mock
    private UserRepository userRepo;

    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    @GenerateString(UUID)
    private String userId;
    
    @GeneratePojo
    private Application app;
    
    @GeneratePojo
    private DeleteApplicationRequest request;
    
    @GenerateList(User.class)
    private List<User> followers;
    
    private DeleteApplicationOperation instance;
    
    @Captor
    private ArgumentCaptor<Event> captor;
    
    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
        
        instance = new DeleteApplicationOperation(activityRepo,
                                                  appRepo,
                                                  followerRepo,
                                                  mediaRepo,
                                                  messageRepo,
                                                  userRepo);

        verifyZeroInteractions(activityRepo,
                               appRepo,
                               followerRepo,
                               mediaRepo,
                               messageRepo,
                               userRepo);
    }

    private void setupData() throws Exception
    {
        app.applicationId = appId;
        app.owners.add(userId);
        
        request.applicationId = appId;
        request.token.userId = userId;
        
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getById(appId)).thenReturn(app);
        
        when(followerRepo.getApplicationFollowers(appId)).thenReturn(followers);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new DeleteApplicationOperation(null, appRepo, followerRepo, mediaRepo, messageRepo, userRepo));
        assertThrows(() -> new DeleteApplicationOperation(activityRepo, null, followerRepo, mediaRepo, messageRepo, userRepo));
        assertThrows(() -> new DeleteApplicationOperation(activityRepo, appRepo, null, mediaRepo, messageRepo, userRepo));
        assertThrows(() -> new DeleteApplicationOperation(activityRepo, appRepo, followerRepo, null, messageRepo, userRepo));
        assertThrows(() -> new DeleteApplicationOperation(activityRepo, appRepo, followerRepo, mediaRepo, null, userRepo));
        assertThrows(() -> new DeleteApplicationOperation(activityRepo, appRepo, followerRepo, mediaRepo, messageRepo, null));
    }

    @TimeSensitive
    @Test
    public void testProcess() throws Exception
    {
        
        DeleteApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(appRepo).deleteApplication(appId);
        
        verify(mediaRepo).deleteMedia(app.applicationIconMediaId);
        verify(mediaRepo).deleteAllThumbnails(app.applicationIconMediaId);
        
        verify(mediaRepo).deleteMedia(appId);
        verify(mediaRepo).deleteAllThumbnails(appId);
        
        verify(messageRepo).deleteAllMessages(appId);
        
        for (User follower : followers)
        {
            String followerId = follower.userId;
            
            verify(followerRepo).deleteFollowing(followerId, appId);
        }
        
        
        for(User follower : followers)
        {
            verify(activityRepo).saveEvent(captor.capture(), eq(follower));
            
            Event event = captor.getValue();
            checkEvent(event);
        }            
        
        for (String ownerId : app.owners)
        {
            User owner = new User().setUserId(ownerId);
            
            verify(activityRepo).saveEvent(captor.capture(), eq(owner));
            Event event = captor.getValue();
            checkEvent(event);
        }
    }
    
    @Test
    public void testWhenNotAuthorized() throws Exception
    {
        app.owners.remove(userId);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UnauthorizedException.class);
        
        verify(appRepo, never()).deleteApplication(appId);
        verifyZeroInteractions(followerRepo, mediaRepo, messageRepo);
    }
    
    @Test
    public void testWhenNoFollowers() throws Exception
    {
        when(followerRepo.getApplicationFollowers(appId)).thenReturn(Lists.emptyList());
        
        instance.process(request);
        
        verify(followerRepo, never()).deleteFollowing(anyString(), anyString());
    }
    
    @DontRepeat
    @Test
    public void testWithBadArguments() throws Exception
    {
        //Missing Request
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        //Empty Request
        assertThrows(() -> instance.process(new DeleteApplicationRequest()))
            .isInstanceOf(InvalidArgumentException.class);
        
        //Request with bad ID
        DeleteApplicationRequest requestWithBadId = new DeleteApplicationRequest(request).setApplicationId(badId);
        assertThrows(() -> instance.process(requestWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
            
        //Request with bad User ID
        DeleteApplicationRequest requestWithBadUserId = new DeleteApplicationRequest(request);
        requestWithBadUserId.token.setUserId(badId);
        assertThrows(() -> instance.process(requestWithBadUserId))
            .isInstanceOf(InvalidArgumentException.class);
        
        //Request missing Token
        DeleteApplicationRequest requestMissingToken = new DeleteApplicationRequest(request);
        requestMissingToken.unsetToken();
        assertThrows(() -> instance.process(requestMissingToken))
            .isInstanceOf(InvalidArgumentException.class);
    }

    private void checkEvent(Event event)
    {
        checkThat(event).is(notNull());
        checkThat(event.eventId).is(validUUID());
        assertThat(event.applicationId, is(appId));
        assertThat(event.application, is(app));
        checkThat(event.timestamp).is(epochNowWithinDelta(5_000));
    }

}
