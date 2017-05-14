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
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.TokenRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.authentication.*;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.functions.TimeFunctions;
import tech.aroma.thrift.service.*;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.time.Clock.systemUTC;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.*;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 * This operation extends the lifetime of an Application's Token.
 *
 * @author SirWellington
 */

//Get User Info
//Ensure user can perform this operation
//Get the App's current token
//Extend it's lifetime
//Save it
//Update the Application object with the new expiration date
//Return the updated token

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

        String userId = request.token.userId;
        String appId = request.applicationId;
        Application app = appRepo.getById(appId);

        checkThat(userId)
                .throwing(UnauthorizedException.class)
                .is(elementInCollection(app.owners));

        AuthenticationToken currentToken = getCurrentTokenFor(app);

        updateTokenLifetime(currentToken);
        updateApplicationWithNewExpiration(app, currentToken);

        LOG.debug("App Token successfully renewed");

        ApplicationToken appToken = tokenMapper.apply(currentToken);

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

    private AuthenticationToken getCurrentTokenFor(Application app) throws TException
    {
        List<AuthenticationToken> tokens = tokenRepo.getTokensBelongingTo(app.applicationId);

        checkThat(tokens)
                .throwing(InvalidArgumentException.class)
                .usingMessage(app.name + " currently has no tokens. Recreate one instead.")
                .is(nonEmptyList())
                .throwing(OperationFailedException.class)
                .usingMessage(app.name + " currently has more than one token.")
                .is(collectionOfSize(1));

        AuthenticationToken currentToken = tokens.get(0);
        return currentToken;
    }

    private void updateTokenLifetime(AuthenticationToken token) throws TException
    {
        updateWithNewExpiration(token);
        saveToken(token);
    }

    private void updateWithNewExpiration(AuthenticationToken currentToken)
    {
        long newExpiration = getNewExpiration();
        currentToken.setTimeOfExpiration(newExpiration);
    }

    private long getNewExpiration()
    {
        Instant now = Instant.now(systemUTC());
        long lifetimeInSeconds = TimeFunctions.toSeconds(AromaServiceConstants.DEFAULT_APP_TOKEN_LIFETIME);

        Instant newExpiration = now.plusSeconds(lifetimeInSeconds);
        return newExpiration.toEpochMilli();
    }

    private void saveToken(AuthenticationToken currentToken) throws TException
    {
        currentToken.setStatus(TokenStatus.ACTIVE);
        tokenRepo.saveToken(currentToken);
    }

    private void updateApplicationWithNewExpiration(Application app, AuthenticationToken updatedToken) throws TException
    {
        app.setTimeOfTokenExpiration(updatedToken.timeOfExpiration);
        appRepo.saveApplication(app);
    }

}
