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

import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.data.CredentialRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.service.operations.encryption.AromaPasswordEncryptor;
import tech.aroma.service.operations.encryption.OverTheWireDecryptor;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.Password;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.service.SignInRequest;
import tech.aroma.thrift.service.SignInResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.PeopleGenerators.emails;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class SignInOperationTest
{

    @Mock
    private AuthenticationService.Iface authenticationService;

    @Mock
    private Function<AuthenticationToken, UserToken> tokenMapper;

    @Mock
    private CredentialRepository credentialsRepo;

    @Mock
    private OverTheWireDecryptor decryptor;
    
    @Mock
    private AromaPasswordEncryptor encryptor;

    @Mock
    private UserRepository userRepo;

    @GeneratePojo
    private User user;

    @GenerateString(UUID)
    private String userId;

    @GeneratePojo
    private AuthenticationToken authToken;

    @GeneratePojo
    private UserToken userToken;

    @GenerateString(UUID)
    private String tokenId;

    @GenerateString(UUID)
    private String orgId;

    @Captor
    private ArgumentCaptor<CreateTokenRequest> captor;

    @GeneratePojo
    private SignInRequest request;
    
    @GeneratePojo
    private Password password;

    private SignInOperation instance;

    @Before
    public void setUp() throws TException
    {
        instance = new SignInOperation(authenticationService, tokenMapper, credentialsRepo, decryptor, encryptor, userRepo);
        verifyZeroInteractions(authenticationService, tokenMapper, credentialsRepo, decryptor, encryptor, userRepo);
        
        setupData();
        setupMocks();
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new SignInOperation(null, tokenMapper, credentialsRepo, decryptor, encryptor, userRepo))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new SignInOperation(authenticationService, null, credentialsRepo, decryptor, encryptor, userRepo))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new SignInOperation(authenticationService, tokenMapper, null, decryptor, encryptor, userRepo))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new SignInOperation(authenticationService, tokenMapper, credentialsRepo, null, encryptor, userRepo))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new SignInOperation(authenticationService, tokenMapper, credentialsRepo, decryptor, null, userRepo))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new SignInOperation(authenticationService, tokenMapper, credentialsRepo, decryptor, encryptor, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testProcess() throws Exception
    {
        SignInResponse response = instance.process(request);

        assertThat(response, notNullValue());
        assertThat(response.userToken, is(userToken));

        verify(authenticationService).createToken(captor.capture());

        CreateTokenRequest authRequest = captor.getValue();
        assertThat(authRequest, notNullValue());
        assertThat(authRequest.ownerId, is(userId));
        assertThat(authRequest.desiredTokenType, is(TokenType.USER));
        assertThat(authRequest.ownerName, is(user.name));
    }

    @Test
    public void testProcessWhenUserDoesNotExist() throws Exception
    {
        when(userRepo.getUserByEmail(request.emailAddress))
            .thenThrow(new UserDoesNotExistException());

        assertThrows(() -> instance.process(request))
            .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    public void testWhenCredentialsDontExist() throws Exception
    {
        when(credentialsRepo.containsEncryptedPassword(userId))
            .thenReturn(false);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UserDoesNotExistException.class);
    }
    
    @Test
    public void testWhenCredentialslDoNotMatch() throws Exception
    {
        when(encryptor.match(password.encryptedPassword, password.encryptedPassword))
            .thenReturn(false);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidCredentialsException.class);
    }
    
    @Test
    public void testWhenDecryptorFails() throws Exception
    {
        when(decryptor.decrypt(password.encryptedPassword))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @Test
    public void testWhenEncryptorFails() throws Exception
    {
        when(encryptor.match(anyString(), anyString()))
            .thenThrow(new OperationFailedException());
        
         assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @DontRepeat
    @Test
    public void testProcessWithBadArgs()
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    private void setupData()
    {
        authToken.tokenId = tokenId;
        authToken.ownerId = userId;
        authToken.organizationId = orgId;

        userToken.tokenId = tokenId;
        userToken.userId = userId;
        userToken.organization = orgId;

        user.userId = userId;
        
        request.credentials.setAromaPassword(password);
        request.emailAddress = one(emails());
    }

    private void setupMocks() throws TException
    {
        when(tokenMapper.apply(authToken))
            .thenReturn(userToken);

        when(userRepo.getUserByEmail(request.emailAddress))
            .thenReturn(user);

        when(authenticationService.createToken(Mockito.any(CreateTokenRequest.class)))
            .thenReturn(new CreateTokenResponse(authToken));
        
        when(decryptor.decrypt(password.encryptedPassword))
            .thenReturn(password.encryptedPassword);
        
        when(encryptor.match(password.encryptedPassword, password.encryptedPassword))
            .thenReturn(true);
        
        when(credentialsRepo.getEncryptedPassword(userId))
            .thenReturn(password.encryptedPassword);
        
        when(credentialsRepo.containsEncryptedPassword(userId))
            .thenReturn(true);
    }

}
