/*
 * Copyright 2015 Aroma Tech.
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
import org.mockito.Mock;
import tech.aroma.data.MessageRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.MessageDoesNotExistException;
import tech.aroma.thrift.service.GetFullMessageRequest;
import tech.aroma.thrift.service.GetFullMessageResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class GetFullMessageOperationTest
{

    @Mock
    private MessageRepository messageRepo;

    @GeneratePojo
    private GetFullMessageRequest request;

    @GeneratePojo
    private Message message;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String messageId;
    
    @GenerateString(ALPHABETIC)
    private String badId;

    private GetFullMessageOperation instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new GetFullMessageOperation(messageRepo);
        verifyZeroInteractions(messageRepo);

        setupData();
        setupMocks();
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GetFullMessageOperation(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testProcess() throws Exception
    {
        GetFullMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.fullMessage, is(message));
    }
    
    @Test
    public void testWhenMessageNotExists() throws Exception
    {
        when(messageRepo.getMessage(appId, messageId))
            .thenThrow(new MessageDoesNotExistException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(MessageDoesNotExistException.class);
    }
    
    @DontRepeat
    @Test
    public void testWithEmptyRequest() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.process(new GetFullMessageRequest()))
            .isInstanceOf(InvalidArgumentException.class);
        
    }
    
    @Test
    public void testWithBadIds() throws Exception
    {
        request.messageId = badId;
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
        
        request.messageId = messageId;
        request.applicationId = badId;
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
    }

    private void setupData() throws Exception
    {
        request.messageId = messageId;
        request.applicationId = appId;
        
        message.messageId = messageId;
        message.applicationId = appId;
    }

    private void setupMocks() throws Exception
    {
        when(messageRepo.getMessage(appId, messageId))
            .thenReturn(message);
    }

}
