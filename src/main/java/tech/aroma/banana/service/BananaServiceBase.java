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
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.aroma.banana.thrift.exceptions.MessageDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.exceptions.UnauthorizedException;
import tech.aroma.banana.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.banana.thrift.service.BananaService;
import tech.aroma.banana.thrift.service.BananaServiceConstants;
import tech.aroma.banana.thrift.service.DeleteMessageRequest;
import tech.aroma.banana.thrift.service.DeleteMessageResponse;
import tech.aroma.banana.thrift.service.DismissMessageRequest;
import tech.aroma.banana.thrift.service.DismissMessageResponse;
import tech.aroma.banana.thrift.service.FollowApplicationRequest;
import tech.aroma.banana.thrift.service.FollowApplicationResponse;
import tech.aroma.banana.thrift.service.GetActivityRequest;
import tech.aroma.banana.thrift.service.GetActivityResponse;
import tech.aroma.banana.thrift.service.GetApplicationInfoRequest;
import tech.aroma.banana.thrift.service.GetApplicationInfoResponse;
import tech.aroma.banana.thrift.service.GetBuzzRequest;
import tech.aroma.banana.thrift.service.GetBuzzResponse;
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
import tech.aroma.banana.thrift.service.GetUserInfoRequest;
import tech.aroma.banana.thrift.service.GetUserInfoResponse;
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
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.service.BananaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 * This is the Top Level of the Banana Service. All of the Operations arrive here and routed to their respective
 * {@linkplain ThriftOperation Operation}.
 *
 * @author SirWellington
 */
@Internal
final class BananaServiceBase implements BananaService.Iface
{

    private final static Logger LOG = LoggerFactory.getLogger(BananaServiceBase.class);

    //Action and Save Operations
    private final ThriftOperation<SignInRequest, SignInResponse> signInOperation;
    private final ThriftOperation<SignUpRequest, SignUpResponse> signUpOperation;
    private final ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse> provisionApplicationOperation;
    private final ThriftOperation<RegenerateApplicationTokenRequest, RegenerateApplicationTokenResponse> regenerateApplicationTokenOperation;
    private final ThriftOperation<FollowApplicationRequest, FollowApplicationResponse> followApplicationOperation;
    private final ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse> registerHealthCheckOperation;
    private final ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse> renewApplicationTokenOperation;
    private final ThriftOperation<SearchForApplicationsRequest, SearchForApplicationsResponse> searchForApplicationsOperation;
    private final ThriftOperation<SaveChannelRequest, SaveChannelResponse> saveChannelOperation;
    private final ThriftOperation<RemoveSavedChannelRequest, RemoveSavedChannelResponse> removeSavedChannelOperation;
    private final ThriftOperation<SnoozeChannelRequest, SnoozeChannelResponse> snoozeChannelOperation;
    
    //Query and GET Operations
    private final ThriftOperation<GetActivityRequest, GetActivityResponse> getActivityOperation;
    private final ThriftOperation<GetBuzzRequest, GetBuzzResponse> getBuzzOperation;
    private final ThriftOperation<GetMyApplicationsRequest, GetMyApplicationsResponse> getMyApplicationsOperation;
    private final ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse> getMySavedChannelsOperation;
    private final ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse> getApplicationInfoOperation;
    private final ThriftOperation<GetDashboardRequest, GetDashboardResponse> getDashboardOperation;
    private final ThriftOperation<GetMessagesRequest, GetMessagesResponse> getMessagesOperation;
    private final ThriftOperation<GetFullMessageRequest, GetFullMessageResponse> getFullMessageOperation;
    private final ThriftOperation<GetUserInfoRequest, GetUserInfoResponse> getUserInfoOperation;

