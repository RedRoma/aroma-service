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

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.exceptions.ServiceAlreadyRegisteredException;
import tech.aroma.banana.thrift.exceptions.ServiceDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.UnauthorizedException;
import tech.aroma.banana.thrift.service.BananaService;
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
import tech.aroma.banana.thrift.service.RenewServiceTokenRequest;
import tech.aroma.banana.thrift.service.RenewServiceTokenResponse;
import tech.aroma.banana.thrift.service.SearchForServicesRequest;
import tech.aroma.banana.thrift.service.SearchForServicesResponse;
import tech.aroma.banana.thrift.service.SendMessageRequest;
import tech.aroma.banana.thrift.service.SendMessageResponse;
import tech.aroma.banana.thrift.service.SignInRequest;
import tech.aroma.banana.thrift.service.SignInResponse;
import tech.aroma.banana.thrift.service.SubscribeToServiceRequest;
import tech.aroma.banana.thrift.service.SubscribeToServiceResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class BananaServiceImpl implements BananaService.Iface
{

    private final static Logger LOG = LoggerFactory.getLogger(BananaServiceImpl.class);

    private ThriftOperation<SignInRequest, SignInResponse> signInOperation;
    private ThriftOperation<ProvisionServiceRequest, ProvisionServiceResponse> provisionServiceOperation;

    BananaServiceImpl(ThriftOperation<SignInRequest, SignInResponse> signInOperation,
                      ThriftOperation<ProvisionServiceRequest, ProvisionServiceResponse> provisionServiceOperation)
    {
        this.signInOperation = signInOperation;
        this.provisionServiceOperation = provisionServiceOperation;
    }

    @Override
    public SignInResponse signIn(SignInRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, TException
    {
        checkThat(request)
            .throwing(InvalidArgumentException.class)
            .is(notNull());

        return signInOperation.process(request);
    }

    @Override
    public ProvisionServiceResponse provisionService(ProvisionServiceRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException("missing request"))
            .is(notNull());
        
        return provisionServiceOperation.process(request);
    }

    @Override
    public SubscribeToServiceResponse subscribeToService(SubscribeToServiceRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, ServiceAlreadyRegisteredException, TException
    {
        return null;
    }

    @Override
    public RegisterHealthCheckResponse registerHealthCheck(RegisterHealthCheckRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, UnauthorizedException, TException
    {
        return null;
    }

    @Override
    public RenewServiceTokenResponse renewServiceToken(RenewServiceTokenRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, UnauthorizedException, TException
    {
        return null;
    }

    @Override
    public RegenerateTokenResponse regenerateToken(RegenerateTokenRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, UnauthorizedException, TException
    {
        return null;
    }

    @Override
    public GetServiceInfoResponse getServiceInfo(GetServiceInfoRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, ServiceDoesNotExistException, UnauthorizedException, TException
    {
        return null;
    }

    @Override
    public SearchForServicesResponse searchForServices(SearchForServicesRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, UnauthorizedException, TException
    {
        return null;
    }

    @Override
    public GetServiceSubscribersResponse getServiceSubscribers(GetServiceSubscribersRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, UnauthorizedException, TException
    {
        return null;
    }

    @Override
    public SendMessageResponse sendMessage(SendMessageRequest request) throws OperationFailedException, InvalidArgumentException, InvalidCredentialsException, TException
    {
        return null;
    }

    @Override
    public void sendMessageAsync(SendMessageRequest request) throws TException
    {
    }

}
