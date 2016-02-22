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

package tech.aroma.banana.service.operations;

import java.util.List;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.InboxRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetDashboardRequest;
import tech.aroma.thrift.service.GetDashboardResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class GetDashboardOperationTest 
{
    @Mock
    private InboxRepository inboxRepo;
    
    @GenerateList(Message.class)
    private List<Message> messages;
    
    @GenerateString(UUID)
    private String userId;
    
    @GeneratePojo
    private GetDashboardRequest request;
    
    private GetDashboardOperation instance;
    
    @Before
    public void setUp() throws Exception
    {
        instance = new GetDashboardOperation(inboxRepo);
        
        setupData();
        setupMocks();
    }

    @Test
    public void testProcess() throws Exception
    {
        GetDashboardResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        response.recentMessages.forEach(m -> assertThat(m, isIn(messages)));
    }
    
    @Test
    public void testProcessEdgeCases()
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    private void setupMocks() throws TException
    {
        when(inboxRepo.getMessagesForUser(userId))
            .thenReturn(messages);
    }

    private void setupData()
    {
        request.token.userId = userId;
    }

}
