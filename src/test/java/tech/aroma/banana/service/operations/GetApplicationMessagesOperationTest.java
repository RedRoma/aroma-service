/*
 * Copyright 2016 Aroma Tech.
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

package tech.aroma.banana.service.operations;

import java.util.Comparator;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.service.GetApplicationMessagesRequest;
import tech.aroma.banana.thrift.service.GetApplicationMessagesResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
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
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class GetApplicationMessagesOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private MessageRepository messageRepo;

    @Mock
    private UserRepository userRepo;

    private GetApplicationMessagesOperation instance;

    @GeneratePojo
    private GetApplicationMessagesRequest request;

    @GenerateString(UUID)
    private String appId;

    @GeneratePojo
    private Application app;

    @GenerateList(Message.class)
    private List<Message> messages;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new GetApplicationMessagesOperation(appRepo, messageRepo, userRepo);
    }

    private void setupData() throws Exception
    {

        request.applicationId = appId;
    }

    private void setupMocks() throws Exception
    {
        when(messageRepo.getByApplication(appId))
            .thenReturn(messages);
    }

    @Test
    public void testProcess() throws Exception
    {
        GetApplicationMessagesResponse response = instance.process(request);
        assertThat(response, notNullValue());

        List<Message> sortedMessages = messages.stream()
            .sorted(Comparator.comparingLong(Message::getTimeMessageReceived).reversed())
            .limit(request.limit)
            .collect(toList());

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
