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

import java.util.*;
import java.util.function.Function;
import javax.inject.Inject;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.*;
import tech.aroma.thrift.*;
import tech.aroma.thrift.authentication.*;
import tech.aroma.thrift.authentication.service.*;
import tech.aroma.thrift.email.EmailMessage;
import tech.aroma.thrift.email.EmailNewApplication;
import tech.aroma.thrift.email.service.EmailService;
import tech.aroma.thrift.email.service.SendEmailRequest;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.service.*;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.time.Instant.now;
import static tech.aroma.data.assertions.AuthenticationAssertions.completeToken;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 * @author SirWellington
 */
@Internal
final class ProvisionApplicationOperation implements ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(ProvisionApplicationOperation.class);

    private final ApplicationRepository appRepo;
    private final FollowerRepository followerRepo;
    private final MediaRepository mediaRepo;
    private final UserRepository userRepo;
    private final AuthenticationService.Iface authenticationService;
    private final EmailService.Iface emailService;
    private final Function<AuthenticationToken, ApplicationToken> appTokenMapper;

    @Inject
    ProvisionApplicationOperation(ApplicationRepository appRepo,
                                  FollowerRepository followerRepo,
                                  MediaRepository mediaRepo,
                                  UserRepository userRepo,
                                  AuthenticationService.Iface authenticationService,
                                  EmailService.Iface emailService,
                                  Function<AuthenticationToken, ApplicationToken> appTokenMapper)
    {
        checkThat(appRepo,
                  followerRepo,
                  mediaRepo,
                  userRepo,
                  authenticationService,
                  emailService,
                  appTokenMapper)
                .are(notNull());

        this.appRepo = appRepo;
        this.followerRepo = followerRepo;
        this.mediaRepo = mediaRepo;
        this.userRepo = userRepo;
        this.authenticationService = authenticationService;
        this.emailService = emailService;
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

        saveOwnersAsFollowers(app);
        sendOutEmail(user, app, appToken, authTokenForUser);

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

    private void saveOwnersAsFollowers(Application app)
    {
        Sets.nullToEmpty(app.owners)
            .parallelStream()
            .map(this::getUserInfo)
            .filter(Objects::nonNull)
            .forEach(owner -> this.tryToSaveOwner(owner, app));
    }

    private User getUserInfo(String userId)
    {
        try
        {
            return userRepo.getUser(userId);
        }
        catch (TException ex)
        {
            LOG.warn("Could not get user info for Owner with ID [{}]", userId, ex);
            return null;
        }
    }

    private void tryToSaveOwner(User owner, Application app)
    {
        try
        {
            followerRepo.saveFollowing(owner, app);
        }
        catch (TException ex)
        {
            LOG.warn("Could not save Following Information between Owner [{}] and App [{}]", owner, app, ex);
        }
    }

    private void sendOutEmail(User user, Application app, ApplicationToken appToken, AuthenticationToken token)
    {
        EmailNewApplication newApplicationEmail = new EmailNewApplication()
                .setApp(app)
                .setCreator(user)
                .setAppToken(appToken);

        EmailMessage message = new EmailMessage();
        message.setNewApp(newApplicationEmail);

        SendEmailRequest request = new SendEmailRequest()
                .setEmailAddress(user.email)
                .setEmailMessage(message)
                .setToken(token);

        try
        {
            emailService.sendEmail(request);
            LOG.debug("Sent out Email for new App creation to {}", user.email);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to send out Email to {}", user.email, ex);
        }
    }
}
