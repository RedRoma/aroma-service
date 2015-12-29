
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
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
        //Action and Save Operations
    @Mock
    private ThriftOperation<SignInRequest, SignInResponse> signInOperation;
    
    @Mock
    private ThriftOperation<SignUpRequest, SignUpResponse> signUpOperation;
    
    @Mock
    private ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse> provisionApplicationOperation;
    
    @Mock
    private ThriftOperation<RegenerateApplicationTokenRequest, RegenerateApplicationTokenResponse> regenerateApplicationTokenOperation;
    
    @Mock
    private ThriftOperation<SubscribeToApplicationRequest, SubscribeToApplicationResponse> subscriveToApplicationOperation;
    
    @Mock
    private ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse> registerHealthCheckOperation;
    
    @Mock
    private ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse> renewApplicationTokenOperation;
    
    @Mock
    private ThriftOperation<SearchForApplicationsRequest, SearchForApplicationsResponse> searchForApplicationsOperation;
    
    @Mock
    private ThriftOperation<SaveChannelRequest, SaveChannelResponse> saveChannelOperation;
    
    @Mock
    private ThriftOperation<RemoveSavedChannelRequest, RemoveSavedChannelResponse> removeSavedChannelOperation;
    
    @Mock
    private ThriftOperation<SnoozeChannelRequest, SnoozeChannelResponse> snoozeChannelOperation;
    
    //Query and GET Operations
    @Mock
    private ThriftOperation<GetActivityRequest, GetActivityResponse> getActivityOperation;
    
    @Mock
    private ThriftOperation<GetMyApplicationsRequest, GetMyApplicationsResponse> getMyApplicationsOperation;
    
    @Mock
    private ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse> getMySavedChannelsOperation;
    
    @Mock
    private ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse> getApplicationInfoOperation;
    
    @Mock
    private ThriftOperation<GetDashboardRequest, GetDashboardResponse> getDashboardOperation;
    
    @Mock
    private ThriftOperation<GetMessagesRequest, GetMessagesResponse> getMessagesOperation;
    
    @Mock
    private ThriftOperation<GetFullMessageRequest, GetFullMessageResponse> getFullMessageOperation;


    private BananaServiceImpl instance;

    @Before
    public void setUp()
    {
        instance = new BananaServiceImpl(signInOperation,
                                         signUpOperation,
                                         provisionApplicationOperation,
                                         regenerateApplicationTokenOperation,
                                         subscriveToApplicationOperation,
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
                                         getDashboardOperation,
                                         getMessagesOperation,
                                         getFullMessageOperation);

        verifyZeroInteractions(signInOperation,
                                         signUpOperation,
                                         provisionApplicationOperation,
                                         regenerateApplicationTokenOperation,
                                         subscriveToApplicationOperation,
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
                                         getDashboardOperation,
                                         getMessagesOperation,
                                         getFullMessageOperation);

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
        
        GetApplicationInfoRequest request = pojos(GetApplicationInfoRequest.class).get();
        GetApplicationInfoResponse expectedResponse = pojos(GetApplicationInfoResponse.class).get();
        
        when(getApplicationInfoOperation.process(request))
            .thenReturn(expectedResponse);
        
        GetApplicationInfoResponse response = instance.getApplicationInfo(request);
        assertThat(response, notNullValue());
        assertThat(response, is(expectedResponse));
        
        verify(getApplicationInfoOperation).process(request);
        
        assertThrows(() -> instance.getApplicationInfo(null))
            .isInstanceOf(InvalidArgumentException.class);
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

    @Test
    public void testGetActivity() throws Exception
    {
    }

    @Test
    public void testGetMessages() throws Exception
    {
    }

    @Test
    public void testGetFullMessage() throws Exception
    {
    }

}
