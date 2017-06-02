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

package tech.aroma.service;

import javax.inject.Inject;

import decorice.DecoratedBy;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.authentication.*;
import tech.aroma.thrift.authentication.service.*;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.service.*;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern;

import static tech.aroma.service.AromaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern.Role.DECORATOR;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 * This Layer decorates an existing {@link AromaService.Iface} and authenticates calls against an
 * {@linkplain AuthenticationService.Iface Authentication Service}.
 *
 * @author SirWellington
 */
@Internal
@DecoratorPattern(role = DECORATOR)
final class AuthenticationLayer implements AromaService.Iface
{

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationLayer.class);

    private final AromaService.Iface delegate;
    private final AuthenticationService.Iface authenticationService;

    @Inject
    AuthenticationLayer(@DecoratedBy(AuthenticationLayer.class) AromaService.Iface delegate,
                        AuthenticationService.Iface authenticationService)
    {
        checkThat(delegate, authenticationService)
                .are(notNull());

        this.delegate = delegate;
        this.authenticationService = authenticationService;
    }

    @Override
    public double getApiVersion() throws TException
    {
        return delegate.getApiVersion();
    }

    @Override
    public DeleteApplicationResponse deleteApplication(DeleteApplicationRequest request) throws OperationFailedException,
                                                                                                InvalidArgumentException,
                                                                                                InvalidTokenException,
                                                                                                ApplicationDoesNotExistException,
                                                                                                UnauthorizedException,
                                                                                                TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.deleteApplication(request);
    }

    @Override
    public DeleteMessageResponse deleteMessage(DeleteMessageRequest request) throws OperationFailedException,
                                                                                    InvalidArgumentException,
                                                                                    InvalidTokenException,
                                                                                    MessageDoesNotExistException,
                                                                                    UnauthorizedException,
                                                                                    TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.deleteMessage(request);
    }

    @Override
    public DismissMessageResponse dismissMessage(DismissMessageRequest request) throws OperationFailedException,
                                                                                       InvalidArgumentException,
                                                                                       InvalidTokenException,
                                                                                       MessageDoesNotExistException,
                                                                                       UnauthorizedException,
                                                                                       TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.dismissMessage(request);
    }

    @Override
    public FollowApplicationResponse followApplication(FollowApplicationRequest request) throws OperationFailedException,
                                                                                                InvalidArgumentException,
                                                                                                InvalidTokenException,
                                                                                                ApplicationDoesNotExistException,
                                                                                                ApplicationAlreadyRegisteredException,
                                                                                                CustomChannelUnreachableException,
                                                                                                TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.followApplication(request);
    }

    @Override
    public ProvisionApplicationResponse provisionApplication(ProvisionApplicationRequest request) throws OperationFailedException,
                                                                                                         InvalidArgumentException,
                                                                                                         InvalidCredentialsException,
                                                                                                         ApplicationDoesNotExistException,
                                                                                                         UnauthorizedException,
                                                                                                         TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.provisionApplication(request);
    }

    @Override
    public RecreateApplicationTokenResponse recreateToken(RecreateApplicationTokenRequest request) throws OperationFailedException,
                                                                                                          InvalidArgumentException,
                                                                                                          InvalidCredentialsException,
                                                                                                          ApplicationDoesNotExistException,
                                                                                                          UnauthorizedException,
                                                                                                          TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.recreateToken(request);
    }

    @Override
    public RegisterHealthCheckResponse registerHealthCheck(RegisterHealthCheckRequest request) throws OperationFailedException,
                                                                                                      InvalidArgumentException,
                                                                                                      InvalidCredentialsException,
                                                                                                      ApplicationDoesNotExistException,
                                                                                                      UnauthorizedException,
                                                                                                      TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.registerHealthCheck(request);
    }

    @Override
    public RenewApplicationTokenResponse renewApplicationToken(RenewApplicationTokenRequest request) throws OperationFailedException,
                                                                                                            InvalidArgumentException,
                                                                                                            InvalidCredentialsException,
                                                                                                            ApplicationDoesNotExistException,
                                                                                                            UnauthorizedException,
                                                                                                            TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.renewApplicationToken(request);
    }

    @Override
    public SignInResponse signIn(SignInRequest request) throws OperationFailedException,
                                                               InvalidArgumentException,
                                                               InvalidCredentialsException,
                                                               UserDoesNotExistException,
                                                               TException
    {
        checkNotNull(request);

        return delegate.signIn(request);
    }

    @Override
    public SignUpResponse signUp(SignUpRequest request) throws OperationFailedException,
                                                               InvalidArgumentException,
                                                               InvalidCredentialsException,
                                                               AccountAlreadyExistsException,
                                                               TException
    {
        checkNotNull(request);

        return delegate.signUp(request);
    }

    @Override
    public GetActivityResponse getActivity(GetActivityRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException,
                                                                              TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getActivity(request);
    }

    @Override
    public GetApplicationInfoResponse getApplicationInfo(GetApplicationInfoRequest request) throws OperationFailedException,
                                                                                                   InvalidArgumentException,
                                                                                                   InvalidCredentialsException,
                                                                                                   ApplicationDoesNotExistException,
                                                                                                   UnauthorizedException,
                                                                                                   TException
    {
        checkNotNull(request);
        checkToken(request.token);

        return delegate.getApplicationInfo(request);
    }

    @Override
    public GetDashboardResponse getDashboard(GetDashboardRequest request) throws OperationFailedException,
                                                                                 InvalidArgumentException,
                                                                                 InvalidCredentialsException,
                                                                                 TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getDashboard(request);
    }

    @Override
    public GetApplicationMessagesResponse getApplicationMessages(GetApplicationMessagesRequest request) throws OperationFailedException,
                                                                                                               InvalidArgumentException,
                                                                                                               InvalidTokenException,
                                                                                                               UnauthorizedException,
                                                                                                               TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getApplicationMessages(request);
    }

    @Override
    public GetInboxResponse getInbox(GetInboxRequest request) throws OperationFailedException,
                                                                     InvalidArgumentException,
                                                                     InvalidTokenException,
                                                                     TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getInbox(request);
    }

    @Override
    public GetFullMessageResponse getFullMessage(GetFullMessageRequest request) throws OperationFailedException,
                                                                                       InvalidArgumentException,
                                                                                       InvalidCredentialsException,
                                                                                       TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getFullMessage(request);
    }

    @Override
    public GetMediaResponse getMedia(GetMediaRequest request) throws OperationFailedException,
                                                                     InvalidArgumentException,
                                                                     InvalidTokenException,
                                                                     DoesNotExistException,
                                                                     UnauthorizedException,
                                                                     TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getMedia(request);
    }

    @Override
    public GetApplicationsOwnedByResponse getApplicationsOwnedBy(GetApplicationsOwnedByRequest request) throws OperationFailedException,
                                                                                                               InvalidArgumentException,
                                                                                                               InvalidCredentialsException,
                                                                                                               TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getApplicationsOwnedBy(request);
    }

    @Override
    public GetApplicationsFollowedByResponse getApplicationsFollowedBy(GetApplicationsFollowedByRequest request) throws OperationFailedException,
                                                                                                                        InvalidArgumentException,
                                                                                                                        InvalidCredentialsException,
                                                                                                                        TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getApplicationsFollowedBy(request);
    }

    @Override
    public GetBuzzResponse getBuzz(GetBuzzRequest request) throws OperationFailedException,
                                                                  InvalidArgumentException,
                                                                  InvalidTokenException,
                                                                  ApplicationDoesNotExistException,
                                                                  UnauthorizedException,
                                                                  TException
    {
        checkNotNull(request);

        if (request.isSetToken())
        {
            checkAndEnrichToken(request.token);
        }

        return delegate.getBuzz(request);
    }

    @Override
    public GetUserInfoResponse getUserInfo(GetUserInfoRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidTokenException,
                                                                              UnauthorizedException,
                                                                              UserDoesNotExistException,
                                                                              TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getUserInfo(request);
    }

    @Override
    public SearchForApplicationsResponse searchForApplications(SearchForApplicationsRequest request) throws OperationFailedException,
                                                                                                            InvalidArgumentException,
                                                                                                            InvalidCredentialsException,
                                                                                                            UnauthorizedException,
                                                                                                            TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.searchForApplications(request);
    }

    @Override
    public UpdateApplicationResponse updateApplication(UpdateApplicationRequest request) throws OperationFailedException,
                                                                                                InvalidArgumentException,
                                                                                                InvalidTokenException,
                                                                                                ApplicationDoesNotExistException,
                                                                                                UnauthorizedException,
                                                                                                TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.updateApplication(request);
    }

    @Override
    public UnfollowApplicationResponse unfollowApplication(UnfollowApplicationRequest request) throws OperationFailedException,
                                                                                                      InvalidArgumentException,
                                                                                                      InvalidTokenException,
                                                                                                      ApplicationDoesNotExistException,
                                                                                                      UnauthorizedException,
                                                                                                      TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.unfollowApplication(request);
    }


    @Override
    public UpdateReactionsResponse updateReactions(UpdateReactionsRequest request) throws OperationFailedException,
                                                                                          InvalidArgumentException,
                                                                                          InvalidTokenException,
                                                                                          ApplicationDoesNotExistException,
                                                                                          UnauthorizedException,
                                                                                          TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.updateReactions(request);
    }

    @Override
    public GetReactionsResponse getReactions(GetReactionsRequest request) throws OperationFailedException,
                                                                                 InvalidArgumentException,
                                                                                 InvalidTokenException,
                                                                                 ApplicationDoesNotExistException,
                                                                                 UnauthorizedException,
                                                                                 TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getReactions(request);
    }


    //==========================================================
    // DEVICE REGISTRATION OPERATIONS
    //==========================================================


    @Override
    public CheckIfDeviceIsRegisteredResponse checkIfDeviceIsRegistered(CheckIfDeviceIsRegisteredRequest request) throws OperationFailedException,
                                                                                                                        InvalidArgumentException,
                                                                                                                        InvalidTokenException,
                                                                                                                        UnauthorizedException,
                                                                                                                        TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.checkIfDeviceIsRegistered(request);
    }

    @Override
    public GetRegisteredDevicesResponse getRegisteredDevices(GetRegisteredDevicesRequest request) throws OperationFailedException,
                                                                                                         InvalidArgumentException,
                                                                                                         InvalidTokenException,
                                                                                                         UnauthorizedException,
                                                                                                         TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getRegisteredDevices(request);
    }

    @Override
    public RegisterDeviceResponse registerDevice(RegisterDeviceRequest request) throws OperationFailedException,
                                                                                       InvalidArgumentException,
                                                                                       InvalidTokenException,
                                                                                       UnauthorizedException,
                                                                                       TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.registerDevice(request);
    }

    @Override
    public UnregisterDeviceResponse unregisterDevice(UnregisterDeviceRequest request) throws OperationFailedException,
                                                                                             InvalidArgumentException,
                                                                                             InvalidTokenException,
                                                                                             UnauthorizedException,
                                                                                             TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.unregisterDevice(request);
    }


    //==========================================================
    // INTERNAL OPERATIONS
    //==========================================================
    private void checkAndEnrichToken(UserToken token) throws InvalidTokenException, TException
    {
        checkTokenIsValid(token);

        if (token.isSetUserId())
        {
            return;
        }

        String tokenId = token.tokenId;

        GetTokenInfoRequest request = new GetTokenInfoRequest()
                .setTokenId(tokenId)
                .setTokenType(TokenType.USER);

        GetTokenInfoResponse tokenInfo;
        try
        {
            tokenInfo = authenticationService.getTokenInfo(request);
        }
        catch (TException ex)
        {
            LOG.error("Failed to get additional token info from Authentication Service", ex);
            throw new OperationFailedException("Could not ascertain token info: " + ex.getMessage());
        }

        checkThat(tokenInfo)
                .throwing(OperationFailedException.class)
                .usingMessage("failed to enrich user token. Auth Service returned null response")
                .is(notNull());

        AuthenticationToken authToken = tokenInfo.token;

        token.setUserId(authToken.ownerId);
        token.setTimeOfExpiration(authToken.timeOfExpiration);
        token.setOrganization(authToken.organizationId);
    }

    private void checkTokenIsValid(UserToken token) throws TException
    {
        checkThat(token)
                .throwing(InvalidTokenException.class)
                .usingMessage("Request missing Token")
                .is(notNull());

        VerifyTokenRequest request = new VerifyTokenRequest()
                .setTokenId(token.tokenId)
                .setOwnerId(token.userId);

        try
        {
            authenticationService.verifyToken(request);
        }
        catch (TException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            LOG.error("Failed to make request to Authentication Service", ex);
            throw new OperationFailedException("Could not query Authentication Service for Token: " + ex.getMessage());
        }

    }

    private void checkToken(AuthenticationToken token) throws TException
    {
        checkThat(token)
                .throwing(InvalidTokenException.class)
                .usingMessage("Request missing Token")
                .is(notNull());

        checkThat(token.tokenId)
                .usingMessage("Request Token is Invalid")
                .throwing(InvalidTokenException.class)
                .is(nonEmptyString());

        VerifyTokenRequest request = new VerifyTokenRequest()
                .setTokenId(token.tokenId)
                .setOwnerId(token.ownerId);

        try
        {
            authenticationService.verifyToken(request);
        }
        catch (TException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            LOG.error("Failed to make request to Authentication Service", ex);
            throw new OperationFailedException("Could not query Authentication Service for Token: " + ex.getMessage());
        }
    }
}
