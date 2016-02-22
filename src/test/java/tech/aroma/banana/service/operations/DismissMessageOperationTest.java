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

import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.banana.data.InboxRepository;
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.service.DismissMessageRequest;
import tech.aroma.banana.thrift.service.DismissMessageResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class DismissMessageOperationTest 
{
    @Mock
    private InboxRepository inboxRepo;
    
    @GeneratePojo
    private UserToken token;
    
    @GenerateString(UUID)
    private String userId;
    
    @GenerateString(UUID)
    private String messageId;
    
    @GeneratePojo
    private DismissMessageRequest request;

    private DismissMessageOperation instance;
    
    @Before
    public void setUp() throws Exception
    {
        instance = new DismissMessageOperation(inboxRepo);
        verifyZeroInteractions(inboxRepo);
        
        setupData();
        
        setupMocks();
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new DismissMessageOperation(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testProcess() throws Exception
    {
        DismissMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(inboxRepo).deleteMessageForUser(userId, messageId);
    }
    
    @Test
    public void testProcessWithMultipleMessages() throws Exception
    {
        int count = one(integers(10, 50));
        List<String> messageIds = listOf(uuids, count);
        request.setMessageIds(messageIds);
        
        Set<String> expected = Sets.copyOf(messageIds);
        
        instance.process(request);
        
        for(String id : expected)
        {
            verify(inboxRepo).deleteMessageForUser(userId, id);
        }
    }
    
    @Test
    public void testProcessWithDismissAll() throws Exception
    {
        request.setDismissAll(true);
        
        DismissMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(inboxRepo).deleteAllMessagesForUser(userId);
    }
    
    @Test
    public void testProcessWithBadRequests() throws Exception
    {
        DismissMessageRequest nullRequest = null;
        assertThrows(() -> instance.process(nullRequest)).isInstanceOf(InvalidArgumentException.class);
        
        DismissMessageRequest requestWithoutToken = new DismissMessageRequest(request);
        requestWithoutToken.unsetToken();
        assertThrows(() -> instance.process(requestWithoutToken)).isInstanceOf(InvalidArgumentException.class);
        
        UserToken tokenWithoutUserId = new UserToken(token);
        tokenWithoutUserId.unsetUserId();
        
        DismissMessageRequest requestWithoutUserId = new DismissMessageRequest(request);
        requestWithoutUserId.setToken(tokenWithoutUserId);
            
        assertThrows(() -> instance.process(requestWithoutUserId)).isInstanceOf(InvalidArgumentException.class);
        
        List<String> badIds = listOf(alphabeticString());
        DismissMessageRequest requestWithBadMessageIds = new DismissMessageRequest(request);
        requestWithBadMessageIds.setMessageIds(badIds);
        
        assertThrows(() -> instance.process(requestWithBadMessageIds)).isInstanceOf(InvalidArgumentException.class);


    }

    private void setupData()
    {
        token.userId = userId;
        
        request.setToken(token);
        request.setDismissAll(false);
        request.messageId = messageId;
        
        request.messageIds.clear();
        
    }

    private void setupMocks()
    {
    }

}