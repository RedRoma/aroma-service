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

import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.banana.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.banana.thrift.exceptions.AccountAlreadyExistsException;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.banana.thrift.service.SignUpRequest;
import tech.aroma.banana.thrift.service.SignUpResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class SignUpOperationTest
{

    @Mock
    private UserRepository userRepo;
    
    @Mock
    private AuthenticationService.Iface authenticationService;
    
    @Mock
    private Function<AuthenticationToken, UserToken> tokenMapper;

    @GeneratePojo
    private SignUpRequest request;
    
    @GenerateString(UUID)
    private String orgId;
    
    @GeneratePojo
    private AuthenticationToken authToken;
    
    @GeneratePojo
    private UserToken userToken;

    private SignUpOperation instance;
    
    @Captor
    private ArgumentCaptor<User> userCaptor;
    
    @Captor
    private ArgumentCaptor<CreateTokenRequest> requestCaptor;
    
    private CreateTokenResponse authResponse;
    
    private String fullName;
    
    @Before
    public void setUp() throws TException
    {
        instance = new SignUpOperation(userRepo, authenticationService, tokenMapper);
        
        verifyZeroInteractions(userRepo, authenticationService, tokenMapper);
        
        setupData();
        setupMock();
    }

    @Test
    public void testProcess() throws Exception
    {
        SignUpResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.userToken, is(userToken));
    }
    
    @Test
    public void testProcessWhenEmailExists() throws Exception
    {
        reset(userRepo);
        when(userRepo.getUserByEmail(request.email))
            .thenReturn(new User().setEmail(request.email));
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(AccountAlreadyExistsException.class);
    }

    @DontRepeat
    @Test
    public void testWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testWhenNamesAreMissing() throws Exception
    {
        String firstName = request.firstName;
        String middleName = request.middleName;
        String lastName = request.lastName;
        
        request.unsetFirstName();
        request.unsetMiddleName();
        request.unsetLastName();
        request.setName(fullName);
        
        
        SignUpResponse response = instance.process(request);
        
        assertThat(response, notNullValue());
        assertThat(response.userToken, is(userToken));
        
        verify(userRepo).saveUser(userCaptor.capture());
        
        User userSaved = userCaptor.getValue();
        assertThat(userSaved.name, is(fullName));
        assertThat(userSaved.firstName, is(firstName));
        assertThat(userSaved.middleName, is(middleName));
        assertThat(userSaved.lastName, is(lastName));
    }

    private void setupMock() throws TException
    {
        when(authenticationService.createToken(any(CreateTokenRequest.class)))
            .thenReturn(authResponse);
        
        when(tokenMapper.apply(authToken)).thenReturn(userToken);
        
        request.organizationId = orgId;
        authToken.organizationId = orgId;
        
        when(userRepo.getUserByEmail(request.email))
            .thenThrow(new UserDoesNotExistException());
    }

    private void setupData()
    {
        authResponse = new CreateTokenResponse(authToken);
        
        String first = one(alphabeticString(10));
        String middle = one(alphabeticString(10));
        String last = one(alphabeticString(10));
        String full = first + " " + middle + " " + last;
        
        request.name = full;
        request.firstName = first;
        request.middleName = middle;
        request.lastName = last;
        
        fullName = full;
    }
}
