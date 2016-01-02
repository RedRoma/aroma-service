
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
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.service.BananaServiceConstants;
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
import tech.aroma.banana.thrift.service.SubscribeToApplicationRequest;
import tech.aroma.banana.thrift.service.SubscribeToApplicationResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
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
    private ThriftOperation<GetBuzzRequest, GetBuzzResponse> getBuzzOperation;
    
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
    
    @Mock
    private ThriftOperation<GetUserInfoRequest, GetUserInfoResponse> getUserInfoOperation;


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
                                         getBuzzOperation,
                                         getMyApplicationsOperation,
                                         getMySavedChannelsOperation,
                                         getApplicationInfoOperation,
                                         getDashboardOperation,
                                         getMessagesOperation,
                                         getFullMessageOperation,
                                         getUserInfoOperation);

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
                               getBuzzOperation,
                               getMyApplicationsOperation,
                               getMySavedChannelsOperation,
                               getApplicationInfoOperation,
                               getDashboardOperation,
                               getMessagesOperation,
                               getFullMessageOperation,
                               getUserInfoOperation);

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
        RegenerateApplicationTokenRequest request = pojos(RegenerateApplicationTokenRequest.class).get();
        RegenerateApplicationTokenResponse expectedResponse = mock(RegenerateApplicationTokenResponse.class);
        when(regenerateApplicationTokenOperation.process(request))
            .thenReturn(expectedResponse);
        
        RegenerateApplicationTokenResponse result = instance.regenerateToken(request);
        assertThat(result, is(expectedResponse));
        verify(regenerateApplicationTokenOperation).process(request);
        
        //Edge Cases
        assertThrows(() -> instance.regenerateToken(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        when(regenerateApplicationTokenOperation.process(request))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.regenerateToken(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testRegisterHealthCheck() throws Exception
    {
        RegisterHealthCheckRequest request = pojos(RegisterHealthCheckRequest.class).get();
        RegisterHealthCheckResponse response = mock(RegisterHealthCheckResponse.class);
        when(registerHealthCheckOperation.process(request))
            .thenReturn(response);
        
        RegisterHealthCheckResponse result = instance.registerHealthCheck(request);
        assertThat(result, is(response));
        verify(registerHealthCheckOperation).process(request);
        
        //Edge cases
        assertThrows(() -> instance.registerHealthCheck(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        when(registerHealthCheckOperation.process(request))
            .thenThrow(new OperationFailedException());
        assertThrows(() -> instance.registerHealthCheck(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testRemoveSavedChannel() throws Exception
    {
        RemoveSavedChannelRequest request = pojos(RemoveSavedChannelRequest.class).get();
        RemoveSavedChannelResponse response = mock(RemoveSavedChannelResponse.class);
        when(removeSavedChannelOperation.process(request))
            .thenReturn(response);
        
        RemoveSavedChannelResponse result = instance.removeSavedChannel(request);
        assertThat(result, is(response));
        verify(removeSavedChannelOperation).process(request);
        
        //Edge cases
        assertThrows(() -> instance.removeSavedChannel(null))
            .isInstanceOf(InvalidArgumentException.class);
       
        when(removeSavedChannelOperation.process(request))
            .thenThrow(new OperationFailedException());
        assertThrows(() -> instance.removeSavedChannel(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testRenewApplicationToken() throws Exception
    {
        RenewApplicationTokenRequest request = one(pojos(RenewApplicationTokenRequest.class));
        RenewApplicationTokenResponse response = mock(RenewApplicationTokenResponse.class);
        when(renewApplicationTokenOperation.process(request))
            .thenReturn(response);
        
        RenewApplicationTokenResponse result = instance.renewApplicationToken(request);
        assertThat(result, is(response));
        verify(renewApplicationTokenOperation).process(request);
        
        //Edge cases
        assertThrows(() -> instance.renewApplicationToken(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        when(renewApplicationTokenOperation.process(request))
            .thenThrow(new OperationFailedException());
        assertThrows(() -> instance.renewApplicationToken(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testSaveChannel() throws Exception
    {
        SaveChannelRequest request  = one(pojos(SaveChannelRequest.class));
        SaveChannelResponse response = mock(SaveChannelResponse.class);
        when(saveChannelOperation.process(request))
            .thenReturn(response);
        
        SaveChannelResponse result = instance.saveChannel(request);
        assertThat(result, is(response));
        verify(saveChannelOperation).process(request);
        
        //Edge cases
        assertThrows(() -> instance.saveChannel(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        when(saveChannelOperation.process(request))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.saveChannel(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testSearchForApplications() throws Exception
    {
        SearchForApplicationsRequest request = one(pojos(SearchForApplicationsRequest.class));
        SearchForApplicationsResponse response = mock(SearchForApplicationsResponse.class);
        when(searchForApplicationsOperation.process(request))
            .thenReturn(response);
        
        SearchForApplicationsResponse result = instance.searchForApplications(request);
        assertThat(result, is(response));
        verify(searchForApplicationsOperation).process(request);
        
        //Edge cases
        assertThrows(() -> instance.searchForApplications(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        when(searchForApplicationsOperation.process(request))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.searchForApplications(request))
            .isInstanceOf(OperationFailedException.class);
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

    @Test
    public void testGetBuzz() throws Exception
    {
        System.out.println("getBuzz");
        GetBuzzRequest request = pojos(GetBuzzRequest.class).get();
        GetBuzzResponse expectedResponse = mock(GetBuzzResponse.class);
        when(getBuzzOperation.process(request))
            .thenReturn(expectedResponse);
        
        GetBuzzResponse result = instance.getBuzz(request);
        assertThat(result, is(expectedResponse));
        verify(getBuzzOperation).process(request);
        
        //Edge cases
        assertThrows(() -> instance.getBuzz(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        when(getBuzzOperation.process(request))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.getBuzz(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testGetUserInfo() throws Exception
    {
        GetUserInfoRequest request = pojos(GetUserInfoRequest.class).get();
        GetUserInfoResponse expectedResponse = mock(GetUserInfoResponse.class);
        when(getUserInfoOperation.process(request))
            .thenReturn(expectedResponse);
        
        GetUserInfoResponse result = instance.getUserInfo(request);
        assertThat(result, is(expectedResponse));
        verify(getUserInfoOperation).process(request);
        
        //Edge cases
        assertThrows(() -> instance.getUserInfo(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        when(getUserInfoOperation.process(request))
            .thenThrow(new OperationFailedException());
        assertThrows(() -> instance.getUserInfo(request))
            .isInstanceOf(OperationFailedException.class);
    }

}
