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

import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.TimeUnit;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.thrift.authentication.service.InvalidateTokenRequest;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.service.RenewApplicationTokenRequest;
import tech.aroma.thrift.service.RenewApplicationTokenResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.AuthenticationAssertions.completeToken;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.elementInCollection;

/**
 * This Operation deletes an App's existing token and creates a new one.
 * The Token's Lifetime should also be refreshed.
 * 
 * @author SirWellington
 */
final class RecreateApplicationTokenOperation implements ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(RecreateApplicationTokenOperation.class);
    private final static LengthOfTime DEFAULT_TOKEN_LIFETIME = new LengthOfTime(TimeUnit.DAYS, 180);

    private final AuthenticationService.Iface authenticationService;
    private final ApplicationRepository appRepo;
    private final Function<AuthenticationToken, ApplicationToken> tokenMapper;

    @Inject
    RecreateApplicationTokenOperation(AuthenticationService.Iface authenticationService, 
                                   ApplicationRepository appRepo,
                                   Function<AuthenticationToken, ApplicationToken> tokenMapper)
    {
        checkThat(authenticationService, appRepo, tokenMapper)
            .is(notNull());

        this.authenticationService = authenticationService;
        this.appRepo = appRepo;
        this.tokenMapper = tokenMapper;
    }

    @Override
    public RenewApplicationTokenResponse process(RenewApplicationTokenRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        String appId = request.applicationId;
        Application app = appRepo.getById(appId);

        String userId = request.token.userId;
        checkThat(userId)
            .throwing(UnauthorizedException.class)
            .is(elementInCollection(app.owners));

        deleteTokensFor(appId);
        
        ApplicationToken newToken = createNewTokenFor(app);
        
        app.setTimeOfTokenExpiration(newToken.timeOfExpiration);
        appRepo.saveApplication(app);
        
        return new RenewApplicationTokenResponse().setServiceToken(newToken);
    }

    private AlchemyAssertion<RenewApplicationTokenRequest> good()
    {
        return request ->
        {
            checkThat(request).is(notNull());
            
            checkThat(request.applicationId)
                .is(validApplicationId());
            
            checkThat(request.token)
                .usingMessage("missing user token")
                .is(notNull());
            
            checkThat(request.token.userId)
                .is(validUserId());
        };
    }

    private void deleteTokensFor(String appId) throws OperationFailedException
    {
        InvalidateTokenRequest request = new InvalidateTokenRequest()
            .setBelongingTo(appId);

        try
        {
            authenticationService.invalidateToken(request);
        }
        catch (TException ex)
        {
            LOG.error("Failed to delete all tokens for App {}", appId, ex);
            throw new OperationFailedException("Could not delete existing tokens for App: " + ex.getMessage());
        }
        
        LOG.debug("Successfully deleted tokens for App {}", appId);
    }

    private ApplicationToken createNewTokenFor(Application app) throws InvalidArgumentException, OperationFailedException
    {
        CreateTokenRequest request = createRequestToCreateTokenFor(app);

        CreateTokenResponse response;
        try
        {
            response = authenticationService.createToken(request);
        }
        catch (TException ex)
        {
            LOG.error("Failed to create token for App {}", app, ex);
            throw new OperationFailedException("Could not create a new Token for App: " + ex.getMessage());
        }

        AuthenticationToken authToken = response.token;

        checkThat(authToken)
            .throwing(OperationFailedException.class)
            .usingMessage("Authentication Service returned incomplete Token")
            .is(completeToken());

        return tokenMapper.apply(authToken);
    }

    private CreateTokenRequest createRequestToCreateTokenFor(Application app) throws InvalidArgumentException
    {
        LengthOfTime timeToLive = DEFAULT_TOKEN_LIFETIME;

        return new CreateTokenRequest()
            .setOwnerId(app.applicationId)
            .setDesiredTokenType(TokenType.APPLICATION)
            .setLifetime(timeToLive)
            .setOrganizationId(app.organizationId)
            .setOwnerName(app.name);
    }

}
