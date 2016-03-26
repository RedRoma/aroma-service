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
import tech.aroma.data.CredentialRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.service.operations.encryption.AromaPasswordEncryptor;
import tech.aroma.service.operations.encryption.OverTheWireDecryptor;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.Credentials;
import tech.aroma.thrift.authentication.Password;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.thrift.exceptions.AccountAlreadyExistsException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.service.SignUpRequest;
import tech.aroma.thrift.service.SignUpResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.PeopleGenerators.emails;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;
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
    private CredentialRepository credentialRepo;
    
    @Mock
    private MediaRepository mediaRepo;
    
    @Mock
    private UserRepository userRepo;
    
    @Mock
    private AuthenticationService.Iface authenticationService;
    
    @Mock
    private Function<AuthenticationToken, UserToken> tokenMapper;
    
    @Mock
    private OverTheWireDecryptor decryptor;
    
    @Mock
    private AromaPasswordEncryptor encryptor;
    
    @GenerateString(HEXADECIMAL)
    private String overTheWirePassword;
    
    @GenerateString(ALPHABETIC)
    private String password;
    
    @GenerateString(HEXADECIMAL)
    private String digestedPassword;

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
        instance = new SignUpOperation(authenticationService, credentialRepo, mediaRepo, userRepo, tokenMapper, decryptor, encryptor);
        
        verifyZeroInteractions(authenticationService, credentialRepo, userRepo, tokenMapper, decryptor, encryptor);
        
        setupData();
        setupMock();
    }
    
    @DontRepeat
    @Test
    public void testConstrutor() throws Exception
    {
        assertThrows(() -> new SignUpOperation(null, credentialRepo, mediaRepo, userRepo, tokenMapper, decryptor, encryptor));
        assertThrows(() -> new SignUpOperation(authenticationService, null, mediaRepo, userRepo, tokenMapper, decryptor, encryptor));
        assertThrows(() -> new SignUpOperation(authenticationService, credentialRepo, null, userRepo, tokenMapper, decryptor, encryptor));
        assertThrows(() -> new SignUpOperation(authenticationService, credentialRepo, mediaRepo, null, tokenMapper, decryptor, encryptor));
        assertThrows(() -> new SignUpOperation(authenticationService, credentialRepo, mediaRepo, userRepo, null, decryptor, encryptor));
        assertThrows(() -> new SignUpOperation(authenticationService, credentialRepo, mediaRepo, userRepo, tokenMapper, null, encryptor));
        assertThrows(() -> new SignUpOperation(authenticationService, credentialRepo, mediaRepo, userRepo, tokenMapper, decryptor, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        SignUpResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.userToken, is(userToken));
        
        verify(userRepo).saveUser(userCaptor.capture());
        
        User savedUser = userCaptor.getValue();
        assertThat(savedUser, notNullValue());
        assertThat(savedUser.name, is(request.name));
        assertThat(savedUser.firstName, is(request.firstName));
        assertThat(savedUser.middleName, is(request.middleName));
        assertThat(savedUser.lastName, is(request.lastName));
        assertThat(savedUser.roles, hasItem(request.mainRole));
        
        verify(credentialRepo).saveEncryptedPassword(savedUser.userId, digestedPassword);
        
        verify(authenticationService).createToken(requestCaptor.capture());
        
        CreateTokenRequest requestMade = requestCaptor.getValue();
        assertThat(requestMade, notNullValue());
        assertThat(requestMade.ownerId, is(savedUser.userId));
        assertThat(requestMade.desiredTokenType, is(TokenType.USER));
        
        verify(mediaRepo).saveMedia(savedUser.userId, request.profileImage);
    }
    
    @Test
    public void testWhenMediaRepoFails() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(mediaRepo)
            .saveMedia(anyString(), any(Image.class));
        
        SignUpResponse response = instance.process(request);

        verify(userRepo).saveUser(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        
        verify(credentialRepo).saveEncryptedPassword(savedUser.userId, digestedPassword);
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

        Credentials credentials = request.credentials;
        request.unsetCredentials();
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
        
        Credentials emptyCredentials = new Credentials();
        request.setCredentials(emptyCredentials);
        assertThrows(() -> instance.process(request))
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
    
    @Test
    public void testWhenFailToSaveCredentials() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(credentialRepo)
            .saveEncryptedPassword(anyString(), anyString());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
        
        verify(userRepo, never()).saveUser(any(User.class));
    }
    
    @Test
    public void testWhenOverTheWireDecryptionFails() throws Exception
    {
        when(decryptor.decrypt(overTheWirePassword))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
        
        verify(userRepo, never()).saveUser(any(User.class));
    }

    @Test
    public void testWhenPasswordEncryptionFails() throws Exception
    {
        when(encryptor.encryptPassword(password))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
        
        verify(userRepo, never()).saveUser(any(User.class));
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

        when(decryptor.decrypt(overTheWirePassword))
            .thenReturn(password);
        
        when(encryptor.encryptPassword(password))
            .thenReturn(digestedPassword);
    }

    private void setupData() throws TException
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
        
        Credentials credentials = new Credentials();
        credentials.setAromaPassword(new Password(overTheWirePassword));
        request.setCredentials(credentials);
        
        request.email = one(emails());
    }
}
