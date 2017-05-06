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

import java.util.Comparator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.MessageRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.service.GetApplicationMessagesRequest;
import tech.aroma.thrift.service.GetApplicationMessagesResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class GetApplicationMessagesOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private FollowerRepository followerRepo;
    
    @Mock
    private MessageRepository messageRepo;

    private GetApplicationMessagesOperation instance;

    @GeneratePojo
    private GetApplicationMessagesRequest request;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String userId;
    
    @GeneratePojo
    private Application app;

    @GenerateList(Message.class)
    private List<Message> messages;

    private List<Message> sortedMessages;
    
    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new GetApplicationMessagesOperation(appRepo, followerRepo, messageRepo);
    }

    private void setupData() throws Exception
    {

        request.applicationId = appId;
        request.token.userId = userId;
        
        app.owners.add(userId);
        
        sortedMessages = messages.stream()
            .sorted(Comparator.comparingLong(Message::getTimeMessageReceived).reversed())
            .limit(request.limit)
            .collect(toList());
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getById(appId)).thenReturn(app);
        
        when(messageRepo.getByApplication(appId))
            .thenReturn(messages);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GetApplicationMessagesOperation(null, followerRepo, messageRepo));
        assertThrows(() -> new GetApplicationMessagesOperation(appRepo, null, messageRepo));
        assertThrows(() -> new GetApplicationMessagesOperation(appRepo, followerRepo, null));
    }
    
    @Test
    public void testProcess() throws Exception
    {
        GetApplicationMessagesResponse response = instance.process(request);
        assertThat(response, notNullValue());

        assertThat(response.messages, is(sortedMessages));
    }
    
    @DontRepeat
    @Test
    public void testWhenNoMessages() throws Exception
    {
        when(messageRepo.getByApplication(appId))
            .thenReturn(Lists.emptyList());
        
        GetApplicationMessagesResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.messages, is(empty()));
    }
    
    @DontRepeat
    @Test
    public void testWhenMessageRepoFails() throws Exception
    {
        when(messageRepo.getByApplication(appId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @Test
    public void testWhenUserIsAnOwnerButNotAFollower() throws Exception
    {
        when(followerRepo.followingExists(userId, appId))
            .thenReturn(false);
        
        GetApplicationMessagesResponse response = instance.process(request);
        assertThat(response.messages, is(sortedMessages));
    }
    
    @Test
    public void testWhenUserIsNotAnOwnerButIsAFollower() throws Exception
    {
        app.owners.remove(userId);
        
        when(followerRepo.followingExists(userId, appId)).thenReturn(true);
        
        GetApplicationMessagesResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.messages, not(empty()));
        assertThat(response.messages, is(sortedMessages));
    }
    
    @DontRepeat
    @Test
    public void testWhenAppRepoFails() throws Exception
    {
        when(appRepo.getById(appId))
            .thenThrow(new DoesNotExistException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(DoesNotExistException.class);
    }
    
    @Test
    public void testWhenFollowerRepoFails() throws Exception
    {
        app.owners.remove(userId);
        
        when(followerRepo.followingExists(userId, appId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @DontRepeat
    @Test
    public void testWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);

        GetApplicationMessagesRequest emptyRequest = new GetApplicationMessagesRequest();
        assertThrows(() -> instance.process(emptyRequest))
            .isInstanceOf(InvalidArgumentException.class);

        GetApplicationMessagesRequest requestWithoutAppId = new GetApplicationMessagesRequest(request);
        requestWithoutAppId.unsetApplicationId();
        assertThrows(() -> instance.process(requestWithoutAppId))
            .isInstanceOf(InvalidArgumentException.class);

        GetApplicationMessagesRequest requestWithBadLimit = new GetApplicationMessagesRequest(request)
            .setLimit(one(negativeIntegers()));
        assertThrows(() -> instance.process(requestWithBadLimit))
            .isInstanceOf(InvalidArgumentException.class);

        String badId = one(alphabeticString());
        GetApplicationMessagesRequest requestWithBadId = new GetApplicationMessagesRequest(request)
            .setApplicationId(badId);
        assertThrows(() -> instance.process(requestWithoutAppId))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
