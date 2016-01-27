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
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.banana.thrift.service.GetUserInfoRequest;
import tech.aroma.banana.thrift.service.GetUserInfoResponse;
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
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class GetUserInfoOperationTest 
{
    @Mock
    private UserRepository userRepo;
    
    @GeneratePojo
    private GetUserInfoRequest request;
    
    @GeneratePojo
    private User user;
    
    @GenerateString(UUID)
    private String userId;
    
    private GetUserInfoOperation instance;
    
    @Before
    public void setUp() throws TException
    {
        instance = new GetUserInfoOperation(userRepo);
        verifyZeroInteractions(userRepo);
        
        setupData();
        setupMocks();
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new GetUserInfoOperation(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testProcess() throws Exception
    {
        GetUserInfoResponse result = instance.process(request);
        
        assertThat(result, notNullValue());
        assertThat(result.userInfo, is(user));
    }

    @Test
    public void testProcessWhenUserDoesNotExist() throws Exception
    {
        when(userRepo.getUser(userId))
            .thenThrow(new UserDoesNotExistException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UserDoesNotExistException.class);
    }
    
    private void setupData() throws TException
    {
        when(userRepo.getUser(userId)).thenReturn(user);
    }

    private void setupMocks()
    {
        request.userId = userId;
        user.userId = userId;
    }

}