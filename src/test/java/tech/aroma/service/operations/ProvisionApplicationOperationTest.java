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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.thrift.email.EmailNewApplication;
import tech.aroma.thrift.email.service.EmailService;
import tech.aroma.thrift.email.service.SendEmailRequest;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.service.AromaServiceConstants;
import tech.aroma.thrift.service.ProvisionApplicationRequest;
import tech.aroma.thrift.service.ProvisionApplicationResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class ProvisionApplicationOperationTest
{

    @Mock
    private ApplicationRepository appRepo;
    
    @Mock
    private FollowerRepository followerRepo;
    
    @Mock
    private MediaRepository mediaRepo;
    
    @Mock
    private UserRepository userRepo;

    @Mock
    private AuthenticationService.Iface authenticationService;
    
    @Mock
    private EmailService.Iface emailService;

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
    
    @Captor
    private ArgumentCaptor<SendEmailRequest> emailCaptor;
    
    @Before
    public void setUp() throws TException
    {
        instance = new ProvisionApplicationOperation(appRepo,
                                                     followerRepo,
                                                     mediaRepo,
                                                     userRepo,
                                                     authenticationService,
                                                     emailService,
                                                     appTokenMapper);

        verifyZeroInteractions(appRepo,
                               followerRepo,
                               mediaRepo,
                               userRepo,
                               authenticationService,
                               emailService,
                               appTokenMapper);

        setupData();
        setupMocks();
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new ProvisionApplicationOperation(null, followerRepo, mediaRepo, userRepo, authenticationService, emailService, appTokenMapper));
        assertThrows(() -> new ProvisionApplicationOperation(appRepo, null, mediaRepo, userRepo, authenticationService, emailService, appTokenMapper));
        assertThrows(() -> new ProvisionApplicationOperation(appRepo, followerRepo, null, userRepo, authenticationService, emailService, appTokenMapper));
        assertThrows(() -> new ProvisionApplicationOperation(appRepo, followerRepo, mediaRepo, null, authenticationService, emailService, appTokenMapper));
        assertThrows(() -> new ProvisionApplicationOperation(appRepo, followerRepo, mediaRepo, userRepo, null, emailService, appTokenMapper));
        assertThrows(() -> new ProvisionApplicationOperation(appRepo, followerRepo, mediaRepo, userRepo, authenticationService, null, appTokenMapper));
        assertThrows(() -> new ProvisionApplicationOperation(appRepo, followerRepo, mediaRepo, userRepo, authenticationService, emailService, null));
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
        
        verify(mediaRepo).saveMedia(savedApp.applicationIconMediaId, request.icon);
        
        verify(followerRepo).saveFollowing(user, savedApp);
        
        verify(emailService).sendEmail(emailCaptor.capture());
        SendEmailRequest emailRequest = emailCaptor.getValue();
        verifyEmailRequest(emailRequest, savedApp);
    }
    
    @Test
    public void testWithoutAppIcon() throws Exception
    {
        request.unsetIcon();
        
        ProvisionApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
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
        assertThat(savedApp.isSetApplicationIconMediaId(), is(false));
        
        verifyZeroInteractions(mediaRepo);
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
    
    @Test
    public void testWhenMultipleOwners() throws Exception
    {
        List<User> additionalOwners = listOf(pojos(User.class), 5)
            .stream()
            .map(u -> u.setUserId(one(uuids)))
            .collect(toList());
        
        Map<String, User> ownerMap = additionalOwners.stream()
            .collect(toMap(User::getUserId, u -> u));
        
        for (Map.Entry<String, User> owner : ownerMap.entrySet())
        {
            when(userRepo.getUser(owner.getKey())).thenReturn(owner.getValue());
        }
        
        request.setOwners(Sets.copyOf(ownerMap.keySet()));
        
        ProvisionApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(appRepo).saveApplication(captor.capture());
        
        Application savedApp = captor.getValue();
        Set<String> expectedOwners = Sets.copyOf(ownerMap.keySet());
        expectedOwners.add(userId);
        
        assertThat(savedApp.owners, is(expectedOwners));
        
        for (User owner : additionalOwners)
        {
            verify(followerRepo).saveFollowing(owner, savedApp);
        }
    }

    private void setupData()
    {
        request.token.unsetUserId();
        request.applicationName = one(alphabeticString(AromaServiceConstants.APPLICATION_NAME_MAX_LENGTH - 1));
        request.owners.clear();
        
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

    private void verifyEmailRequest(SendEmailRequest emailRequest, Application savedApp)
    {
        assertThat(emailRequest, notNullValue());
        assertThat(emailRequest.emailAddress, is(user.email));
        assertThat(emailRequest.emailMessage.isSetNewApp(), is(true));
        
        EmailNewApplication newApp = emailRequest.emailMessage.getNewApp();
        assertThat(newApp.app, is(savedApp));
        assertThat(newApp.appToken, is(appToken));
        assertThat(newApp.creator, is(user));
    }

}
