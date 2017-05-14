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

import java.time.Instant;
import java.util.function.Function;

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.TokenRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.service.*;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.aroma.thrift.functions.TokenFunctions;
import tech.aroma.thrift.service.*;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.ApplicationGenerators.applications;
import static tech.aroma.thrift.generators.TokenGenerators.authenticationTokens;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.lessThanOrEqualTo;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class RenewApplicationTokenOperationTest
{

    @Mock
    private AuthenticationService.Iface authenticationService;

    @Mock
    private ApplicationRepository appRepo;
    
    @Mock
    private TokenRepository tokenRepo;

    @Mock
    private Function<AuthenticationToken, ApplicationToken> tokenMapper;

    @GeneratePojo
    private RenewApplicationTokenRequest request;

    private Application app;
    private ApplicationToken appToken;
    private AuthenticationToken authToken;

    @GenerateString(UUID)
    private String userId;

    @GenerateString(UUID)
    private String appId;

    private String tokenId;
    
    @Captor
    private ArgumentCaptor<AuthenticationToken> captor;

    private RenewApplicationTokenOperation instance;

    @Before
    public void setUp() throws TException
    {
        instance = new RenewApplicationTokenOperation(authenticationService, appRepo, tokenRepo, tokenMapper);
        verifyZeroInteractions(authenticationService, appRepo, tokenMapper);

        setupData();
        setupMocks();
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new RenewApplicationTokenOperation(null, appRepo, tokenRepo, tokenMapper));
        assertThrows(() -> new RenewApplicationTokenOperation(authenticationService, null, tokenRepo, tokenMapper));
        assertThrows(() -> new RenewApplicationTokenOperation(authenticationService, appRepo, null, tokenMapper));
        assertThrows(() -> new RenewApplicationTokenOperation(authenticationService, appRepo, tokenRepo, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        long expectedExpirationTime = Instant.now()
            .plusSeconds(TimeFunctions.toSeconds(AromaServiceConstants.DEFAULT_APP_TOKEN_LIFETIME))
            .toEpochMilli();
        
        long delta = 100;
        
        RenewApplicationTokenResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(tokenRepo).saveToken(captor.capture());
        
        AuthenticationToken savedToken = captor.getValue();
        assertThat(savedToken, is(authToken));
        
        checkThat(savedToken.timeOfExpiration)
            .is(greaterThanOrEqualTo(expectedExpirationTime - delta))
            .is(lessThanOrEqualTo(expectedExpirationTime + delta));
        
        assertThat(response.applicationToken, is(appToken));
    }

    @Test
    public void testProcessWhenNotAuthorized() throws Exception
    {
        app.owners.remove(userId);

        assertThrows(() -> instance.process(request))
            .isInstanceOf(UnauthorizedException.class);
    }

    @DontRepeat
    @Test
    public void testWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);

        RenewApplicationTokenRequest emptyRequest = new RenewApplicationTokenRequest();
        assertThrows(() -> instance.process(emptyRequest))
            .isInstanceOf(InvalidArgumentException.class);

        RenewApplicationTokenRequest requestWithoutToken = new RenewApplicationTokenRequest(request);
        requestWithoutToken.unsetToken();
        assertThrows(() -> instance.process(requestWithoutToken))
            .isInstanceOf(InvalidArgumentException.class);

        RenewApplicationTokenRequest requestWithoutAppId = new RenewApplicationTokenRequest(request);
        requestWithoutAppId.unsetApplicationId();
        assertThrows(() -> instance.process(requestWithoutAppId))
            .isInstanceOf(InvalidArgumentException.class);

    }

    private void setupMocks() throws TException
    {
        when(tokenMapper.apply(authToken)).thenReturn(appToken);
        when(appRepo.getById(appId)).thenReturn(app);

        InvalidateTokenRequest invalidateRequest = new InvalidateTokenRequest()
            .setBelongingTo(appId);
        
        when(authenticationService.invalidateToken(invalidateRequest))
            .thenReturn(new InvalidateTokenResponse());

        when(authenticationService.createToken(Mockito.any()))
            .thenReturn(new CreateTokenResponse(authToken));
        
        when(tokenRepo.getTokensBelongingTo(appId))
            .thenReturn(Lists.createFrom(authToken));
    }

    private void setupData()
    {
        request.token.userId = userId;

        app = one(applications());
        appId = app.applicationId;
        request.applicationId = appId;

        authToken = one(authenticationTokens());
        appToken = TokenFunctions.authTokenToAppTokenFunction().apply(authToken);
        tokenId = appToken.tokenId;

        app.owners.add(userId);
        app.timeOfTokenExpiration = appToken.timeOfExpiration;
    }

}
