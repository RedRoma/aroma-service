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

package tech.aroma.service;

import decorice.DecoratedBy;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.thrift.authentication.service.VerifyTokenRequest;
import tech.aroma.thrift.exceptions.AccountAlreadyExistsException;
import tech.aroma.thrift.exceptions.ApplicationAlreadyRegisteredException;
import tech.aroma.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.thrift.exceptions.ChannelDoesNotExistException;
import tech.aroma.thrift.exceptions.CustomChannelUnreachableException;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.thrift.exceptions.InvalidTokenException;
import tech.aroma.thrift.exceptions.MessageDoesNotExistException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.service.AromaService;
import tech.aroma.thrift.service.DeleteMessageRequest;
import tech.aroma.thrift.service.DeleteMessageResponse;
import tech.aroma.thrift.service.DismissMessageRequest;
import tech.aroma.thrift.service.DismissMessageResponse;
import tech.aroma.thrift.service.FollowApplicationRequest;
import tech.aroma.thrift.service.FollowApplicationResponse;
import tech.aroma.thrift.service.GetActivityRequest;
import tech.aroma.thrift.service.GetActivityResponse;
import tech.aroma.thrift.service.GetApplicationInfoRequest;
import tech.aroma.thrift.service.GetApplicationInfoResponse;
import tech.aroma.thrift.service.GetApplicationMessagesRequest;
import tech.aroma.thrift.service.GetApplicationMessagesResponse;
import tech.aroma.thrift.service.GetBuzzRequest;
import tech.aroma.thrift.service.GetBuzzResponse;
import tech.aroma.thrift.service.GetDashboardRequest;
import tech.aroma.thrift.service.GetDashboardResponse;
import tech.aroma.thrift.service.GetFullMessageRequest;
import tech.aroma.thrift.service.GetFullMessageResponse;
import tech.aroma.thrift.service.GetInboxRequest;
import tech.aroma.thrift.service.GetInboxResponse;
import tech.aroma.thrift.service.GetMediaRequest;
import tech.aroma.thrift.service.GetMediaResponse;
import tech.aroma.thrift.service.GetMyApplicationsRequest;
import tech.aroma.thrift.service.GetMyApplicationsResponse;
import tech.aroma.thrift.service.GetMySavedChannelsRequest;
import tech.aroma.thrift.service.GetMySavedChannelsResponse;
import tech.aroma.thrift.service.GetUserInfoRequest;
import tech.aroma.thrift.service.GetUserInfoResponse;
import tech.aroma.thrift.service.ProvisionApplicationRequest;
import tech.aroma.thrift.service.ProvisionApplicationResponse;
import tech.aroma.thrift.service.RegenerateApplicationTokenRequest;
import tech.aroma.thrift.service.RegenerateApplicationTokenResponse;
import tech.aroma.thrift.service.RegisterHealthCheckRequest;
import tech.aroma.thrift.service.RegisterHealthCheckResponse;
import tech.aroma.thrift.service.RemoveSavedChannelRequest;
import tech.aroma.thrift.service.RemoveSavedChannelResponse;
import tech.aroma.thrift.service.RenewApplicationTokenRequest;
import tech.aroma.thrift.service.RenewApplicationTokenResponse;
import tech.aroma.thrift.service.SaveChannelRequest;
import tech.aroma.thrift.service.SaveChannelResponse;
import tech.aroma.thrift.service.SearchForApplicationsRequest;
import tech.aroma.thrift.service.SearchForApplicationsResponse;
import tech.aroma.thrift.service.SignInRequest;
import tech.aroma.thrift.service.SignInResponse;
import tech.aroma.thrift.service.SignUpRequest;
import tech.aroma.thrift.service.SignUpResponse;
import tech.aroma.thrift.service.SnoozeChannelRequest;
import tech.aroma.thrift.service.SnoozeChannelResponse;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern;

import static tech.aroma.service.AromaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern.Role.DECORATOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

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
    public RegenerateApplicationTokenResponse regenerateToken(RegenerateApplicationTokenRequest request) throws OperationFailedException,
                                                                                                                InvalidArgumentException,
                                                                                                                InvalidCredentialsException,
                                                                                                                ApplicationDoesNotExistException,
                                                                                                                UnauthorizedException,
                                                                                                                TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.regenerateToken(request);
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
    public RemoveSavedChannelResponse removeSavedChannel(RemoveSavedChannelRequest request) throws OperationFailedException,
                                                                                                   InvalidArgumentException,
                                                                                                   InvalidCredentialsException,
                                                                                                   UnauthorizedException,
                                                                                                   ChannelDoesNotExistException,
                                                                                                   TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.removeSavedChannel(request);
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
    public SaveChannelResponse saveChannel(SaveChannelRequest request) throws OperationFailedException, 
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException, 
                                                                              UnauthorizedException,
                                                                              TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.saveChannel(request);
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
    public SnoozeChannelResponse snoozeChannel(SnoozeChannelRequest request) throws OperationFailedException,
                                                                                    InvalidArgumentException,
                                                                                    InvalidCredentialsException,
                                                                                    UnauthorizedException,
                                                                                    ChannelDoesNotExistException,
                                                                                    TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.snoozeChannel(request);
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
    public GetInboxResponse getInbox(GetInboxRequest request) throws OperationFailedException, InvalidArgumentException,
                                                                     InvalidTokenException, TException
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
    public GetMyApplicationsResponse getMyApplications(GetMyApplicationsRequest request) throws OperationFailedException,
                                                                                                InvalidArgumentException,
                                                                                                InvalidCredentialsException,
                                                                                                TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getMyApplications(request);
    }

    @Override
    public GetMySavedChannelsResponse getMySavedChannels(GetMySavedChannelsRequest request) throws OperationFailedException,
                                                                                                   InvalidArgumentException,
                                                                                                   InvalidCredentialsException,
                                                                                                   TException
    {
        checkNotNull(request);
        checkAndEnrichToken(request.token);

        return delegate.getMySavedChannels(request);
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
        
        if(request.isSetToken())
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

    private void checkAndEnrichToken(UserToken token) throws InvalidTokenException, TException
    {
        checkTokenIsValid(token);
        
        if(token.isSetUserId())
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
