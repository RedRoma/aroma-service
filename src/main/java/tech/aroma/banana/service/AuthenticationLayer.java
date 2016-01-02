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

package tech.aroma.banana.service;

import decorice.DecoratedBy;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.exceptions.AccountAlreadyExistsException;
import tech.aroma.banana.thrift.exceptions.ApplicationAlreadyRegisteredException;
import tech.aroma.banana.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.ChannelDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.CustomChannelUnreachableException;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.exceptions.UnauthorizedException;
import tech.aroma.banana.thrift.service.BananaService;
import tech.aroma.banana.thrift.service.GetActivityRequest;
import tech.aroma.banana.thrift.service.GetActivityResponse;
import tech.aroma.banana.thrift.service.GetApplicationInfoRequest;
import tech.aroma.banana.thrift.service.GetApplicationInfoResponse;
import tech.aroma.banana.thrift.service.GetDashboardRequest;
import tech.aroma.banana.thrift.service.GetDashboardResponse;
import tech.aroma.banana.thrift.service.GetFullMessageRequest;
import tech.aroma.banana.thrift.service.GetFullMessageResponse;
import tech.aroma.banana.thrift.service.GetMessagesRequest;
import tech.aroma.banana.thrift.service.GetMessagesResponse;
import tech.aroma.banana.thrift.service.GetMyApplicationsRequest;
import tech.aroma.banana.thrift.service.GetMyApplicationsResponse;
import tech.aroma.banana.thrift.service.GetMySavedChannelsRequest;
import tech.aroma.banana.thrift.service.GetMySavedChannelsResponse;
import tech.aroma.banana.thrift.service.ProvisionApplicationRequest;
import tech.aroma.banana.thrift.service.ProvisionApplicationResponse;
import tech.aroma.banana.thrift.service.RegenerateApplicationTokenRequest;
import tech.aroma.banana.thrift.service.RegenerateApplicationTokenResponse;
import tech.aroma.banana.thrift.service.RegisterHealthCheckRequest;
import tech.aroma.banana.thrift.service.RegisterHealthCheckResponse;
import tech.aroma.banana.thrift.service.RemoveSavedChannelRequest;
import tech.aroma.banana.thrift.service.RemoveSavedChannelResponse;
import tech.aroma.banana.thrift.service.RenewApplicationTokenRequest;
import tech.aroma.banana.thrift.service.RenewApplicationTokenResponse;
import tech.aroma.banana.thrift.service.SaveChannelRequest;
import tech.aroma.banana.thrift.service.SaveChannelResponse;
import tech.aroma.banana.thrift.service.SearchForApplicationsRequest;
import tech.aroma.banana.thrift.service.SearchForApplicationsResponse;
import tech.aroma.banana.thrift.service.SignInRequest;
import tech.aroma.banana.thrift.service.SignInResponse;
import tech.aroma.banana.thrift.service.SignUpRequest;
import tech.aroma.banana.thrift.service.SignUpResponse;
import tech.aroma.banana.thrift.service.SnoozeChannelRequest;
import tech.aroma.banana.thrift.service.SnoozeChannelResponse;
import tech.aroma.banana.thrift.service.SubscribeToApplicationRequest;
import tech.aroma.banana.thrift.service.SubscribeToApplicationResponse;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern;

import static tech.aroma.banana.service.BananaAssertions.checkNotNull;
import static tech.aroma.banana.thrift.assertions.BananaAssertions.validTokenIn;
import static tech.aroma.banana.thrift.assertions.BananaAssertions.validUserTokenIn;
import static tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern.Role.CONCRETE_DECORATOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This Layer decorates an existing {@link BananaService.Iface} and authenticates calls again an
 * {@linkplain AuthenticationService.Iface Authentication Service}.
 *
 * @author SirWellington
 */