    @Inject
    BananaServiceBase(ThriftOperation<SignInRequest, SignInResponse> signInOperation,
                      ThriftOperation<SignUpRequest, SignUpResponse> signUpOperation,
                      ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse> provisionApplicationOperation,
                      ThriftOperation<RegenerateApplicationTokenRequest, RegenerateApplicationTokenResponse> regenerateApplicationTokenOperation,
                      ThriftOperation<FollowApplicationRequest, FollowApplicationResponse> followApplicationOperation,
                      ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse> registerHealthCheckOperation,
                      ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse> renewApplicationTokenOperation,
                      ThriftOperation<SearchForApplicationsRequest, SearchForApplicationsResponse> searchForApplicationsOperation,
                      ThriftOperation<SaveChannelRequest, SaveChannelResponse> saveChannelOperation,
                      ThriftOperation<RemoveSavedChannelRequest, RemoveSavedChannelResponse> removeSavedChannelOperation,
                      ThriftOperation<SnoozeChannelRequest, SnoozeChannelResponse> snoozeChannelOperation,
                      ThriftOperation<GetActivityRequest, GetActivityResponse> getActivityOperation,
                      ThriftOperation<GetBuzzRequest, GetBuzzResponse> getBuzzOperation,
                      ThriftOperation<GetMyApplicationsRequest, GetMyApplicationsResponse> getMyApplicationsOperation,
                      ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse> getMySavedChannelsOperation,
                      ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse> getApplicationInfoOperation,
                      ThriftOperation<GetDashboardRequest, GetDashboardResponse> getDashboardOperation,
                      ThriftOperation<GetMessagesRequest, GetMessagesResponse> getMessagesOperation,
                      ThriftOperation<GetFullMessageRequest, GetFullMessageResponse> getFullMessageOperation,
                      ThriftOperation<GetUserInfoRequest, GetUserInfoResponse> getUserInfoOperation)
    {
        checkThat(signInOperation,
                  signUpOperation,
                  provisionApplicationOperation,
                  regenerateApplicationTokenOperation,
                  followApplicationOperation,
                  registerHealthCheckOperation,
                  renewApplicationTokenOperation,
                  searchForApplicationsOperation,
                  saveChannelOperation,
                  removeSavedChannelOperation,
                  snoozeChannelOperation,
                  getActivityOperation,
                  getMyApplicationsOperation,
                  getMySavedChannelsOperation,
                  getApplicationInfoOperation,
                  getBuzzOperation,
                  getDashboardOperation,
                  getMessagesOperation,
                  getFullMessageOperation,
                  getUserInfoOperation)
            .are(notNull());

        this.signInOperation = signInOperation;
        this.signUpOperation = signUpOperation;
        this.provisionApplicationOperation = provisionApplicationOperation;
        this.regenerateApplicationTokenOperation = regenerateApplicationTokenOperation;
        this.followApplicationOperation = followApplicationOperation;
        this.registerHealthCheckOperation = registerHealthCheckOperation;
        this.renewApplicationTokenOperation = renewApplicationTokenOperation;
        this.searchForApplicationsOperation = searchForApplicationsOperation;
        this.saveChannelOperation = saveChannelOperation;
        this.removeSavedChannelOperation = removeSavedChannelOperation;
        this.snoozeChannelOperation = snoozeChannelOperation;
        
        this.getActivityOperation = getActivityOperation;
        this.getBuzzOperation = getBuzzOperation;
        this.getMyApplicationsOperation = getMyApplicationsOperation;
        this.getMySavedChannelsOperation = getMySavedChannelsOperation;
        this.getApplicationInfoOperation = getApplicationInfoOperation;
        this.getDashboardOperation = getDashboardOperation;
        this.getMessagesOperation = getMessagesOperation;
        this.getFullMessageOperation = getFullMessageOperation;
        this.getUserInfoOperation = getUserInfoOperation;
    }
    
    
    
    
    
    @Override
    public double getApiVersion() throws TException
    {
        return BananaServiceConstants.API_VERSION;
    }
   
    @Override
    public DeleteMessageResponse deleteMessage(DeleteMessageRequest request) throws OperationFailedException,
                                                                                    InvalidArgumentException,
                                                                                    InvalidTokenException,
                                                                                    MessageDoesNotExistException,
                                                                                    UnauthorizedException, TException
    {
        throw new OperationFailedException("Not supported yet.");
    }

    @Override
    public DismissMessageResponse dismissMessage(DismissMessageRequest request) throws OperationFailedException,
                                                                                       InvalidArgumentException,
                                                                                       InvalidTokenException,
                                                                                       MessageDoesNotExistException,
                                                                                       UnauthorizedException, TException
    {
        throw new OperationFailedException("Not supported yet."); 
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
        throw new OperationFailedException("Not supported yet."); 
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

        LOG.info("Received request to provision Application: {}", request);

        return provisionApplicationOperation.process(request);
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

        LOG.info("Received request to regenerate an Application Token {}", request);

        return regenerateApplicationTokenOperation.process(request);
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

        LOG.info("Received request to register health check for Application: {}", request);

        return registerHealthCheckOperation.process(request);
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

        LOG.info("Receive drequest to remove a saved channel: {}", request);

        return removeSavedChannelOperation.process(request);
    }

