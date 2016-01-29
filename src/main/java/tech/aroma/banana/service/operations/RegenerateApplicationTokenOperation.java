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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.TimeUnit;
import tech.aroma.banana.thrift.authentication.ApplicationToken;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.TokenType;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.banana.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.banana.thrift.authentication.service.InvalidateTokenRequest;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.exceptions.UnauthorizedException;
import tech.aroma.banana.thrift.service.RegenerateApplicationTokenRequest;
import tech.aroma.banana.thrift.service.RegenerateApplicationTokenResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.data.assertions.AuthenticationAssertions.completeToken;
import static tech.aroma.banana.data.assertions.RequestAssertions.validAppId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.elementInCollection;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.inTheFuture;

/**
 *
 * @author SirWellington
 */
final class RegenerateApplicationTokenOperation implements ThriftOperation<RegenerateApplicationTokenRequest, RegenerateApplicationTokenResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(RegenerateApplicationTokenOperation.class);

    private final AuthenticationService.Iface authenticationService;
    private final ApplicationRepository appRepo;
    private final Function<AuthenticationToken, ApplicationToken> tokenMapper;

    @Inject
    RegenerateApplicationTokenOperation(AuthenticationService.Iface authenticationService,
                                        ApplicationRepository appRepo,
                                        Function<AuthenticationToken, ApplicationToken> tokenMapper)
    {
        checkThat(authenticationService, appRepo, tokenMapper)
            .are(notNull());

        this.authenticationService = authenticationService;
        this.appRepo = appRepo;
        this.tokenMapper = tokenMapper;
    }

    @Override
    public RegenerateApplicationTokenResponse process(RegenerateApplicationTokenRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        String userId = request.token.userId;
        String appId = request.applicationId;
        Application app = appRepo.getById(appId);

        checkThat(userId)
            .throwing(UnauthorizedException.class)
            .is(elementInCollection(app.owners));

        deleteTokensFor(appId);

        ApplicationToken appToken = createNewTokenFor(app);

        LOG.debug("App Token successfully regenerated");

        return new RegenerateApplicationTokenResponse()
            .setApplicationToken(appToken);

        //Get User ID
        //Get App Info
        //Assert user has authorization to do this
        //Get the App's current token
        //When creating the token, be sure that 
        //Delete existing tokens for App
        //Create new token
        //Return token
    }
    
    private AlchemyAssertion<RegenerateApplicationTokenRequest> good()
    {
        return request ->
        {
            checkThat(request)
                .usingMessage("request is missing")
                .is(notNull());
            
            checkThat(request.applicationId)
                .is(validAppId());
            
            checkThat(request.token)
                .is(notNull());
            
            checkThat(request.token.userId)
                .usingMessage("Request is missing userId in token")
                .is(nonEmptyString());
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
        LengthOfTime timeToLive = determineNewAppTokenLifetimeFor(app);

        return new CreateTokenRequest()
            .setOwnerId(app.applicationId)
            .setDesiredTokenType(TokenType.APPLICATION)
            .setLifetime(timeToLive)
            .setOrganizationId(app.organizationId)
            .setOwnerName(app.name);
    }

    private LengthOfTime determineNewAppTokenLifetimeFor(Application app) throws InvalidArgumentException
    {
        Instant expiration = Instant.ofEpochMilli(app.timeOfTokenExpiration);
        checkThat(expiration)
            .throwing(InvalidArgumentException.class)
            .usingMessage("App's Token already expired on: " + expiration)
            .is(inTheFuture());

        Instant now = Instant.now();
        long secondsUntil = now.until(expiration, ChronoUnit.SECONDS);
        return new LengthOfTime(TimeUnit.SECONDS, secondsUntil);
    }

}
