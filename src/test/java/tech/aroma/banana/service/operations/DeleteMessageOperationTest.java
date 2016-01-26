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

import java.util.Collection;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.exceptions.UnauthorizedException;
import tech.aroma.banana.thrift.service.DeleteMessageRequest;
import tech.aroma.banana.thrift.service.DeleteMessageResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toSet;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class DeleteMessageOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private MessageRepository messageRepo;
    
    
    @GeneratePojo
    private DeleteMessageRequest request;
    
    private DeleteMessageOperation instance;
    
    @GeneratePojo
    private Application app;
    
    @GenerateString(UUID)
    private String userId;
    
    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String msgId;
    
    @GenerateString(HEXADECIMAL)
    private String tokenId;
    
    private Collection<String> messageIds;

    @Before
    public void setUp() throws TException
    {
        instance = new DeleteMessageOperation(appRepo, messageRepo);
        setupData();
        setupMocks();
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new DeleteMessageOperation(null, messageRepo))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new DeleteMessageOperation(appRepo, null))
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
    }
    
    @Test
    public void testProcessWithOneMessage() throws Exception
    {
        request.unsetMessageIds();
        DeleteMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(messageRepo).deleteMessage(appId, msgId);
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
    }
    
    @Test
    public void testProcessWhenNotAuthorized() throws Exception
    {
        app.owners.remove(userId);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UnauthorizedException.class);
    }

    private void setupData()
    {
        request.token.tokenId = tokenId;
        request.token.userId = userId;
        request.applicationId = appId;
        request.messageId = msgId;
        
        
        app.applicationId = appId;
        app.owners.add(userId);
        
        messageIds = listOf(uuids).stream().collect(toSet());
        messageIds.add(request.messageId);
        request.messageIds = Lists.copy(messageIds);
    }

    private void setupMocks() throws TException
    {
        when(appRepo.getById(appId)).thenReturn(app);
    }

}
