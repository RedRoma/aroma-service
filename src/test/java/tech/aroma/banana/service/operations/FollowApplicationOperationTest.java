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

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.FollowerRepository;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.banana.thrift.service.FollowApplicationRequest;
import tech.aroma.banana.thrift.service.FollowApplicationResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class FollowApplicationOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private FollowerRepository followRepo;

    @Mock
    private UserRepository userRepo;

    private FollowApplicationOperation instance;
    
    @GeneratePojo
    private FollowApplicationRequest request;
    
    @GeneratePojo
    private Application app;
    
    @GeneratePojo
    private User user;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String userId;
    
    @Before
    public void setUp() throws Exception
    {
        instance = new FollowApplicationOperation(appRepo, followRepo, userRepo);
        verifyZeroInteractions(appRepo, followRepo, userRepo);
        
        setupData();
        setupMocks();
    }

    @Test
    public void testProcess() throws Exception
    {
        FollowApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(followRepo).saveFollowing(user, app);
    }
    
    @Test
    public void testProcessWhenAppDoesNotExist() throws Exception
    {
        when(appRepo.getById(appId))
            .thenThrow(new ApplicationDoesNotExistException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(ApplicationDoesNotExistException.class);
        
        verifyZeroInteractions(followRepo);
    }
    
    @DontRepeat
    @Test
    public void testProcessWhenAppRepoFails() throws Exception
    {
        when(appRepo.getById(appId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
        
        verifyZeroInteractions(followRepo);
    }
    
    @Test
    public void testProcessWhenUserDoesNotExist() throws Exception
    {
        when(userRepo.getUser(userId))
            .thenThrow(new UserDoesNotExistException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UserDoesNotExistException.class);
        
        verifyZeroInteractions(followRepo);
    }
    
    @DontRepeat
    @Test
    public void testProcessWhenUserRepoFails() throws Exception
    {
        when(userRepo.getUser(userId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
        
        verifyZeroInteractions(followRepo);
    }

    @Test
    public void testProcessWhenFollowRepoFails() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(followRepo)
            .saveFollowing(user, app);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }

    private void setupMocks() throws TException
    {
        when(appRepo.getById(appId)).thenReturn(app);
        when(userRepo.getUser(userId)).thenReturn(user);
    }

    private void setupData()
    {
        request.token.userId = userId;
        request.applicationId = appId;
        
        app.applicationId = appId;
        
        user.userId = userId;
    }

}
