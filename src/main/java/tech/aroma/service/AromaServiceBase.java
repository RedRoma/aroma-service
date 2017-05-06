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
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.AromaConstants;
import tech.aroma.thrift.exceptions.AccountAlreadyExistsException;
import tech.aroma.thrift.exceptions.ApplicationAlreadyRegisteredException;
import tech.aroma.thrift.exceptions.ApplicationDoesNotExistException;
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
import tech.aroma.thrift.service.CheckIfDeviceIsRegisteredRequest;
import tech.aroma.thrift.service.CheckIfDeviceIsRegisteredResponse;
import tech.aroma.thrift.service.DeleteApplicationRequest;
import tech.aroma.thrift.service.DeleteApplicationResponse;
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
import tech.aroma.thrift.service.GetApplicationsFollowedByRequest;
import tech.aroma.thrift.service.GetApplicationsFollowedByResponse;
import tech.aroma.thrift.service.GetApplicationsOwnedByRequest;
import tech.aroma.thrift.service.GetApplicationsOwnedByResponse;
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
import tech.aroma.thrift.service.GetReactionsRequest;
import tech.aroma.thrift.service.GetReactionsResponse;
import tech.aroma.thrift.service.GetRegisteredDevicesRequest;
import tech.aroma.thrift.service.GetRegisteredDevicesResponse;
import tech.aroma.thrift.service.GetUserInfoRequest;
import tech.aroma.thrift.service.GetUserInfoResponse;
import tech.aroma.thrift.service.ProvisionApplicationRequest;
import tech.aroma.thrift.service.ProvisionApplicationResponse;
import tech.aroma.thrift.service.RecreateApplicationTokenRequest;
import tech.aroma.thrift.service.RecreateApplicationTokenResponse;
import tech.aroma.thrift.service.RegisterDeviceRequest;
import tech.aroma.thrift.service.RegisterDeviceResponse;
import tech.aroma.thrift.service.RegisterHealthCheckRequest;
import tech.aroma.thrift.service.RegisterHealthCheckResponse;
import tech.aroma.thrift.service.RenewApplicationTokenRequest;
import tech.aroma.thrift.service.RenewApplicationTokenResponse;
import tech.aroma.thrift.service.SearchForApplicationsRequest;
import tech.aroma.thrift.service.SearchForApplicationsResponse;
import tech.aroma.thrift.service.SignInRequest;
import tech.aroma.thrift.service.SignInResponse;
import tech.aroma.thrift.service.SignUpRequest;
import tech.aroma.thrift.service.SignUpResponse;
import tech.aroma.thrift.service.UnfollowApplicationRequest;
import tech.aroma.thrift.service.UnfollowApplicationResponse;
import tech.aroma.thrift.service.UnregisterDeviceRequest;
import tech.aroma.thrift.service.UnregisterDeviceResponse;
import tech.aroma.thrift.service.UpdateApplicationRequest;
import tech.aroma.thrift.service.UpdateApplicationResponse;
import tech.aroma.thrift.service.UpdateReactionsRequest;
import tech.aroma.thrift.service.UpdateReactionsResponse;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.service.AromaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern.Role.COMPONENT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This is the Top Level of the Aroma Service. All of the Operations arrive here and routed to their respective
 * {@linkplain ThriftOperation Operation}.
 *
 * @author SirWellington
 */
@DecoratorPattern(role = COMPONENT)
@Internal
final class AromaServiceBase implements AromaService.Iface
{

    private final static Logger LOG = LoggerFactory.getLogger(AromaServiceBase.class);

    //------------------------------
    //Authentication Operations
    private final ThriftOperation<SignInRequest, SignInResponse> signInOperation;
    private final ThriftOperation<SignUpRequest, SignUpResponse> signUpOperation;

    //------------------------------
    //User Profile Operations
    private final ThriftOperation<GetUserInfoRequest, GetUserInfoResponse> getUserInfoOperation;

    //------------------------------
    //Inbox Operations
    private final ThriftOperation<GetInboxRequest, GetInboxResponse> getInboxOperation;

