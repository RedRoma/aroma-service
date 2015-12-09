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

import java.util.concurrent.ExecutorService;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.exceptions.ChannelDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.exceptions.ServiceAlreadyRegisteredException;
import tech.aroma.banana.thrift.exceptions.ServiceDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.UnauthorizedException;
import tech.aroma.banana.thrift.service.BananaService;
import tech.aroma.banana.thrift.service.GetMySavedChannelsRequest;
import tech.aroma.banana.thrift.service.GetMySavedChannelsResponse;
import tech.aroma.banana.thrift.service.GetServiceInfoRequest;
import tech.aroma.banana.thrift.service.GetServiceInfoResponse;
import tech.aroma.banana.thrift.service.GetServiceSubscribersRequest;
import tech.aroma.banana.thrift.service.GetServiceSubscribersResponse;
import tech.aroma.banana.thrift.service.ProvisionServiceRequest;
import tech.aroma.banana.thrift.service.ProvisionServiceResponse;
import tech.aroma.banana.thrift.service.RegenerateTokenRequest;
import tech.aroma.banana.thrift.service.RegenerateTokenResponse;
import tech.aroma.banana.thrift.service.RegisterHealthCheckRequest;
import tech.aroma.banana.thrift.service.RegisterHealthCheckResponse;
import tech.aroma.banana.thrift.service.RemoveSavedChannelRequest;
import tech.aroma.banana.thrift.service.RemoveSavedChannelResponse;
import tech.aroma.banana.thrift.service.RenewServiceTokenRequest;
import tech.aroma.banana.thrift.service.RenewServiceTokenResponse;
import tech.aroma.banana.thrift.service.SaveChannelRequest;
import tech.aroma.banana.thrift.service.SaveChannelResponse;
import tech.aroma.banana.thrift.service.SearchForServicesRequest;
import tech.aroma.banana.thrift.service.SearchForServicesResponse;
import tech.aroma.banana.thrift.service.SendMessageRequest;
import tech.aroma.banana.thrift.service.SendMessageResponse;
import tech.aroma.banana.thrift.service.SignInRequest;
import tech.aroma.banana.thrift.service.SignInResponse;
import tech.aroma.banana.thrift.service.SnoozeChannelRequest;
import tech.aroma.banana.thrift.service.SnoozeChannelResponse;
import tech.aroma.banana.thrift.service.SubscribeToServiceRequest;
import tech.aroma.banana.thrift.service.SubscribeToServiceResponse;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
final class BananaServiceImpl implements BananaService.Iface
{
    
    private final static Logger LOG = LoggerFactory.getLogger(BananaServiceImpl.class);
    
    private ExecutorService executor;
    
    private ThriftOperation<SignInRequest, SignInResponse> signInOperation;
    private ThriftOperation<ProvisionServiceRequest, ProvisionServiceResponse> provisionServiceOperation;
    private ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse> getMySavedChannelsOperation;
    private ThriftOperation<GetServiceInfoRequest, GetServiceInfoResponse> getServiceInfoOperation;
    private ThriftOperation<RegenerateTokenRequest, RegenerateTokenResponse> regerateTokenOperation;
    private ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse> registerHealthCheckOperation;
    private ThriftOperation<RemoveSavedChannelRequest, RemoveSavedChannelResponse> removeSavedChannelOperation;
    private ThriftOperation<RenewServiceTokenRequest, RenewServiceTokenResponse> renewServiceTokenOperation;
    private ThriftOperation<SaveChannelRequest, SaveChannelResponse> saveChannelOperation;
    private ThriftOperation<SearchForServicesRequest, SearchForServicesResponse> searchForServicesOperation;
    private ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;
    private ThriftOperation<SnoozeChannelRequest, SnoozeChannelResponse> snoozeChannelOperation;
    private ThriftOperation<SubscribeToServiceRequest, SubscribeToServiceResponse> subscribeToChannelOperation;

