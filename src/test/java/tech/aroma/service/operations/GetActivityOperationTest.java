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
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.service.GetActivityRequest;
import tech.aroma.thrift.service.GetActivityResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.EventGenerators.events;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class GetActivityOperationTest 
{

    @Mock
    private ActivityRepository activityRepo;
    
    @Mock
    private UserRepository userRepo;
    
    @GeneratePojo
    private GetActivityRequest request;
    
    private List<Event> events;
    
    @GenerateString(UUID)
    private String userId;
    
    @GeneratePojo
    private User user;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    private GetActivityOperation instance;
    
    @Before
    public void setUp() throws Exception
    {
        instance = new GetActivityOperation(activityRepo, userRepo);
        
        setupData();
        setupMocks();
    }
    
    private void setupData()
    {
        events = listOf(events());
        user.userId = userId;
        
        request.token.userId = userId;
    }
    
    private void setupMocks() throws Exception
    {
        User expected = new User().setUserId(userId);
        when(activityRepo.getAllEventsFor(expected))
            .thenReturn(events);
    }

    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new GetActivityOperation(null, userRepo));
        assertThrows(() -> new GetActivityOperation(activityRepo, null));
    }
    
    @Test
    public void testProcess() throws Exception
    {
        GetActivityResponse response = instance.process(request);
        
        assertThat(response, notNullValue());
        assertThat(response.events, is(events));
        
    }
    
    @Test
    public void testWhenActivityRepoFails() throws Exception
    {
        when(activityRepo.getAllEventsFor(Mockito.any(User.class)))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @Test
    public void testWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.process(new GetActivityRequest()))
            .isInstanceOf(InvalidArgumentException.class);
        
        GetActivityRequest requestMissingToken = new GetActivityRequest(request);
        requestMissingToken.unsetToken();
        
        assertThrows(() -> instance.process(requestMissingToken))
            .isInstanceOf(InvalidArgumentException.class);
        
        GetActivityRequest requestWithBadId = new GetActivityRequest(request);
        requestWithBadId.token.setUserId(badId);
        assertThrows(() -> instance.process(requestWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
        
    }
    
    @DontRepeat
    @Test
    public void testWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
