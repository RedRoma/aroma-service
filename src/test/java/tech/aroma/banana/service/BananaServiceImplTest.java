
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

import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.service.GetMySavedChannelsRequest;
import tech.aroma.banana.thrift.service.GetMySavedChannelsResponse;
import tech.aroma.banana.thrift.service.GetMyServicesRequest;
import tech.aroma.banana.thrift.service.GetMyServicesResponse;
import tech.aroma.banana.thrift.service.GetServiceInfoRequest;
import tech.aroma.banana.thrift.service.GetServiceInfoResponse;
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
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class BananaServiceImplTest
{

    private ExecutorService executor = MoreExecutors.newDirectExecutorService();

    @Mock
    private ThriftOperation<SignInRequest, SignInResponse> signInOperation;

    @Mock
    private ThriftOperation<ProvisionServiceRequest, ProvisionServiceResponse> provisionServiceOperation;

    @Mock
    private ThriftOperation<GetMyServicesRequest, GetMyServicesResponse> getMyServicesOperation;
    
    @Mock
    private ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse> getMySavedChannelsOperation;

    @Mock
    private ThriftOperation<GetServiceInfoRequest, GetServiceInfoResponse> getServiceInfoOperation;

    @Mock
    private ThriftOperation<RegenerateTokenRequest, RegenerateTokenResponse> regerateTokenOperation;

    @Mock
    private ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse> registerHealthCheckOperation;

    @Mock
    private ThriftOperation<RemoveSavedChannelRequest, RemoveSavedChannelResponse> removeSavedChannelOperation;

    @Mock
    private ThriftOperation<RenewServiceTokenRequest, RenewServiceTokenResponse> renewServiceTokenOperation;

    @Mock
    private ThriftOperation<SaveChannelRequest, SaveChannelResponse> saveChannelOperation;

    @Mock
    private ThriftOperation<SearchForServicesRequest, SearchForServicesResponse> searchForServicesOperation;

    @Mock
    private ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;

    @Mock
    private ThriftOperation<SnoozeChannelRequest, SnoozeChannelResponse> snoozeChannelOperation;

    @Mock
    private ThriftOperation<SubscribeToServiceRequest, SubscribeToServiceResponse> subscribeToChannelOperation;

    private BananaServiceImpl instance;

    @Before
    public void setUp()
    {

        instance = new BananaServiceImpl(executor,
                                         signInOperation,
                                         provisionServiceOperation,
                                         getMySavedChannelsOperation,
                                         getMyServicesOperation,
                                         sendMessageOperation);
        
        verifyZeroInteractions(signInOperation,
                               provisionServiceOperation,
                               getMySavedChannelsOperation,
                               getServiceInfoOperation,
                               regerateTokenOperation,
                               registerHealthCheckOperation,
                               removeSavedChannelOperation,
                               renewServiceTokenOperation,
                               saveChannelOperation,
                               searchForServicesOperation,
                               sendMessageOperation,
                               snoozeChannelOperation,
                               subscribeToChannelOperation);
    }

    @Test
    public void testSignIn() throws Exception
    {
        SignInRequest request = pojos(SignInRequest.class).get();
        SignInResponse expectedResponse = pojos(SignInResponse.class).get();
        when(signInOperation.process(request)).thenReturn(expectedResponse);

        SignInResponse response = instance.signIn(request);
        assertThat(response, is(expectedResponse));
        verify(signInOperation).process(request);

        //Edge cases
        assertThrows(() -> instance.signIn(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testProvisionService() throws Exception
    {
        ProvisionServiceRequest request = one(pojos(ProvisionServiceRequest.class));
        ProvisionServiceResponse expectedResponse = one(pojos(ProvisionServiceResponse.class));
        when(provisionServiceOperation.process(request)).thenReturn(expectedResponse);

        ProvisionServiceResponse response = instance.provisionService(request);
        assertThat(response, is(expectedResponse));
        verify(provisionServiceOperation).process(request);

        //Edge cases
        assertThrows(() -> instance.provisionService(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testSubscribeToService() throws Exception
    {
    }

    @Test
    public void testRegisterHealthCheck() throws Exception
    {
    }

    @Test
    public void testRenewServiceToken() throws Exception
    {
    }

    @Test
    public void testRegenerateToken() throws Exception
    {
    }

    @Test
    public void testGetServiceInfo() throws Exception
    {
    }

    @Test
    public void testSearchForServices() throws Exception
    {
    }

    @Test
    public void testGetServiceSubscribers() throws Exception
    {
    }

    @Test
    public void testSendMessage() throws Exception
    {
        SendMessageRequest request = pojos(SendMessageRequest.class).get();
        SendMessageResponse expectedResponse = pojos(SendMessageResponse.class).get();
        when(sendMessageOperation.process(request)).thenReturn(expectedResponse);

        SendMessageResponse response = instance.sendMessage(request);
        assertThat(response, is(expectedResponse));
        verify(sendMessageOperation).process(request);

        //Edge cases
        assertThrows(() -> instance.sendMessage(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testSendMessageAsync() throws Exception
    {
    }

    @Test
    public void testSaveChannel() throws Exception
    {
    }

    @Test
    public void testRemoveSavedChannel() throws Exception
    {
    }

    @Test
    public void testGetMySavedChannels() throws Exception
    {
    }

    @Test
    public void testSnoozeChannel() throws Exception
    {
    }

    @Test
    public void testSignUp() throws Exception
    {
    }

    @Test
    public void testGetMyServices() throws Exception
    {
        GetMyServicesRequest request = pojos(GetMyServicesRequest.class).get();
        GetMyServicesResponse expectedResponse = pojos(GetMyServicesResponse.class).get();
        when(getMyServicesOperation.process(request)).thenReturn(expectedResponse);
        
        GetMyServicesResponse response = instance.getMyServices(request);
        assertThat(response, is(expectedResponse));
        verify(getMyServicesOperation).process(request);
        
        //Edge cases
        assertThrows(() -> instance.getMyServices(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