    @Inject
    BananaServiceImpl(ExecutorService executor, 
                      ThriftOperation<SignInRequest, SignInResponse> signInOperation, 
                      ThriftOperation<ProvisionServiceRequest, ProvisionServiceResponse> provisionServiceOperation, 
                      ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse> getMySavedChannelsOperation, 
                      ThriftOperation<GetServiceInfoRequest, GetServiceInfoResponse> getServiceInfoOperation, 
                      ThriftOperation<RegenerateTokenRequest, RegenerateTokenResponse> regerateTokenOperation, 
                      ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse> registerHealthCheckOperation,
                      ThriftOperation<RemoveSavedChannelRequest, RemoveSavedChannelResponse> removeSavedChannelOperation,
                      ThriftOperation<RenewServiceTokenRequest, RenewServiceTokenResponse> renewServiceTokenOperation, 
                      ThriftOperation<SaveChannelRequest, SaveChannelResponse> saveChannelOperation,
                      ThriftOperation<SearchForServicesRequest, SearchForServicesResponse> searchForServicesOperation,
                      ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation,
                      ThriftOperation<SnoozeChannelRequest, SnoozeChannelResponse> snoozeChannelOperation,
                      ThriftOperation<SubscribeToServiceRequest, SubscribeToServiceResponse> subscribeToChannelOperation)
    {
        this.executor = executor;
        this.signInOperation = signInOperation;
        this.provisionServiceOperation = provisionServiceOperation;
        this.getMySavedChannelsOperation = getMySavedChannelsOperation;
        this.getServiceInfoOperation = getServiceInfoOperation;
        this.regerateTokenOperation = regerateTokenOperation;
        this.registerHealthCheckOperation = registerHealthCheckOperation;
        this.removeSavedChannelOperation = removeSavedChannelOperation;
        this.renewServiceTokenOperation = renewServiceTokenOperation;
        this.saveChannelOperation = saveChannelOperation;
        this.searchForServicesOperation = searchForServicesOperation;
        this.sendMessageOperation = sendMessageOperation;
        this.snoozeChannelOperation = snoozeChannelOperation;
        this.subscribeToChannelOperation = subscribeToChannelOperation;
    }


    
    

    
    @Override
    public SignInResponse signIn(SignInRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, TException
    {
        checkThat(request)
            .throwing(InvalidArgumentException.class)
            .is(notNull());
        
        LOG.info("Received request to Sign In: {}", request);
        
        return signInOperation.process(request);
    }
    
    @Override
    public ProvisionServiceResponse provisionService(ProvisionServiceRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException("missing request"))
            .is(notNull());
        
        LOG.info("Received request to Provision a new Service {}", request);
        
        return provisionServiceOperation.process(request);
    }
    
    @Override
    public SubscribeToServiceResponse subscribeToService(SubscribeToServiceRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, ServiceAlreadyRegisteredException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }
    
    @Override
    public RegisterHealthCheckResponse registerHealthCheck(RegisterHealthCheckRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, UnauthorizedException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }
    
    @Override
    public RenewServiceTokenResponse renewServiceToken(RenewServiceTokenRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, UnauthorizedException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }
    
    @Override
    public RegenerateTokenResponse regenerateToken(RegenerateTokenRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, UnauthorizedException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }
    
    @Override
    public GetServiceInfoResponse getServiceInfo(GetServiceInfoRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, UnauthorizedException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }
    
    @Override
    public SearchForServicesResponse searchForServices(SearchForServicesRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, UnauthorizedException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }
    
    @Override
    public GetServiceSubscribersResponse getServiceSubscribers(GetServiceSubscribersRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, UnauthorizedException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }
    
    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException("missing request"))
            .is(notNull());
        
        return sendMessageOperation.process(request);
    }
    
    @Override
    public void sendMessageAsync(SendMessageRequest request) throws TException
    {
        executor.submit(() -> this.sendMessage(request));
    }

    @Override
    public SaveChannelResponse saveChannel(SaveChannelRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, UnauthorizedException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }

    @Override
    public RemoveSavedChannelResponse removeSavedChannel(RemoveSavedChannelRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, UnauthorizedException, ChannelDoesNotExistException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }

    @Override
    public GetMySavedChannelsResponse getMySavedChannels(GetMySavedChannelsRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, UnauthorizedException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }

    @Override
    public SnoozeChannelResponse snoozeChannel(SnoozeChannelRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, UnauthorizedException, ChannelDoesNotExistException, TException
    {
        throw new OperationFailedException("Operation Not Implemented Yet.");
    }
    
}
