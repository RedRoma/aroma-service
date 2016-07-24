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

import java.time.Instant;
import java.util.function.Function;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.thrift.authentication.service.InvalidateTokenRequest;
import tech.aroma.thrift.authentication.service.InvalidateTokenResponse;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.service.RecreateApplicationTokenRequest;
import tech.aroma.thrift.service.RecreateApplicationTokenResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.ApplicationGenerators.applications;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.TimeGenerators.futureInstants;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class RecreateApplicationTokenOperationTest
{
    @Mock
    private AuthenticationService.Iface authenticationService;

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private Function<AuthenticationToken, ApplicationToken> tokenMapper;

    @GeneratePojo
    private RecreateApplicationTokenRequest request;

    private Application app;

    @GeneratePojo
    private ApplicationToken appToken;

    @GeneratePojo
    private AuthenticationToken authToken;

    @GenerateString(UUID)
    private String userId;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(HEXADECIMAL)
    private String tokenId;

    private RecreateApplicationTokenOperation instance;

    @Before
    public void setUp() throws TException
    {
        instance = new RecreateApplicationTokenOperation(authenticationService, appRepo, tokenMapper);
        verifyZeroInteractions(authenticationService, appRepo, tokenMapper);

        setupData();
        setupMocks();
    }

    @Test
    public void testProcess() throws Exception
    {
        long originalExpiration = app.timeOfTokenExpiration;
        
        RecreateApplicationTokenResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.applicationToken, is(appToken));
        assertThat(app.timeOfTokenExpiration, not(originalExpiration));
        assertThat(app.timeOfTokenExpiration, is(appToken.timeOfExpiration));
        
        verify(appRepo).saveApplication(app);
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

        RecreateApplicationTokenRequest emptyRequest = new RecreateApplicationTokenRequest();
        assertThrows(() -> instance.process(emptyRequest))
            .isInstanceOf(InvalidArgumentException.class);

        RecreateApplicationTokenRequest requestWithoutToken = new RecreateApplicationTokenRequest(request);
        requestWithoutToken.unsetToken();
        assertThrows(() -> instance.process(requestWithoutToken))
            .isInstanceOf(InvalidArgumentException.class);

        RecreateApplicationTokenRequest requestWithoutAppId = new RecreateApplicationTokenRequest(request);
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
    }

    private void setupData()
    {
        app = one(applications());
        app.applicationId = appId;
        
        request.token.userId = userId;
        request.applicationId = appId;

        appToken.tokenId = tokenId;
        authToken.tokenId = tokenId;

        app.owners.add(userId);

        Instant tokenExpiration = one(futureInstants());
        authToken.timeOfExpiration = tokenExpiration.toEpochMilli();
        appToken.timeOfExpiration = authToken.timeOfExpiration;
    }

}