@Internal
@DecoratorPattern(role = CONCRETE_DECORATOR)
final class AuthenticationLayer implements BananaService.Iface
{

    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationLayer.class);

    private final BananaService.Iface delegate;
    private final AuthenticationService.Iface authenticationService;

    @Inject
    AuthenticationLayer(@DecoratedBy(AuthenticationLayer.class) BananaService.Iface delegate,
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
    public ProvisionApplicationResponse provisionApplication(ProvisionApplicationRequest request) throws OperationFailedException,
                                                                                                         InvalidArgumentException,
                                                                                                         InvalidCredentialsException,
                                                                                                         ApplicationDoesNotExistException,
                                                                                                         UnauthorizedException,
                                                                                                         TException
    {
        checkNotNull(request);
        checkToken(request.token);

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
        checkToken(request.token);

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
        checkToken(request.token);

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
        checkToken(request.token);

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
        checkToken(request.token);

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
        checkToken(request.token);

        return delegate.saveChannel(request);
    }

    @Override
    public SignInResponse signIn(SignInRequest request) throws OperationFailedException,
                                                               InvalidArgumentException,
                                                               InvalidCredentialsException,
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
        checkToken(request.token);

        return delegate.snoozeChannel(request);
    }

    @Override
    public SubscribeToApplicationResponse subscribeToApplication(SubscribeToApplicationRequest request) throws OperationFailedException,
                                                                                                               InvalidArgumentException,
                                                                                                               InvalidCredentialsException,
                                                                                                               ApplicationDoesNotExistException,
                                                                                                               ApplicationAlreadyRegisteredException,
                                                                                                               CustomChannelUnreachableException,
                                                                                                               TException
    {
        checkNotNull(request);
        checkToken(request.token);

        return delegate.subscribeToApplication(request);
    }

    @Override
    public GetActivityResponse getActivity(GetActivityRequest request) throws OperationFailedException, 
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException, 
                                                                              TException
    {
        checkNotNull(request);
        checkToken(request.token);

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
        checkToken(request.token);

        return delegate.getDashboard(request);
    }

    @Override
    public GetMessagesResponse getMessages(GetMessagesRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException, 
                                                                              TException
    {
        checkNotNull(request);
        checkToken(request.token);

        return delegate.getMessages(request);
    }

    @Override
    public GetFullMessageResponse getFullMessage(GetFullMessageRequest request) throws OperationFailedException,
                                                                                       InvalidArgumentException,
                                                                                       InvalidCredentialsException,
                                                                                       TException
    {
        checkNotNull(request);
        checkToken(request.token);

        return delegate.getFullMessage(request);
    }

    @Override
    public GetMyApplicationsResponse getMyApplications(GetMyApplicationsRequest request) throws OperationFailedException,
                                                                                                InvalidArgumentException,
                                                                                                InvalidCredentialsException,
                                                                                                TException
    {
        checkNotNull(request);
        checkToken(request.token);

        return delegate.getMyApplications(request);
    }

    @Override
    public GetMySavedChannelsResponse getMySavedChannels(GetMySavedChannelsRequest request) throws OperationFailedException,
                                                                                                   InvalidArgumentException,
                                                                                                   InvalidCredentialsException,
                                                                                                   TException
    {
        checkNotNull(request);
        checkToken(request.token);

        return delegate.getMySavedChannels(request);
    }

    @Override
    public SearchForApplicationsResponse searchForApplications(SearchForApplicationsRequest request) throws OperationFailedException,
                                                                                                            InvalidArgumentException,
                                                                                                            InvalidCredentialsException,
                                                                                                            UnauthorizedException,
                                                                                                            TException
    {
        checkNotNull(request);
        checkToken(request.token);

        return delegate.searchForApplications(request);
    }

    private void checkToken(UserToken token) throws InvalidTokenException
    {
        checkThat(token)
            .throwing(InvalidTokenException.class)
            .is(validUserTokenIn(authenticationService));
    }

    private void checkToken(AuthenticationToken token) throws InvalidTokenException
    {
        checkThat(token)
            .throwing(InvalidTokenException.class)
            .is(validTokenIn(authenticationService));
    }

}
