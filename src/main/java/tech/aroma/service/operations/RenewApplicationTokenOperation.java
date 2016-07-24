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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.TokenRepository;
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
import tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.time.Clock.systemUTC;
import static tech.aroma.data.assertions.AuthenticationAssertions.completeToken;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.elementInCollection;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.inTheFuture;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 * This operation extends the lifetime of an Application's Token.
 * @author SirWellington
 */
final class RenewApplicationTokenOperation implements ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(RenewApplicationTokenOperation.class);

    private final AuthenticationService.Iface authenticationService;
    private final ApplicationRepository appRepo;
    private final TokenRepository tokenRepo;
    private final Function<AuthenticationToken, ApplicationToken> tokenMapper;

    @Inject
    RenewApplicationTokenOperation(AuthenticationService.Iface authenticationService,
                                        ApplicationRepository appRepo,
                                        TokenRepository tokenRepo,
                                        Function<AuthenticationToken, ApplicationToken> tokenMapper)
    {
        checkThat(authenticationService, appRepo, tokenRepo, tokenMapper)
            .are(notNull());

        this.authenticationService = authenticationService;
        this.appRepo = appRepo;
        this.tokenRepo = tokenRepo;
        this.tokenMapper = tokenMapper;
    }

    @Override
    public RenewApplicationTokenResponse process(RenewApplicationTokenRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        //Get User Info
        //Ensure user can perform this operation
        //Get the App's current token
        //Extend it's lifetime
        //Save it
        //Update the Application object with the new expiration date
        //Return the updated token
        
        String userId = request.token.userId;
        String appId = request.applicationId;
        Application app = appRepo.getById(appId);
        
        updateTokensForApp(app);
        
        checkThat(userId)
            .throwing(UnauthorizedException.class)
            .is(elementInCollection(app.owners));

        deleteTokensFor(appId);

        ApplicationToken appToken = createNewTokenFor(app);

        LOG.debug("App Token successfully regenerated");

        return new RenewApplicationTokenResponse()
            .setApplicationToken(appToken);

    }
    
    private AlchemyAssertion<RenewApplicationTokenRequest> good()
    {
        return request ->
        {
            checkThat(request)
                .usingMessage("request is missing")
                .is(notNull());
            
            checkThat(request.applicationId)
                .is(validApplicationId());
            
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

    private void updateTokensForApp(Application app) throws TException
    {
        List<AuthenticationToken> tokens = tokenRepo.getTokensBelongingTo(app.applicationId);
        
        checkThat(tokens)
            .throwing(InvalidArgumentException.class)
            .usingMessage(app.name +" currently has no tokens. Recreate one instead.")
            .is(CollectionAssertions.nonEmptyList())
            .throwing(OperationFailedException.class)
            .usingMessage(app.name + " currently has more than one token.")
            .is(CollectionAssertions.collectionOfSize(1));
            
            
        AuthenticationToken currentToken = tokens.get(0);
        
        setNewExpiration(currentToken);
    }

    private void setNewExpiration(AuthenticationToken currentToken)
    {
        Instant now = Instant.now(systemUTC());
        
        
    }

}
