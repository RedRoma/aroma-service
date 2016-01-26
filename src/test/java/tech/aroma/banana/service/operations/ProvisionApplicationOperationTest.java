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
import org.mockito.Mockito;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.authentication.ApplicationToken;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.TokenType;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.banana.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.banana.thrift.service.ProvisionApplicationRequest;
import tech.aroma.banana.thrift.service.ProvisionApplicationResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class ProvisionApplicationOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private AuthenticationService.Iface authenticationService;

    @Mock
    private Function<AuthenticationToken, ApplicationToken> appTokenMapper;

    @GeneratePojo
    private AuthenticationToken authToken;
    
    @GeneratePojo
    private ApplicationToken appToken;
    
    @Captor
    private ArgumentCaptor<Application> captor;
    
    @Captor
    private ArgumentCaptor<CreateTokenRequest> authRequestCaptor;

    private ProvisionApplicationOperation instance;

    @GeneratePojo
    private ProvisionApplicationRequest request;
    
    @GeneratePojo
    private User user;
    
    @GenerateString
    private String userId;
    
    @Before
    public void setUp() throws TException
    {
        instance = new ProvisionApplicationOperation(appRepo, userRepo, authenticationService, appTokenMapper);
        
        verifyZeroInteractions(appRepo, userRepo, authenticationService, appTokenMapper);
        
        setupData();
        setupMocks();
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new ProvisionApplicationOperation(null, userRepo, authenticationService, appTokenMapper))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new ProvisionApplicationOperation(appRepo, null, authenticationService, appTokenMapper))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new ProvisionApplicationOperation(appRepo, userRepo, null, appTokenMapper))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new ProvisionApplicationOperation(appRepo, userRepo, authenticationService, null))
            .isInstanceOf(IllegalArgumentException.class);
        
    }

    @Test
    public void testProcess() throws Exception
    {
        ProvisionApplicationResponse response = instance.process(request);
        
        verify(appRepo).saveApplication(captor.capture());
        
        Application savedApp = captor.getValue();
        assertThat(savedApp, notNullValue());
        assertThat(savedApp.name, is(request.applicationName));
        assertThat(savedApp.tier, is(request.tier));
        assertThat(savedApp.owners.contains(userId), is(true));
        assertThat(savedApp.owners.containsAll(request.owners), is(true));
        assertThat(savedApp.organizationId, is(request.organizationId));
        assertThat(savedApp.applicationDescription, is(request.applicationDescription));
        assertThat(savedApp.timeOfTokenExpiration, is(appToken.timeOfExpiration));
        
        assertThat(response, notNullValue());
        assertThat(response.applicationToken, is(appToken));
        assertThat(response.applicationInfo, is(savedApp));
        
        verify(authenticationService).createToken(authRequestCaptor.capture());
        
        CreateTokenRequest authRequestMade = authRequestCaptor.getValue();
        assertThat(authRequestMade, notNullValue());
        assertThat(authRequestMade.organizationId, is(request.organizationId));
        assertThat(authRequestMade.ownerId, is(savedApp.applicationId));
        assertThat(authRequestMade.desiredTokenType, is(TokenType.APPLICATION));
        assertThat(authRequestMade.ownerName, is(savedApp.name));
    }
    
    @Test
    public void testWhenUserDoesNotExist() throws Exception
    {
        when(userRepo.getUser(userId))
            .thenThrow(UserDoesNotExistException.class);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UserDoesNotExistException.class);
    }
    
    @Test
    public void testWhenAppRepoFails() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(appRepo)
            .saveApplication(Mockito.any());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @DontRepeat
    @Test
    public void testProcessEdgeCases()
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        ProvisionApplicationRequest emptyRequest = new ProvisionApplicationRequest();
        assertThrows(() -> instance.process(emptyRequest))
            .isInstanceOf(InvalidArgumentException.class);
    }

    private void setupData()
    {
        request.token.unsetUserId();
        authToken.ownerId = userId;
        user.userId = userId;
    }

    private void setupMocks() throws TException
    {
        GetTokenInfoRequest expectedAuthRequest = new GetTokenInfoRequest(request.token.tokenId, TokenType.USER);
        
        when(authenticationService.getTokenInfo(expectedAuthRequest))
            .thenReturn(new GetTokenInfoResponse(authToken));
        
        when(userRepo.getUser(userId)).thenReturn(user);
        
        when(appTokenMapper.apply(authToken))
            .thenReturn(appToken);
            
        
        when(authenticationService.createToken(Mockito.any()))
            .thenReturn(new CreateTokenResponse(authToken));
    }

}
