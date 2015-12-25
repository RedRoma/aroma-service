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

import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.exceptions.AccountAlreadyExistsException;
import tech.aroma.banana.thrift.exceptions.ApplicationAlreadyRegisteredException;
import tech.aroma.banana.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.ChannelDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.CustomChannelUnreachableException;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.exceptions.UnauthorizedException;
import tech.aroma.banana.thrift.service.BananaService;
import tech.aroma.banana.thrift.service.BananaServiceConstants;
import tech.aroma.banana.thrift.service.GetApplicationInfoRequest;
import tech.aroma.banana.thrift.service.GetApplicationInfoResponse;
import tech.aroma.banana.thrift.service.GetApplicationSubscribersRequest;
import tech.aroma.banana.thrift.service.GetApplicationSubscribersResponse;
import tech.aroma.banana.thrift.service.GetDashboardRequest;
import tech.aroma.banana.thrift.service.GetDashboardResponse;
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
import tech.aroma.banana.thrift.service.SendMessageRequest;
import tech.aroma.banana.thrift.service.SendMessageResponse;
import tech.aroma.banana.thrift.service.SignInRequest;
import tech.aroma.banana.thrift.service.SignInResponse;
import tech.aroma.banana.thrift.service.SignUpRequest;
import tech.aroma.banana.thrift.service.SignUpResponse;
import tech.aroma.banana.thrift.service.SnoozeChannelRequest;
import tech.aroma.banana.thrift.service.SnoozeChannelResponse;
import tech.aroma.banana.thrift.service.SubscribeToApplicationRequest;
import tech.aroma.banana.thrift.service.SubscribeToApplicationResponse;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.service.BananaAssertions.checkNotNull;
import static tech.aroma.banana.service.BananaAssertions.withMessage;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This is the Top Level of the Banana Service. All of the Operations arrive here and routed to their respective
 * {@linkplain ThriftOperation Operation}.
 *
 * @author SirWellington
 */
@Internal
final class BananaServiceImpl implements BananaService.Iface
{
    
    private final static Logger LOG = LoggerFactory.getLogger(BananaServiceImpl.class);

    private ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;
    private ThriftOperation<SignInRequest, SignInResponse> signInOperation;
    private ThriftOperation<SignUpRequest, SignUpResponse> signUpOperation;
    private ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse> provisionApplicationOperation;
    private ThriftOperation<SubscribeToApplicationRequest, SubscribeToApplicationResponse> subscriveToApplicationOperation;
    private ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse> registerHealthCheckOperation;
    private ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse> renewApplicationTokenOperation;
    private ThriftOperation<RegenerateApplicationTokenRequest, RegenerateApplicationTokenResponse> renerateApplicationTokenOperation;
    private ThriftOperation<SearchForApplicationsRequest, SearchForApplicationsResponse> searchForApplicationsOperation;
    private ThriftOperation<SaveChannelRequest, SaveChannelResponse> saveChannelOperation;
    private ThriftOperation<RemoveSavedChannelRequest, RemoveSavedChannelResponse> removeSavedChannelOperation;
    private ThriftOperation<SnoozeChannelRequest, SnoozeChannelResponse> snoozeChannelOperation;
    private ThriftOperation<GetApplicationSubscribersRequest, GetApplicationSubscribersResponse> getApplicationSubscribersOperation;
    private ThriftOperation<GetMyApplicationsRequest, GetMyApplicationsResponse> getMyApplicationsOperation;
    private ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse> getMySavedChannelsOperation;
    private ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse> getApplicationInfoOperation;
    private ThriftOperation<GetDashboardRequest, GetDashboardResponse> getDashboardOperation;

    @Inject
    BananaServiceImpl(ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation,
                      ThriftOperation<SignInRequest, SignInResponse> signInOperation,
                      ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse> provisionApplicationOperation,
                      ThriftOperation<GetMyApplicationsRequest, GetMyApplicationsResponse> getMyApplicationsOperation,
                      ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse> getMySavedChannelsOperation,
                      ThriftOperation<GetDashboardRequest, GetDashboardResponse> getDashboardOperation,
                      ThriftOperation<GetApplicationSubscribersRequest,GetApplicationSubscribersResponse> getApplicationSubscribersResponse)
    {
        checkThat(sendMessageOperation,
                  signInOperation,
                  provisionApplicationOperation,
                  getMyApplicationsOperation,
                  getMySavedChannelsOperation,
                  getDashboardOperation,
                  getApplicationSubscribersResponse)
            .are(notNull());
        
        this.sendMessageOperation = sendMessageOperation;
        this.signInOperation = signInOperation;
        this.provisionApplicationOperation = provisionApplicationOperation;
        this.getMyApplicationsOperation = getMyApplicationsOperation;
        this.getMySavedChannelsOperation = getMySavedChannelsOperation;
        this.getDashboardOperation = getDashboardOperation;
        this.getApplicationSubscribersOperation = getApplicationSubscribersResponse;
    }

    
    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException,
                                                                              TException
    {
        checkNotNull(request);
        
        LOG.debug("Received request to send a message: {}", request);
        
        return sendMessageOperation.process(request);
    }
    