    @Override
    public RenewApplicationTokenResponse renewApplicationToken(RenewApplicationTokenRequest request) throws
        OperationFailedException,
        InvalidArgumentException,
        InvalidCredentialsException,
        ApplicationDoesNotExistException,
        UnauthorizedException,
        TException
    {
        checkNotNull(request);

        LOG.info("Received request to renew an application token: {}", request);

        return renewApplicationTokenOperation.process(request);
    }

    @Override
    public SaveChannelResponse saveChannel(SaveChannelRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException,
                                                                              UnauthorizedException,
                                                                              TException
    {
        checkNotNull(request);

        LOG.info("Received request to Save a Channel: {}", request);

        return saveChannelOperation.process(request);
    }

    @Override
    public SignInResponse signIn(SignInRequest request) throws OperationFailedException,
                                                               InvalidArgumentException,
                                                               InvalidCredentialsException,
                                                               TException
    {
        checkNotNull(request);

        LOG.info("Received request to sign in: {}", request);

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

        return signUpOperation.process(request);
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

        LOG.info("Received request to snooze a channel: {}", request);

        return snoozeChannelOperation.process(request);
    }

    @Override
    public GetActivityResponse getActivity(GetActivityRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException,
                                                                              TException
    {
        checkNotNull(request);

        LOG.info("Received request to get Activity: {}", request);

        return getActivityOperation.process(request);
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

        LOG.info("Receive request to get Application Info: {}", request);

        return getApplicationInfoOperation.process(request);
    }

    @Override
    public GetDashboardResponse getDashboard(GetDashboardRequest request) throws OperationFailedException,
                                                                                 InvalidArgumentException,
                                                                                 InvalidCredentialsException, 
                                                                                 TException
    {
        checkNotNull(request);

        LOG.info("Received request to get Dashboard: {}", request);

        return getDashboardOperation.process(request);
    }

    @Override
    public GetMessagesResponse getMessages(GetMessagesRequest request) throws OperationFailedException,
                                                                              InvalidArgumentException,
                                                                              InvalidCredentialsException,
                                                                              TException
    {
        checkNotNull(request);

        LOG.info("Received request to Get Messages: {}", request);
        
        return getMessagesOperation.process(request);
    }

    @Override
    public GetFullMessageResponse getFullMessage(GetFullMessageRequest request) throws OperationFailedException,
                                                                                       InvalidArgumentException,
                                                                                       InvalidCredentialsException, 
                                                                                       TException
    {
        checkNotNull(request);

        LOG.info("Received request to Get Full Message: {}", request);

        
        return getFullMessageOperation.process(request);
    }

    @Override
    public GetMyApplicationsResponse getMyApplications(GetMyApplicationsRequest request) throws OperationFailedException,
                                                                                                InvalidArgumentException,
                                                                                                InvalidCredentialsException,
                                                                                                TException
    {
        checkNotNull(request);

        LOG.info("Received request to Get My Applications: {}", request);

        return getMyApplicationsOperation.process(request);
    }

    @Override
    public GetMySavedChannelsResponse getMySavedChannels(GetMySavedChannelsRequest request) throws OperationFailedException,
                                                                                                   InvalidArgumentException,
                                                                                                   InvalidCredentialsException,
                                                                                                   TException
    {
        checkNotNull(request);

        LOG.info("Received request to Get My Saved Channels: {}", request);

        return getMySavedChannelsOperation.process(request);
    }

    @Override
    public SearchForApplicationsResponse searchForApplications(SearchForApplicationsRequest request) throws OperationFailedException,
                                                                                                            InvalidArgumentException,
                                                                                                            InvalidCredentialsException,
                                                                                                            UnauthorizedException,
                                                                                                            TException
    {
        checkNotNull(request);

        LOG.info("Received request to Search for applications: {}", request);

        
        return searchForApplicationsOperation.process(request);
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
        
        LOG.info("Received request to get Buzz: {}", request);
        
        return getBuzzOperation.process(request);
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
        
        LOG.info("Received request to get User Info: {}", request);
        
        return getUserInfoOperation.process(request);
    }

}
