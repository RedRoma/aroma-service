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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.*;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.reactions.Reaction;
import tech.aroma.thrift.service.GetReactionsRequest;
import tech.aroma.thrift.service.GetReactionsResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.ApplicationGenerators.applications;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class GetReactionsOperationTest 
{
    
    @Mock
    private ApplicationRepository appRepo;
    
    @Mock
    private ReactionRepository reactionsRepo;
    
    @Mock
    private UserRepository userRepo;
    
    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String userId;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    private List<Reaction> reactions;
    
    private GetReactionsOperation instance;
    
    @GeneratePojo
    private GetReactionsRequest request;
    
    private Application app;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new GetReactionsOperation(appRepo, reactionsRepo, userRepo);
        verifyZeroInteractions(appRepo, reactionsRepo, userRepo);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new GetReactionsOperation(null, reactionsRepo, userRepo));
        assertThrows(() -> new GetReactionsOperation(appRepo, null, userRepo));
        assertThrows(() -> new GetReactionsOperation(appRepo, reactionsRepo, null));
    }


    private void setupData() throws Exception
    {
        app = one(applications());
        app.owners.add(userId);
        
        reactions = listOf(reactions(), 10);
        
        request.token.userId = userId;
        request.unsetForAppId();
        
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getById(appId)).thenReturn(app);
        
        when(userRepo.containsUser(userId)).thenReturn(true);
        
        when(reactionsRepo.getReactionsForUser(userId))
            .thenReturn(reactions);
        
        when(reactionsRepo.getReactionsForApplication(appId))
            .thenReturn(reactions);
    }

    @Test
    public void testProcessForUser() throws Exception
    {
        GetReactionsResponse response = instance.process(request);
        
        assertThat(response.reactions, is(reactions));
        
        verify(reactionsRepo).getReactionsForUser(userId);
        verify(reactionsRepo, never()).getReactionsForApplication(anyString());
    }
    
    @Test
    public void testProcessForApp() throws Exception
    {
        request.forAppId = appId;
        
        GetReactionsResponse response = instance.process(request);
        assertThat(response.reactions, is(reactions));
        
        verify(reactionsRepo).getReactionsForApplication(appId);
        verify(reactionsRepo, never()).getReactionsForUser(anyString());
    }
    
    @Test
    public void testProcessForAppWhenNotAnOwner() throws Exception
    {
        app.owners.remove(userId);
        request.forAppId = appId;
        
        GetReactionsResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.reactions, is(empty()));
        
        verifyZeroInteractions(reactionsRepo);
    }
    
    @Test
    public void testProcessWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);

        //Includes bad app ID
        request.forAppId = badId;
        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
        
        //Request missing token
        request.forAppId = appId;
        request.unsetToken();
        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testProcessWhenUserDoesNotExist() throws Exception
    {
        when(userRepo.containsUser(userId)).thenReturn(false);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UserDoesNotExistException.class);
    }
    
    @Test
    public void testProcessWhenUserRepoFails() throws Exception
    {
        when(userRepo.containsUser(userId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
        
        verifyZeroInteractions(reactionsRepo);
    }
    
    @Test
    public void testProcessWhenReactionsRepoFails() throws Exception
    {
        when(reactionsRepo.getReactionsForUser(userId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testProcess() throws Exception
    {
    }

}