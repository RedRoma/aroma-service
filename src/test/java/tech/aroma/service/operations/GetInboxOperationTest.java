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
import tech.aroma.data.InboxRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetInboxRequest;
import tech.aroma.thrift.service.GetInboxResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class GetInboxOperationTest
{
    @Mock
    private InboxRepository inboxRepo;

    @Mock
    private UserRepository userRepo;

    @GeneratePojo
    private GetInboxRequest request;

    @GenerateList(Message.class)
    private List<Message> messages;

    @GenerateString(UUID)
    private String userId;

    private GetInboxOperation instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new GetInboxOperation(inboxRepo, userRepo);
        verifyZeroInteractions(inboxRepo, userRepo);

        setupData();
        setupMocks();
    }

    @Test
    public void testProcess() throws Exception
    {
        GetInboxResponse response = instance.process(request);
        assertThat(response, notNullValue());

        List<Message> sortedMessages = messages.stream()
                                               .sorted(Comparator.comparingLong(Message::getTimeMessageReceived)
                                                                 .reversed())
                                               .limit(request.limit)
                                               .collect(toList());

        assertThat(response.messages, is(sortedMessages));
    }

    @DontRepeat
    @Test
    public void testWhenNoMessages() throws Exception
    {
        when(inboxRepo.getMessagesForUser(userId))
                .thenReturn(Lists.emptyList());

        GetInboxResponse response = instance.process(request);
        assertThat(response.messages, is(empty()));
    }


    @DontRepeat
    @Test
    public void testWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.process(null))
                .isInstanceOf(InvalidArgumentException.class);
    }

    private void setupData()
    {
        request.token.userId = userId;
    }

    private void setupMocks() throws Exception
    {
        when(inboxRepo.getMessagesForUser(userId))
                .thenReturn(messages);
    }

}