    //------------------------------
    //Message Operations
    private final ThriftOperation<DeleteMessageRequest, DeleteMessageResponse> deleteMessageOperation;
    private final ThriftOperation<DismissMessageRequest, DismissMessageResponse> dismissMessageOperation;
    private final ThriftOperation<GetApplicationMessagesRequest, GetApplicationMessagesResponse> getApplicationMessagesOperation;
    private final ThriftOperation<GetFullMessageRequest, GetFullMessageResponse> getFullMessageOperation;

    //------------------------------
    //Application Operations
    private final ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse> getApplicationInfoOperation;
    private final ThriftOperation<GetApplicationsOwnedByRequest, GetApplicationsOwnedByResponse> getApplicationsOwnedByOperation;

    private final ThriftOperation<DeleteApplicationRequest, DeleteApplicationResponse> deleteApplicationOperation;

    //------------------------------
    //Reaction Operations
    private final ThriftOperation<UpdateReactionsRequest, UpdateReactionsResponse> updateReactionsOperation;
    private final ThriftOperation<GetReactionsRequest, GetReactionsResponse> getReactionsOperation;

    //------------------------------
    //App Follow Operations
    private final ThriftOperation<GetApplicationsFollowedByRequest, GetApplicationsFollowedByResponse> getApplicationsFollowedByOperation;
    private final ThriftOperation<FollowApplicationRequest, FollowApplicationResponse> followApplicationOperation;
    private final ThriftOperation<UnfollowApplicationRequest, UnfollowApplicationResponse> unfollowApplicationOperation;

    //------------------------------
    //Action and Save Operations
    private final ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse> provisionApplicationOperation;
    private final ThriftOperation<RecreateApplicationTokenRequest, RecreateApplicationTokenResponse> recreateApplicationTokenOperation;
    private final ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse> registerHealthCheckOperation;
    private final ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse> renewApplicationTokenOperation;
    private final ThriftOperation<SearchForApplicationsRequest, SearchForApplicationsResponse> searchForApplicationsOperation;
    private final ThriftOperation<UpdateApplicationRequest, UpdateApplicationResponse> updateApplicationOperation;

    //------------------------------
    //Device Registration Operations
    private final ThriftOperation<CheckIfDeviceIsRegisteredRequest, CheckIfDeviceIsRegisteredResponse> checkIfDeviceIsRegisteredOperation;
    private final ThriftOperation<GetRegisteredDevicesRequest, GetRegisteredDevicesResponse> getRegisteredDevicesOperation;
    private final ThriftOperation<RegisterDeviceRequest, RegisterDeviceResponse> registerDeviceOperation;
    private final ThriftOperation<UnregisterDeviceRequest, UnregisterDeviceResponse> unregisterDeviceOperation;

    //------------------------------
    //Other Operations
    private final ThriftOperation<GetActivityRequest, GetActivityResponse> getActivityOperation;
    private final ThriftOperation<GetBuzzRequest, GetBuzzResponse> getBuzzOperation;
    private final ThriftOperation<GetDashboardRequest, GetDashboardResponse> getDashboardOperation;
    private final ThriftOperation<GetMediaRequest, GetMediaResponse> getMediaOperation;

