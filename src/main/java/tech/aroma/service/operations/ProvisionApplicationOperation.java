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

import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.LengthOfTime;
import tech.aroma.thrift.TimeUnit;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.InvalidTokenException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.service.AromaServiceConstants;
import tech.aroma.thrift.service.ProvisionApplicationRequest;
import tech.aroma.thrift.service.ProvisionApplicationResponse;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.time.Instant.now;
import static tech.aroma.data.assertions.AuthenticationAssertions.completeToken;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthLessThanOrEqualTo;

/**
 *
 * @author SirWellington
 */
@Internal
final class ProvisionApplicationOperation implements ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(ProvisionApplicationOperation.class);

    private final ApplicationRepository appRepo;
    private final MediaRepository mediaRepo;
    private final UserRepository userRepo;
    private final AuthenticationService.Iface authenticationService;
    private final Function<AuthenticationToken, ApplicationToken> appTokenMapper;

    @Inject
    ProvisionApplicationOperation(ApplicationRepository appRepo,
                                  MediaRepository mediaRepo,
                                  UserRepository userRepo,
                                  AuthenticationService.Iface authenticationService,
                                  Function<AuthenticationToken, ApplicationToken> appTokenMapper)
    {
        checkThat(appRepo, mediaRepo, userRepo, authenticationService, appTokenMapper)
            .are(notNull());

        this.appRepo = appRepo;
        this.mediaRepo = mediaRepo;
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

        LOG.debug("Owner ID {} Maps to user {}", authTokenForUser.ownerId, user);

        Application app = createAppFrom(request, user);

        AuthenticationToken authTokenForApp = createAppTokenFor(app);
        ApplicationToken appToken = appTokenMapper.apply(authTokenForApp);

        if (hasIcon(request))
        {
            String mediaIdForIcon = app.applicationId;

            saveIcon(mediaIdForIcon, request.icon);
            app.setApplicationIconMediaId(mediaIdForIcon);
        }

        //Save time of token expiration
        app.setTimeOfTokenExpiration(appToken.timeOfExpiration);

        appRepo.saveApplication(app);

        return new ProvisionApplicationResponse()
            .setApplicationInfo(app)
            .setApplicationToken(appToken);

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
            
            checkThat(request.applicationName)
                .usingMessage("Application name is required")
                .is(nonEmptyString())
                .usingMessage("Application name is too long")
                .is(stringWithLengthLessThanOrEqualTo(AromaServiceConstants.APPLICATION_NAME_MAX_LENGTH));
        };
    }

    private void saveIcon(String mediaId, Image icon) throws TException
    {
        mediaRepo.saveMedia(mediaId, icon);
    }

    private boolean hasIcon(ProvisionApplicationRequest request)
    {
        if (request.isSetIcon())
        {
            byte[] icon = request.getIcon().getData();

            if (icon != null && icon.length > 0)
            {
                return true;
            }
        }

        return false;
    }
}