    @Override
    public void sendMessageAsync(SendMessageRequest request) throws TException
    {
        checkNotNull(request);
        
        LOG.debug("Received request to send async message: {}", request);
        
        throw new OperationFailedException("Not Yet Implemented");
    }
    
    @Override
    public SignInResponse signIn(SignInRequest request) throws OperationFailedException,
                                                               InvalidArgumentException,
                                                               InvalidCredentialsException,
                                                               TException
    {
        checkNotNull(request);
        
        LOG.debug("Received request to Sign In: {}", request);
        
        return signInOperation.process(request);
    }
    
    @Override
    public SignUpResponse signUp(SignUpRequest request) throws OperationFailedException,
                                                               InvalidArgumentException,
                                                               InvalidCredentialsException,
                                                               AccountAlreadyExistsException,
                                                               TException
    {
        checkNotNull(request);
        
        LOG.info("Received request to Sign Up: {}", request);
        
        throw new OperationFailedException("Not Yet Implemented");
    }
    
    @Override
    public ProvisionApplicationResponse provisionApplication(ProvisionApplicationRequest request) throws OperationFailedException,
                                                                                                         InvalidArgumentException,
                                                                                                         InvalidCredentialsException,
                                                                                                         ApplicationDoesNotExistException,
                                                                                                         TException
    {
        checkNotNull(request);
        
        LOG.debug("Received request to Provision a new Application: {}", request);
        
        return provisionApplicationOperation.process(request);
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
        
        throw new OperationFailedException("Not Yet Implemented");
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
        
        throw new OperationFailedException("Not Yet Implemented");
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
        
        throw new OperationFailedException("Not Yet Implemented");
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
        
        throw new OperationFailedException("Not Yet Implemented");
    }
    
    @Override
    public SearchForApplicationsResponse searchForApplications(SearchForApplicationsRequest request) throws OperationFailedException,
                                                                                                            InvalidArgumentException,
                                                                                                            InvalidCredentialsException,
                                                                                                            UnauthorizedException,
                                                                                                            TException
    {
        checkNotNull(request);
        
        throw new OperationFailedException("Not Yet Implemented");
    }
    
    @Override
    public SaveChannelResponse saveChannel(SaveChannelRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException,
                                                                              UnauthorizedException,
                                                                              TException
    {
        checkNotNull(request);
        
        throw new OperationFailedException("Not Yet Implemented");
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
        
        throw new OperationFailedException("Not Yet Implemented");
    }
    
    @Override
    public SnoozeChannelResponse snoozeChannel(SnoozeChannelRequest request) throws OperationFailedException,
                                                                                    InvalidArgumentException,
                                                                                    InvalidCredentialsException,
                                                                                    UnauthorizedException,
                                                                                    ChannelDoesNotExistException, TException
    {
        checkNotNull(request);
        
        throw new OperationFailedException("Not Yet Implemented");
    }
    
    @Override
    public GetApplicationSubscribersResponse getApplicationSubscribers(GetApplicationSubscribersRequest request) throws OperationFailedException,
                                                                                                                        InvalidArgumentException,
                                                                                                                        InvalidCredentialsException,
                                                                                                                        UnauthorizedException,
                                                                                                                        TException
    {
        checkThat(request)
            .throwing(withMessage("request missing"))
            .is(notNull());
        
        return getApplicationSubscribersOperation.process(request);
    }
    
    @Override
    public GetMySavedChannelsResponse getMySavedChannels(GetMySavedChannelsRequest request) throws OperationFailedException,
                                                                                                   InvalidArgumentException,
                                                                                                   InvalidCredentialsException,
                                                                                                   TException
    {
        checkNotNull(request);
        
        LOG.debug("Receive request to GetMySavedChannels: {}", request);
        
        return getMySavedChannelsOperation.process(request);
    }
    
    @Override
    public GetMyApplicationsResponse getMyApplications(GetMyApplicationsRequest request) throws OperationFailedException,
                                                                                                InvalidArgumentException,
                                                                                                InvalidCredentialsException,
                                                                                                TException
    {
        checkNotNull(request);
        
        LOG.debug("Received request to getMyApplications() {}", request);
        
        return getMyApplicationsOperation.process(request);
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
        
        throw new OperationFailedException("Not Yet Implemented");
    }
    
    @Override
    public GetDashboardResponse getDashboard(GetDashboardRequest request) throws OperationFailedException,
                                                                                 InvalidArgumentException,
                                                                                 InvalidCredentialsException,
                                                                                 TException
    {
        checkNotNull(request);
        
        LOG.debug("Received request to GetDashboard: {}", request);
        
        return getDashboardOperation.process(request);
    }
    
    
    @Override
    public double getApiVersion() throws TException
    {
        return BananaServiceConstants.API_VERSION;
    }
}
