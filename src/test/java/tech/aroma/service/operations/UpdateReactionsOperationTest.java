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
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.ReactionRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.reactions.Reaction;
import tech.aroma.thrift.service.UpdateReactionsRequest;
import tech.aroma.thrift.service.UpdateReactionsResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
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
public class UpdateReactionsOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private ReactionRepository reactionsRepo;

    @Mock
    private UserRepository userRepo;

    @GeneratePojo
    private Application app;

    @GeneratePojo
    private UpdateReactionsRequest request;

    private List<Reaction> reactions;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String userId;

    @GenerateString(ALPHABETIC)
    private String badId;

    private UpdateReactionsOperation instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new UpdateReactionsOperation(appRepo, reactionsRepo, userRepo);
    }

    private void setupData() throws Exception
    {
        reactions = listOf(reactions(), 10);
        
        request.token.userId = userId;
        request.unsetForAppId();
        request.setReactions(reactions);
        
        app.applicationId = appId;
        app.owners.add(userId);
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getById(appId)).thenReturn(app);
        when(userRepo.containsUser(userId)).thenReturn(true);
        
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new UpdateReactionsOperation(null, reactionsRepo, userRepo));
        assertThrows(() -> new UpdateReactionsOperation(appRepo, null, userRepo));
        assertThrows(() -> new UpdateReactionsOperation(appRepo, reactionsRepo, null));
    }

    @Test
    public void testProcessForUser() throws Exception
    {
        UpdateReactionsResponse response = instance.process(request);
        assertThat(response.reactions, is(reactions));
        
        verify(reactionsRepo).saveReactionsForUser(userId, reactions);
        verify(reactionsRepo, never()).saveReactionsForApplication(anyString(), any());
        verifyZeroInteractions(appRepo);
    }

    @Test
    public void testProcessForApp() throws Exception
    {
        request.forAppId = appId;
        UpdateReactionsResponse response = instance.process(request);
        assertThat(response.reactions, is(reactions));
        
        verify(reactionsRepo).saveReactionsForApplication(appId, reactions);
        verify(reactionsRepo, never()).saveReactionsForUser(anyString(), any());
    }
    
    @Test
    public void testWhenUserDoesNotExist() throws Exception
    {
        when(userRepo.containsUser(userId)).thenReturn(false);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UserDoesNotExistException.class);
    }
    
    @Test
    public void testWhenUserRepoFails() throws Exception
    {
        when(userRepo.containsUser(userId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
        
        verifyZeroInteractions(reactionsRepo);
    }
    
    @Test
    public void testWhenAppDoesNotExist() throws Exception
    {
        when(appRepo.getById(appId))
            .thenThrow(new ApplicationDoesNotExistException());
        
        request.forAppId = appId;
        assertThrows(() -> instance.process(request))
            .isInstanceOf(ApplicationDoesNotExistException.class);

        verifyZeroInteractions(reactionsRepo);
    }
    
    @Test
    public void testWhenAppRepoFails() throws Exception
    {
        when(appRepo.getById(appId))
            .thenThrow(new OperationFailedException());
        
        request.forAppId = appId;
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @Test
    public void testWhenReactionsRepoFails() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(reactionsRepo)
            .saveReactionsForUser(userId, reactions);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @Test
    public void testProcessWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        //bad app id
        request.forAppId = badId;
        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
        
        //Missing token.
        request.unsetToken();
        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testProcessWhenUserIsNotAnOwner() throws Exception
    {
        app.owners.remove(userId);
        request.forAppId = appId;
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UnauthorizedException.class);
        
        verifyZeroInteractions(reactionsRepo);
    }

}
