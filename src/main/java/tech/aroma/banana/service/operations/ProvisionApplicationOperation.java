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

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.TimeUnit;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.authentication.ApplicationToken;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.TokenType;
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.banana.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.service.ProvisionApplicationRequest;
import tech.aroma.banana.thrift.service.ProvisionApplicationResponse;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.time.Instant.now;
import static tech.aroma.banana.data.assertions.AuthenticationAssertions.completeToken;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
final class ProvisionApplicationOperation implements ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse>
{
    
    private final static Logger LOG = LoggerFactory.getLogger(ProvisionApplicationOperation.class);
    
    private final ApplicationRepository appRepo;
    private final UserRepository userRepo;
    private final AuthenticationService.Iface authenticationService;
    private final Function<AuthenticationToken, ApplicationToken> appTokenMapper;

    @Inject
    ProvisionApplicationOperation(ApplicationRepository appRepo,
                                  UserRepository userRepo,
                                  AuthenticationService.Iface authenticationService,
                                  Function<AuthenticationToken, ApplicationToken> appTokenMapper)
    {
        checkThat(appRepo, userRepo, authenticationService, appTokenMapper)
            .are(notNull());
        
        this.appRepo = appRepo;
        this.userRepo = userRepo;
        this.authenticationService = authenticationService;
        this.appTokenMapper = appTokenMapper;
    }
    
    @Override
    public ProvisionApplicationResponse process(ProvisionApplicationRequest request) throws TException
    {
        LOG.info("Received request to provision an Application", request);
        
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        AuthenticationToken authTokenForUser = getUserTokenFrom(request.token);
        User user = userRepo.getUser(authTokenForUser.ownerId);
        
        Application app = createAppFrom(request, user);
        appRepo.saveApplication(app);
        
        AuthenticationToken authTokenForApp = createAppTokenFor(app);
        ApplicationToken appToken = appTokenMapper.apply(authTokenForApp);
        
        return new ProvisionApplicationResponse()
            .setApplicationInfo(app)
            .setApplicationToken(appToken);

        //Get User from token
        //Create the Application object
        //Store application object
        //Create Token for Application
        //Return token
    }

    
    private AuthenticationToken getUserTokenFrom(UserToken token) throws InvalidTokenException, OperationFailedException
    {
        GetTokenInfoRequest request = new GetTokenInfoRequest()
            .setTokenId(token.tokenId)
            .setTokenType(TokenType.USER);
        
        GetTokenInfoResponse response;
        
        try
        {
            response = authenticationService.getTokenInfo(request);
        }
        catch (InvalidTokenException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            LOG.error("Failed to get token info from Authentication Service for {}", token, ex);
            throw new OperationFailedException("Token invalid: " + ex.getMessage());
        }
        
        checkThat(response.token)
            .throwing(OperationFailedException.class)
            .usingMessage("Auth service returned null response")
            .is(notNull());
        
        return response.token;
    }
    
    private Application createAppFrom(ProvisionApplicationRequest request, User user)
    {
        Set<String> owners = Sets.copyOf(request.owners);
        //Creating user is automatically an Owner
        owners.add(user.userId);
        String appId = UUID.randomUUID().toString();
        
        return new Application()
            .setApplicationId(appId)
            .setName(request.applicationName)
            .setApplicationDescription(request.applicationDescription)
            .setOrganizationId(request.organizationId)
            .setTier(request.tier)
            .setProgrammingLanguage(request.programmingLanguage)
            .setTimeOfProvisioning(now().toEpochMilli())
            .setTotalMessagesSent(0L)
            .setOwners(owners);
    }
    
    private AuthenticationToken createAppTokenFor(Application app) throws OperationFailedException
    {
        LengthOfTime lifetime = new LengthOfTime()
            .setUnit(TimeUnit.DAYS)
            .setValue(180);
        
        CreateTokenRequest request = new CreateTokenRequest()
            .setDesiredTokenType(TokenType.APPLICATION)
            .setLifetime(lifetime)
            .setOrganizationId(app.organizationId)
            .setOwnerId(app.applicationId)
            .setOwnerName(app.name);
        
        CreateTokenResponse response;
        
        try
        {
            response = authenticationService.createToken(request);
        }
        catch (TException ex)
        {
            LOG.error("Failed to create Token for Application: {}", app, ex);
            throw new OperationFailedException("Could not create token for app: " + ex.getMessage());
        }
        
        checkThat(response)
            .throwing(OperationFailedException.class)
            .usingMessage("Authentication Service returned null")
            .is(notNull());
        
        checkThat(response.token)
            .usingMessage("Auth Service returned incomplete token")
            .throwing(OperationFailedException.class)
            .is(completeToken());
        
        return response.token;
    }
        
    private AlchemyAssertion<ProvisionApplicationRequest> good()
    {
        return request ->
        {
            checkThat(request)
                .usingMessage("request is null")
                .is(notNull());
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
        };
    }
}
