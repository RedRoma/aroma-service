/*
 * Copyright 2016 Aroma Tech.
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
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.VerifyTokenRequest;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
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
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class AuthenticationLayerTest
{
    
    @Mock
    private AuthenticationService.Iface authenticationService;
    
    @Mock
    private BananaService.Iface delegate;
    
    private AuthenticationLayer instance;
    
    @GeneratePojo
    private UserToken userToken;
    
    private String tokenId;
    
    private VerifyTokenRequest expectedVerifyTokenRequest;
    
    @Before
    public void setUp()
    {
        instance = new AuthenticationLayer(delegate, authenticationService);
        verifyZeroInteractions(delegate, authenticationService);
        
        tokenId = userToken.tokenId;
        expectedVerifyTokenRequest = new VerifyTokenRequest(tokenId);
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new AuthenticationLayer(null, authenticationService))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new AuthenticationLayer(delegate, null))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testGetApiVersion() throws Exception
    {
        double result = instance.getApiVersion();
        verify(delegate).getApiVersion();
        verifyZeroInteractions(authenticationService);
    }
    
    @Test
    public void testProvisionApplication() throws Exception
    {
        ProvisionApplicationRequest request = new ProvisionApplicationRequest().setToken(userToken);
        ProvisionApplicationResponse expected = mock(ProvisionApplicationResponse.class);
        when(delegate.provisionApplication(request))
            .thenReturn(expected);
        
        ProvisionApplicationResponse result = instance.provisionApplication(request);
        assertThat(result, is(expected));
        verify(delegate).provisionApplication(request);
        
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
    }
    
    @Test
    public void testProvisionApplicationWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.provisionApplication(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.provisionApplication(new ProvisionApplicationRequest()))
            .isInstanceOf(InvalidTokenException.class);
        
        verifyZeroInteractions(delegate);
    }
    
    @Test
    public void testProvisionApplicationWithBadToken() throws Exception
    {
        ProvisionApplicationRequest request = new ProvisionApplicationRequest().setToken(userToken);
        
        when(authenticationService.verifyToken(expectedVerifyTokenRequest))
            .thenThrow(new InvalidTokenException());
        
        assertThrows(() -> instance.provisionApplication(request))
            .isInstanceOf(InvalidTokenException.class);
        
        verifyZeroInteractions(delegate);
    }
    
    @Test
    public void testRegenerateToken() throws Exception
    {
        RegenerateApplicationTokenRequest request = new RegenerateApplicationTokenRequest().setToken(userToken);
        RegenerateApplicationTokenResponse expected = mock(RegenerateApplicationTokenResponse.class);
        when(delegate.regenerateToken(request))
            .thenReturn(expected);
        
        RegenerateApplicationTokenResponse result = instance.regenerateToken(request);
        assertThat(result, is(expected));
        verify(delegate).regenerateToken(request);
        
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
    }
    
    @Test
    public void testRegenerateTokenWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.regenerateToken(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.regenerateToken(new RegenerateApplicationTokenRequest()))
            .isInstanceOf(InvalidTokenException.class);
        
        verifyZeroInteractions(delegate);
    }
    
    @Test
    public void testRegenerateTokenWithBadToken() throws Exception
    {
        RegenerateApplicationTokenRequest request = new RegenerateApplicationTokenRequest().setToken(userToken);
        
        when(authenticationService.verifyToken(expectedVerifyTokenRequest))
            .thenThrow(InvalidTokenException.class);
        
        assertThrows(() -> instance.regenerateToken(request))
            .isInstanceOf(InvalidTokenException.class);
        
        verifyZeroInteractions(delegate);
    }
    
    
    @Test
    public void testRegisterHealthCheck() throws Exception
    {
        RegisterHealthCheckRequest request = new RegisterHealthCheckRequest().setToken(userToken);
        RegisterHealthCheckResponse expected = mock(RegisterHealthCheckResponse.class);
        when(delegate.registerHealthCheck(request))
            .thenReturn(expected);
        
        RegisterHealthCheckResponse result = instance.registerHealthCheck(request);
        assertThat(result, is(expected));
        verify(delegate).registerHealthCheck(request);
        
        VerifyTokenRequest verifyTokenRequest = new VerifyTokenRequest()
            .setTokenId(tokenId);
        verify(authenticationService).verifyToken(verifyTokenRequest);
    }
    
    @Test
    public void testRegisterHealthCheckWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.registerHealthCheck(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.registerHealthCheck(new RegisterHealthCheckRequest()))
            .isInstanceOf(InvalidTokenException.class);
        
        verifyZeroInteractions(delegate);
    }
    
    @Test
    public void testRegisterHealthCheckWithBadToken() throws Exception
    {
        
        RegisterHealthCheckRequest request = new RegisterHealthCheckRequest().setToken(userToken);
        when(authenticationService.verifyToken(new VerifyTokenRequest(tokenId)))
            .thenThrow(InvalidTokenException.class);
        
        assertThrows(() -> instance.registerHealthCheck(request))
            .isInstanceOf(InvalidTokenException.class);
        
        verifyZeroInteractions(delegate);
    }
    
    @Test
    public void testRemoveSavedChannel() throws Exception
    {
        RemoveSavedChannelRequest request = new RemoveSavedChannelRequest().setToken(userToken);
        RemoveSavedChannelResponse expected = mock(RemoveSavedChannelResponse.class);
        when(delegate.removeSavedChannel(request))
            .thenReturn(expected);
        
        RemoveSavedChannelResponse result = instance.removeSavedChannel(request);
        assertThat(result, is(expected));
        verify(delegate).removeSavedChannel(request);
        verify(authenticationService).verifyToken(new VerifyTokenRequest(tokenId));
    }
    
    @Test
    public void testRemoveSavedChannelWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.removeSavedChannel(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.removeSavedChannel(new RemoveSavedChannelRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }
    
    @Test
    public void testRemoveSavedChannelWithBadToken() throws Exception
    {
        RemoveSavedChannelRequest request = new RemoveSavedChannelRequest().setToken(userToken);
        when(authenticationService.verifyToken(new VerifyTokenRequest(tokenId)))
            .thenThrow(new InvalidTokenException());
        
        assertThrows(() -> instance.removeSavedChannel(request))
            .isInstanceOf(InvalidTokenException.class);
        verifyZeroInteractions(delegate);
    }
    
    @Test
    public void testRenewApplicationToken() throws Exception
    {
        RenewApplicationTokenRequest request = new RenewApplicationTokenRequest().setToken(userToken);
        RenewApplicationTokenResponse expected = new RenewApplicationTokenResponse();
        when(delegate.renewApplicationToken(request))
            .thenReturn(expected);
        
        RenewApplicationTokenResponse result = instance.renewApplicationToken(request);
        assertThat(result, is(expected));
        verify(delegate).renewApplicationToken(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
    }
    
    @Test
    public void testSaveChannel() throws Exception
    {
        SaveChannelRequest request = null;
        SaveChannelResponse expResult = null;
//        SaveChannelResponse result = instance.saveChannel(request);
    }
    
    @Test
    public void testSignIn() throws Exception
    {
        SignInRequest request = null;
        SignInResponse expResult = null;
//        SignInResponse result = instance.signIn(request);
    }
    
    @Test
    public void testSignUp() throws Exception
    {
        SignUpRequest request = null;
        SignUpResponse expResult = null;
//        SignUpResponse result = instance.signUp(request);
    }
    
    @Test
    public void testSnoozeChannel() throws Exception
    {
        SnoozeChannelRequest request = null;
        SnoozeChannelResponse expResult = null;
//        SnoozeChannelResponse result = instance.snoozeChannel(request);
    }
    
    @Test
    public void testSubscribeToApplication() throws Exception
    {
        SubscribeToApplicationRequest request = null;
        SubscribeToApplicationResponse expResult = null;
//        SubscribeToApplicationResponse result = instance.subscribeToApplication(request);
    }
    
    @Test
    public void testGetActivity() throws Exception
    {
        GetActivityRequest request = null;
        GetActivityResponse expResult = null;
//        GetActivityResponse result = instance.getActivity(request);
    }
    
    @Test
    public void testGetApplicationInfo() throws Exception
    {
        GetApplicationInfoRequest request = null;
        GetApplicationInfoResponse expResult = null;
//        GetApplicationInfoResponse result = instance.getApplicationInfo(request);
    }
    
    @Test
    public void testGetDashboard() throws Exception
    {
        GetDashboardRequest request = null;
        GetDashboardResponse expResult = null;
//        GetDashboardResponse result = instance.getDashboard(request);
    }
    
    @Test
    public void testGetMessages() throws Exception
    {
        GetMessagesRequest request = null;
        GetMessagesResponse expResult = null;
//        GetMessagesResponse result = instance.getMessages(request);
    }
    
    @Test
    public void testGetFullMessage() throws Exception
    {
        GetFullMessageRequest request = null;
        GetFullMessageResponse expResult = null;
//        GetFullMessageResponse result = instance.getFullMessage(request);
    }
    
    @Test
    public void testGetMyApplications() throws Exception
    {
        GetMyApplicationsRequest request = null;
        GetMyApplicationsResponse expResult = null;
//        GetMyApplicationsResponse result = instance.getMyApplications(request);
    }
    
    @Test
    public void testGetMySavedChannels() throws Exception
    {
        GetMySavedChannelsRequest request = null;
        GetMySavedChannelsResponse expResult = null;
//        GetMySavedChannelsResponse result = instance.getMySavedChannels(request);
    }
    
    @Test
    public void testSearchForApplications() throws Exception
    {
        SearchForApplicationsRequest request = null;
        SearchForApplicationsResponse expResult = null;
//        SearchForApplicationsResponse result = instance.searchForApplications(request);
    }
    
}