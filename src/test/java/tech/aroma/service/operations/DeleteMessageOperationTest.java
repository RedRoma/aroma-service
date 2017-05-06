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

import java.util.List;
import java.util.Set;
import org.apache.thrift.TException;
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
import tech.aroma.data.MessageRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.service.DeleteMessageRequest;
import tech.aroma.thrift.service.DeleteMessageResponse;
import tech.sirwellington.alchemy.annotations.testing.TimeSensitive;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateInteger;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.epochNowWithinDelta;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 *
 * @author SirWellington
 */
@TimeSensitive
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class DeleteMessageOperationTest
{

    @Mock
    private ActivityRepository activityRepo;

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private FollowerRepository followerRepo;

    @Mock
    private MessageRepository messageRepo;

    @Mock
    private UserRepository userRepo;
    
    
    @GeneratePojo
    private DeleteMessageRequest request;
    
    private DeleteMessageOperation instance;
    
    @GeneratePojo
    private Application app;
    
    @GeneratePojo
    private User user;
    
    @GenerateString(UUID)
    private String userId;
    
    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String msgId;
    
    @GenerateString(HEXADECIMAL)
    private String tokenId;
    
    private Set<String> messageIds;
    
    @GenerateList(User.class)
    private List<User> followers;
    
    @GenerateList(User.class)
    private List<User> owners;
    
    @Captor
    private ArgumentCaptor<Event> eventCaptor;
    
    @GenerateInteger
    private int totalMessageStored;
    
    @Before
    public void setUp() throws TException
    {
        instance = new DeleteMessageOperation(activityRepo, appRepo, followerRepo, messageRepo, userRepo);
        setupData();
        setupMocks();
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new DeleteMessageOperation(null, appRepo, followerRepo, messageRepo, userRepo))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new DeleteMessageOperation(activityRepo, null, followerRepo, messageRepo, userRepo))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new DeleteMessageOperation(activityRepo, appRepo, null, messageRepo, userRepo))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new DeleteMessageOperation(activityRepo, appRepo, followerRepo, null, userRepo))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new DeleteMessageOperation(activityRepo, appRepo, followerRepo, messageRepo, null))
            .isInstanceOf(IllegalArgumentException.class);
        
    }

    @Test
    public void testProcess() throws Exception
    {
        DeleteMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        for(String id : messageIds)
        {
            verify(messageRepo).deleteMessage(appId, id);
        }
        
        verifyZeroInteractions(activityRepo, followerRepo, userRepo);
    }
    
    @Test
    public void testProcessWithOneMessage() throws Exception
    {
        request.unsetMessageIds();
        DeleteMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(messageRepo).deleteMessage(appId, msgId);

        verifyZeroInteractions(activityRepo, followerRepo, userRepo);
    }
    
    @Test
    public void testProcessWithBatch() throws Exception
    {
        request.unsetMessageId();
        
        DeleteMessageResponse response = instance.process(request);
        
        for(String id : messageIds)
        {
            verify(messageRepo).deleteMessage(appId, id);
        }

        verifyZeroInteractions(activityRepo, followerRepo, userRepo);
    }
    
    @Test
    public void testProcessWhenNotAuthorized() throws Exception
    {
        app.owners.remove(userId);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UnauthorizedException.class);
    }
    
    @Test
    public void testProcessWhenDeleteAll() throws Exception
    {
        request.setDeleteAll(true);
        
        DeleteMessageResponse response = instance.process(request);
        
        assertThat(response.messagesDeleted, is(totalMessageStored));
        
        verify(messageRepo).deleteAllMessages(appId);
        
        verify(userRepo, atLeastOnce()).getUser(userId);
        verify(followerRepo).getApplicationFollowers(appId);
        
        List<User> interestedParties = Lists.combine(owners, followers)
            .stream()
            .distinct()
            .collect(toList());
        
        for (User interestedParty : interestedParties)
        {
            verify(activityRepo).saveEvent(eventCaptor.capture(), eq(interestedParty));

            Event event = eventCaptor.getValue();
            checkEvent(event);
        }
       
    }
    
    @Test
    public void testDeleteAllWhenAppHasNoFollowers() throws Exception
    {
        request.setDeleteAll(true);
        
        when(followerRepo.getApplicationFollowers(appId)).thenReturn(Lists.emptyList());
        
        DeleteMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        for (User owner: owners)
        {
            verify(activityRepo).saveEvent(eventCaptor.capture(), eq(owner));
            
            Event event = eventCaptor.getValue();
            checkEvent(event);
        }
    }
    
    @Test
    public void testDeleteAllWhenActivityRepoFails() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(activityRepo)
            .saveEvent(any(), any());
        
        request.setDeleteAll(true);
        
        DeleteMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.messagesDeleted, is(totalMessageStored));
    }

    private void setupData()
    {
        request.token.tokenId = tokenId;
        request.token.userId = userId;
        request.applicationId = appId;
        request.messageId = msgId;
        request.setDeleteAll(false);
        
        user.userId = userId;
        
        messageIds = listOf(uuids).stream().collect(toSet());
        messageIds.add(request.messageId);
        request.messageIds = Lists.copy(messageIds);
        
        followers.forEach(u -> u.userId = one(uuids));
        
        owners.forEach(u -> u.userId = one(uuids));
        app.owners = owners.stream().map(User::getUserId).collect(toSet());
        app.applicationId = appId;
        app.owners.add(userId);
    }

    private void setupMocks() throws TException
    {
        when(appRepo.getById(appId)).thenReturn(app);
        
        when(followerRepo.getApplicationFollowers(appId)).thenReturn(followers);
        
        when(userRepo.getUser(userId)).thenReturn(user);

        for(User owner : owners)
        {
            when(userRepo.getUser(owner.userId)).thenReturn(owner);
        }
        
        when(messageRepo.getCountByApplication(appId))
            .thenReturn((long) totalMessageStored);
    }

    private void checkEvent(Event event)
    {
        assertThat(event, notNullValue());
        checkThat(event.timestamp).is(epochNowWithinDelta(10_000));
        assertThat(event.applicationId, is(appId));
        assertThat(event.application, is(app));
        assertThat(event.actor, is(user));
        assertThat(event.userIdOfActor, is(userId));

    }

}