    @Inject
    AromaServiceBase(ThriftOperation<CheckIfDeviceIsRegisteredRequest, CheckIfDeviceIsRegisteredResponse> checkIfDeviceIsRegisteredOperation,
                     ThriftOperation<DeleteApplicationRequest, DeleteApplicationResponse> deleteApplicationOperation,
                     ThriftOperation<DeleteMessageRequest, DeleteMessageResponse> deleteMessageOperation,
                     ThriftOperation<DismissMessageRequest, DismissMessageResponse> dismissMessageOperation,
                     ThriftOperation<FollowApplicationRequest, FollowApplicationResponse> followApplicationOperation,
                     ThriftOperation<GetActivityRequest, GetActivityResponse> getActivityOperation,
                     ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse> getApplicationInfoOperation,
                     ThriftOperation<GetApplicationMessagesRequest, GetApplicationMessagesResponse> getApplicationMessagesOperation,
                     ThriftOperation<GetApplicationsFollowedByRequest, GetApplicationsFollowedByResponse> getApplicationsFollowedByOperation,
                     ThriftOperation<GetApplicationsOwnedByRequest, GetApplicationsOwnedByResponse> getApplicationsOwnedByOperation,
                     ThriftOperation<GetBuzzRequest, GetBuzzResponse> getBuzzOperation,
                     ThriftOperation<GetDashboardRequest, GetDashboardResponse> getDashboardOperation,
                     ThriftOperation<GetFullMessageRequest, GetFullMessageResponse> getFullMessageOperation,
                     ThriftOperation<GetInboxRequest, GetInboxResponse> getInboxOperation,
                     ThriftOperation<GetMediaRequest, GetMediaResponse> getMediaOperation,
                     ThriftOperation<GetReactionsRequest, GetReactionsResponse> getReactionsOperation,
                     ThriftOperation<GetRegisteredDevicesRequest, GetRegisteredDevicesResponse> getRegisteredDevicesOperation,
                     ThriftOperation<GetUserInfoRequest, GetUserInfoResponse> getUserInfoOperation,
                     ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse> provisionApplicationOperation,
                     ThriftOperation<RecreateApplicationTokenRequest, RecreateApplicationTokenResponse> recreateApplicationTokenOperation,
                     ThriftOperation<RegisterDeviceRequest, RegisterDeviceResponse> registerDeviceOperation,
                     ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse> registerHealthCheckOperation,
                     ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse> renewApplicationTokenOperation,
                     ThriftOperation<SearchForApplicationsRequest, SearchForApplicationsResponse> searchForApplicationsOperation,
                     ThriftOperation<SignInRequest, SignInResponse> signInOperation,
                     ThriftOperation<SignUpRequest, SignUpResponse> signUpOperation,
                     ThriftOperation<UnfollowApplicationRequest, UnfollowApplicationResponse> unfollowApplicationOperation,
                     ThriftOperation<UnregisterDeviceRequest, UnregisterDeviceResponse> unregisterDeviceOperation,
                     ThriftOperation<UpdateApplicationRequest, UpdateApplicationResponse> updateApplicationOperation,
                     ThriftOperation<UpdateReactionsRequest, UpdateReactionsResponse> updateReactionsOperation)
    {
        checkThat(checkIfDeviceIsRegisteredOperation,
                  deleteApplicationOperation,
                  dismissMessageOperation,
                  followApplicationOperation,
                  getActivityOperation,
                  getApplicationInfoOperation,
                  getApplicationMessagesOperation,
                  getApplicationsFollowedByOperation,
                  getApplicationsOwnedByOperation,
                  getBuzzOperation,
                  getDashboardOperation,
                  getFullMessageOperation,
                  getInboxOperation,
                  getMediaOperation,
                  getReactionsOperation,
                  getRegisteredDevicesOperation,
                  getUserInfoOperation,
                  provisionApplicationOperation,
                  recreateApplicationTokenOperation,
                  registerDeviceOperation,
                  registerHealthCheckOperation,
                  renewApplicationTokenOperation,
                  searchForApplicationsOperation,
                  signInOperation,
                  signUpOperation,
                  unfollowApplicationOperation,
                  unregisterDeviceOperation,
                  updateApplicationOperation,
                  updateReactionsOperation,
                  deleteMessageOperation)
            .are(notNull());

        this.checkIfDeviceIsRegisteredOperation = checkIfDeviceIsRegisteredOperation;
        this.deleteApplicationOperation = deleteApplicationOperation;
        this.deleteMessageOperation = deleteMessageOperation;
        this.dismissMessageOperation = dismissMessageOperation;
        this.followApplicationOperation = followApplicationOperation;
        this.getActivityOperation = getActivityOperation;
        this.getApplicationInfoOperation = getApplicationInfoOperation;
        this.getApplicationMessagesOperation = getApplicationMessagesOperation;
        this.getApplicationsFollowedByOperation = getApplicationsFollowedByOperation;
        this.getApplicationsOwnedByOperation = getApplicationsOwnedByOperation;
        this.getBuzzOperation = getBuzzOperation;
        this.getDashboardOperation = getDashboardOperation;
        this.getFullMessageOperation = getFullMessageOperation;
        this.getInboxOperation = getInboxOperation;
        this.getMediaOperation = getMediaOperation;
        this.getReactionsOperation = getReactionsOperation;
        this.getRegisteredDevicesOperation = getRegisteredDevicesOperation;
        this.getUserInfoOperation = getUserInfoOperation;
        this.provisionApplicationOperation = provisionApplicationOperation;
        this.recreateApplicationTokenOperation = recreateApplicationTokenOperation;
        this.registerDeviceOperation = registerDeviceOperation;
        this.registerHealthCheckOperation = registerHealthCheckOperation;
        this.renewApplicationTokenOperation = renewApplicationTokenOperation;
        this.searchForApplicationsOperation = searchForApplicationsOperation;
        this.signInOperation = signInOperation;
        this.signUpOperation = signUpOperation;
        this.unfollowApplicationOperation = unfollowApplicationOperation;
        this.unregisterDeviceOperation = unregisterDeviceOperation;
        this.updateApplicationOperation = updateApplicationOperation;
        this.updateReactionsOperation = updateReactionsOperation;
    }
    
    
    
    
    
