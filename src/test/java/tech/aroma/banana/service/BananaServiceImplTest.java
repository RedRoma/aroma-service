
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
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
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
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
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class BananaServiceImplTest
{
    
    @Mock
    private ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageOperation;

    @Mock
    private ThriftOperation<SendMessageRequest, SendMessageResponse> sendMessageAsyncOperation;

    @Mock
    private ThriftOperation<SignInRequest, SignInResponse> signInOperation;

    @Mock
    private ThriftOperation<SignUpRequest, SignUpResponse> signUpOperation;

    @Mock
    private ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse> provisionApplicationOperation;

    @Mock
    private ThriftOperation<SubscribeToApplicationRequest, SubscribeToApplicationResponse> subscriveToApplicationOperation;

    @Mock
    private ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse> registerHealthCheckOperation;

    @Mock
    private ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse> renewApplicationTokenOperation;

    @Mock
    private ThriftOperation<RegenerateApplicationTokenRequest, RegenerateApplicationTokenResponse> renerateApplicationTokenOperation;

    @Mock
    private ThriftOperation<SearchForApplicationsRequest, SearchForApplicationsResponse> searchForApplicationsOperation;

    @Mock
    private ThriftOperation<SaveChannelRequest, SaveChannelResponse> saveChannelOperation;

    @Mock
    private ThriftOperation<RemoveSavedChannelRequest, RemoveSavedChannelResponse> removeSavedChannelOperation;

    @Mock
    private ThriftOperation<SnoozeChannelRequest, SnoozeChannelResponse> snoozeChannelOperation;

    @Mock
    private ThriftOperation<GetApplicationSubscribersRequest, GetApplicationSubscribersResponse> getApplicationSubscribersOperation;
    @GeneratePojo
    private GetApplicationSubscribersRequest getApplicationSubscribersRequest;
    @GeneratePojo
    private GetApplicationSubscribersResponse getApplicationSubscribersResponse;
    
    @Mock
    private ThriftOperation<GetMyApplicationsRequest, GetMyApplicationsResponse> getMyApplicationsOperation;
    
    @Mock
    private ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse> getMySavedChannelsOperation;

    @Mock
    private ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse> getApplicationInfoOperation;

    @Mock
    private ThriftOperation<GetDashboardRequest, GetDashboardResponse> getDashboardOperation;


    private BananaServiceImpl instance;

    @Before
    public void setUp()
    {
        instance = new BananaServiceImpl(sendMessageOperation,
                                         signInOperation,
                                         provisionApplicationOperation,
                                         getMyApplicationsOperation,
                                         getMySavedChannelsOperation,
                                         getDashboardOperation,
                                         getApplicationSubscribersOperation);

        verifyZeroInteractions(sendMessageOperation,
                               signInOperation,
                               provisionApplicationOperation,
                               getMyApplicationsOperation,
                               getMySavedChannelsOperation,
                               getDashboardOperation,
                               getApplicationSubscribersOperation);

    }

    @Test
    public void testGetDashboard() throws Exception
    {
        GetDashboardRequest request = one(pojos(GetDashboardRequest.class));
        GetDashboardResponse expectedResponse = one(pojos(GetDashboardResponse.class));
        when(getDashboardOperation.process(request)).thenReturn(expectedResponse);

        GetDashboardResponse response = instance.getDashboard(request);
        assertThat(response, is(expectedResponse));
        verify(getDashboardOperation).process(request);

        //Edge cases
        assertThrows(() -> instance.getDashboard(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetMySavedChannels() throws Exception
    {
        GetMySavedChannelsRequest request = one(pojos(GetMySavedChannelsRequest.class));
        GetMySavedChannelsResponse expectedResponse = one(pojos(GetMySavedChannelsResponse.class));

        when(getMySavedChannelsOperation.process(request)).thenReturn(expectedResponse);

        GetMySavedChannelsResponse response = instance.getMySavedChannels(request);
        assertThat(response, is(expectedResponse));
        verify(getMySavedChannelsOperation).process(request);

        //Edge Cases
        assertThrows(() -> instance.getMySavedChannels(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetMyApplications() throws Exception
    {
        GetMyApplicationsRequest request = pojos(GetMyApplicationsRequest.class).get();
        GetMyApplicationsResponse expectedResponse = pojos(GetMyApplicationsResponse.class).get();
        when(getMyApplicationsOperation.process(request)).thenReturn(expectedResponse);

        GetMyApplicationsResponse response = instance.getMyApplications(request);
        assertThat(response, is(expectedResponse));
        verify(getMyApplicationsOperation).process(request);

        //Edge cases
        assertThrows(() -> instance.getMyApplications(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetApplicationInfo() throws Exception
    {
    }

    @Test
    public void testGetApplicationSubscribers() throws Exception
    {
        when(getApplicationSubscribersOperation.process(getApplicationSubscribersRequest))
            .thenReturn(getApplicationSubscribersResponse);
        
        GetApplicationSubscribersResponse response = instance.getApplicationSubscribers(getApplicationSubscribersRequest);
        assertThat(response, is(getApplicationSubscribersResponse));
        verify(getApplicationSubscribersOperation).process(getApplicationSubscribersRequest);
        
    }

    @Test
    public void testProvisionApplication() throws Exception
    {
        ProvisionApplicationRequest request = one(pojos(ProvisionApplicationRequest.class));
        ProvisionApplicationResponse expectedResponse = one(pojos(ProvisionApplicationResponse.class));
        when(provisionApplicationOperation.process(request)).thenReturn(expectedResponse);

        ProvisionApplicationResponse response = instance.provisionApplication(request);
        assertThat(response, is(expectedResponse));
        verify(provisionApplicationOperation).process(request);

        //Edge cases
        assertThrows(() -> instance.provisionApplication(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testRenerateApplicationToken() throws Exception
    {
    }

    @Test
    public void testRegisterHealthCheck() throws Exception
    {
    }

    @Test
    public void testRemoveSavedChannel() throws Exception
    {
    }

    @Test
    public void testRenewApplicationToken() throws Exception
    {
    }

    @Test
    public void testSaveChannel() throws Exception
    {
    }

    @Test
    public void testSearchForApplications() throws Exception
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
    public void testSignUp() throws Exception
    {
    }

    @Test
    public void testSnoozeChannel() throws Exception
    {
    }

    @Test
    public void testSubscribeToApplication() throws Exception
    {
    }

    @Test
    public void testRegenerateToken() throws Exception
    {
    }

    @Test
    public void testGetApiVersion() throws Exception
    {
        double apiVersion = instance.getApiVersion();
        assertThat(apiVersion, is(BananaServiceConstants.API_VERSION));
    }

}