    @Override
    public double getApiVersion() throws TException
    {
        return AromaConstants.API_VERSION;
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

        LOG.info("Received request to delete application {}", request);

        return deleteApplicationOperation.process(request);
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
        
        LOG.info("Received request to delete message {}", request);
        
        return deleteMessageOperation.process(request);
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
        
        LOG.info("Received request to dismiss message {}", request);
        
        return dismissMessageOperation.process(request);
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
        
        LOG.info("Received request to follow Application {}", request);
        
        return followApplicationOperation.process(request);
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
    public RecreateApplicationTokenResponse recreateToken(RecreateApplicationTokenRequest request) throws OperationFailedException,
                                                                                                                InvalidArgumentException,
                                                                                                                InvalidCredentialsException,
                                                                                                                ApplicationDoesNotExistException,
                                                                                                                UnauthorizedException,
                                                                                                                TException
    {
        checkNotNull(request);

        LOG.info("Received request to recreate an Application Token {}", request);

        return recreateApplicationTokenOperation.process(request);
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
    public RenewApplicationTokenResponse renewApplicationToken(RenewApplicationTokenRequest request) throws OperationFailedException,
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
    public SignInResponse signIn(SignInRequest request) throws OperationFailedException,
                                                               InvalidArgumentException,
                                                               InvalidCredentialsException,
                                                               UserDoesNotExistException,
                                                               TException
    {
        checkNotNull(request);
        ensureEmailIsLowerCased(request);
        
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
        ensureEmailIsLowerCased(request);
        
        LOG.info("Received request to Sign Up: {}", request);

        return signUpOperation.process(request);
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
        
        LOG.info("Received request to update Application: {}", request);
        
        return updateApplicationOperation.process(request);
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
        
        LOG.info("Received request to Unfollow Application: {}", request);
        
        return unfollowApplicationOperation.process(request);
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
    public GetApplicationMessagesResponse getApplicationMessages(GetApplicationMessagesRequest request) throws OperationFailedException,
                                                                                                               InvalidArgumentException,
                                                                                                               InvalidTokenException,
                                                                                                               UnauthorizedException,
                                                                                                               TException
    {
        checkNotNull(request);
        
        LOG.info("Received request to Get Application Messages: {}", request);
        
        return getApplicationMessagesOperation.process(request);
    }
    

    @Override
    public GetInboxResponse getInbox(GetInboxRequest request) throws OperationFailedException,
                                                                     InvalidArgumentException,
                                                                     InvalidCredentialsException,
                                                                     TException
    {
        checkNotNull(request);

        LOG.info("Received request to Get Inbox: {}", request);
        
        return getInboxOperation.process(request);
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
        
        LOG.info("Received request to Get Media: {}", request);
        
        return getMediaOperation.process(request);
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
    public GetApplicationsFollowedByResponse getApplicationsFollowedBy(GetApplicationsFollowedByRequest request) throws OperationFailedException,
                                                                                                                        InvalidArgumentException,
                                                                                                                        InvalidTokenException,
                                                                                                                        TException
    {
        checkNotNull(request);
        
        LOG.info("Received request to get Applications Followed By: {}", request);
        
        return getApplicationsFollowedByOperation.process(request);
    }

    @Override
    public GetApplicationsOwnedByResponse getApplicationsOwnedBy(GetApplicationsOwnedByRequest request) throws OperationFailedException,
                                                                                                InvalidArgumentException,
                                                                                                InvalidCredentialsException,
                                                                                                TException
    {
        checkNotNull(request);

        LOG.info("Received request to Get My Applications: {}", request);

        return getApplicationsOwnedByOperation.process(request);
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
        ensureEmailIsLowerCasedIfPresent(request);
        
        LOG.info("Received request to get User Info: {}", request);
        
        return getUserInfoOperation.process(request);
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
        
        LOG.info("Received request to update Reactions: {}", request);
        
        return updateReactionsOperation.process(request);
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
        
        LOG.info("Received request to get Reactions: {}", request);
        
        return getReactionsOperation.process(request);
    }
    
   

    //==========================================================
    // DEVICE OPERATIONS
    //==========================================================

    @Override
    public CheckIfDeviceIsRegisteredResponse checkIfDeviceIsRegistered(CheckIfDeviceIsRegisteredRequest request) throws OperationFailedException,
                                                                                                                        InvalidArgumentException,
                                                                                                                        InvalidTokenException,
                                                                                                                        UnauthorizedException,
                                                                                                                        TException
    {
        checkNotNull(request);
        
        LOG.info("Received request to check if device is registered: {}", request.device);
        
        return checkIfDeviceIsRegisteredOperation.process(request);
    }

    @Override
    public GetRegisteredDevicesResponse getRegisteredDevices(GetRegisteredDevicesRequest request) throws OperationFailedException,
                                                                                                         InvalidArgumentException,
                                                                                                         InvalidTokenException,
                                                                                                         UnauthorizedException,
                                                                                                         TException
    {
        checkNotNull(request);
        
        LOG.info("Received request to GetRegisteredDevices by {}", request.token.userId);
        
        return getRegisteredDevicesOperation.process(request);
    }

    @Override
    public RegisterDeviceResponse registerDevice(RegisterDeviceRequest request) throws OperationFailedException,
                                                                                       InvalidArgumentException,
                                                                                       InvalidTokenException,
                                                                                       UnauthorizedException,
                                                                                       TException
    {
        checkNotNull(request);
        
        LOG.info("Received request to register a device: {}", request.device);
        
        return registerDeviceOperation.process(request);
    }

    @Override
    public UnregisterDeviceResponse unregisterDevice(UnregisterDeviceRequest request) throws OperationFailedException,
                                                                                             InvalidArgumentException,
                                                                                             InvalidTokenException,
                                                                                             UnauthorizedException, 
                                                                                             TException
    {
        checkNotNull(request);
        
        LOG.info("Received a request to unregister a device: {}", request.device);
        
        return unregisterDeviceOperation.process(request);
    }
    

    //==========================================================
    // INTERNAL OPERATIONS
    //==========================================================
    private void ensureEmailIsLowerCased(SignInRequest request)
    {
        if (request.isSetEmailAddress())
        {
            request.setEmailAddress(request.emailAddress.toLowerCase());
        }
    }

    private void ensureEmailIsLowerCased(SignUpRequest request)
    {
        if (request.isSetEmail())
        {
            request.setEmail(request.email.toLowerCase());
        }
    }

    private void ensureEmailIsLowerCasedIfPresent(GetUserInfoRequest request)
    {
        if (request.isSetEmail())
        {
            request.setEmail(request.email.toLowerCase());
        }
    }
}
