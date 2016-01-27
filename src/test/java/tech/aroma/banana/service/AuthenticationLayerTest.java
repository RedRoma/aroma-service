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

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.TokenType;
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoRequest;
import tech.aroma.banana.thrift.authentication.service.GetTokenInfoResponse;
import tech.aroma.banana.thrift.authentication.service.VerifyTokenRequest;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.InvalidTokenException;
import tech.aroma.banana.thrift.functions.TokenFunctions;
import tech.aroma.banana.thrift.service.BananaService;
import tech.aroma.banana.thrift.service.DeleteMessageRequest;
import tech.aroma.banana.thrift.service.DeleteMessageResponse;
import tech.aroma.banana.thrift.service.DismissMessageRequest;
import tech.aroma.banana.thrift.service.DismissMessageResponse;
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
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
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

    private AuthenticationToken expectedAuthToken;

    private String tokenId;

    private VerifyTokenRequest expectedVerifyTokenRequest;

    private GetTokenInfoRequest expectedGetTokenInfoRequest;

    @Before
    public void setUp() throws Exception
    {
        instance = new AuthenticationLayer(delegate, authenticationService);
        verifyZeroInteractions(delegate, authenticationService);

        setupData();

        setupMocks();
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
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);
    }

    @DontRepeat
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
        setupWithBadToken();

        ProvisionApplicationRequest request = new ProvisionApplicationRequest().setToken(userToken);

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
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);

    }

    @DontRepeat
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
        setupWithBadToken();

        RegenerateApplicationTokenRequest request = new RegenerateApplicationTokenRequest().setToken(userToken);

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
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);

    }

    @DontRepeat
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
        setupWithBadToken();

        RegisterHealthCheckRequest request = new RegisterHealthCheckRequest().setToken(userToken);

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
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);

    }

    @DontRepeat
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
        setupWithBadToken();

        RemoveSavedChannelRequest request = new RemoveSavedChannelRequest().setToken(userToken);

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
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);

    }

    @DontRepeat
    @Test
    public void testRenewApplicationTokenWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.renewApplicationToken(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.renewApplicationToken(new RenewApplicationTokenRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testRenewApplicationTokenWithBadToken() throws Exception
    {
        setupWithBadToken();

        RenewApplicationTokenRequest request = new RenewApplicationTokenRequest().setToken(userToken);

        assertThrows(() -> instance.renewApplicationToken(request))
            .isInstanceOf(InvalidTokenException.class);
        verifyZeroInteractions(delegate);
    }

    @Test
    public void testSaveChannel() throws Exception
    {
        SaveChannelRequest request = new SaveChannelRequest().setToken(userToken);
        SaveChannelResponse expected = new SaveChannelResponse();
        when(delegate.saveChannel(request))
            .thenReturn(expected);

        SaveChannelResponse result = instance.saveChannel(request);
        assertThat(result, is(expected));
        verify(delegate).saveChannel(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);

    }

    @DontRepeat
    @Test
    public void testSaveChannelWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.saveChannel(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.saveChannel(new SaveChannelRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testSaveChannelWithBadToken() throws Exception
    {
        setupWithBadToken();

        SaveChannelRequest request = new SaveChannelRequest().setToken(userToken);

        assertThrows(() -> instance.saveChannel(request))
            .isInstanceOf(InvalidTokenException.class);
        verifyZeroInteractions(delegate);
    }

    @Test
    public void testSignIn() throws Exception
    {
        SignInRequest request = new SignInRequest();
        SignInResponse expected = mock(SignInResponse.class);
        when(delegate.signIn(request))
            .thenReturn(expected);

        SignInResponse result = instance.signIn(request);
        assertThat(result, is(expected));
        verifyZeroInteractions(authenticationService);
    }

    @DontRepeat
    @Test
    public void testSignInWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.signIn(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testSignUp() throws Exception
    {
        SignUpRequest request = pojos(SignUpRequest.class).get();
        SignUpResponse expected = mock(SignUpResponse.class);
        when(delegate.signUp(request))
            .thenReturn(expected);

        SignUpResponse result = instance.signUp(request);
        assertThat(result, is(expected));
        verify(delegate).signUp(request);
        verifyZeroInteractions(authenticationService);

    }

    @DontRepeat
    @Test
    public void testSignUpWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.signUp(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testSnoozeChannel() throws Exception
    {
        SnoozeChannelRequest request = new SnoozeChannelRequest().setToken(userToken);
        SnoozeChannelResponse expected = mock(SnoozeChannelResponse.class);
        when(delegate.snoozeChannel(request))
            .thenReturn(expected);

        SnoozeChannelResponse result = instance.snoozeChannel(request);
        assertThat(result, is(expected));
        verify(delegate).snoozeChannel(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);

    }

    @DontRepeat
    @Test
    public void testSnoozeChannelWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.snoozeChannel(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.snoozeChannel(new SnoozeChannelRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testSnoozeChannelWithBadToken() throws Exception
    {
        setupWithBadToken();

        SnoozeChannelRequest request = new SnoozeChannelRequest().setToken(userToken);
        assertThrows(() -> instance.snoozeChannel(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    @Test
    public void testSubscribeToApplication() throws Exception
    {
        SubscribeToApplicationRequest request = new SubscribeToApplicationRequest().setToken(userToken);
        SubscribeToApplicationResponse expected = new SubscribeToApplicationResponse();
        when(delegate.subscribeToApplication(request))
            .thenReturn(expected);

        SubscribeToApplicationResponse result = instance.subscribeToApplication(request);
        assertThat(result, is(expected));
        verify(delegate).subscribeToApplication(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);

    }

    @DontRepeat
    @Test
    public void testSubscribeToApplicationWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.subscribeToApplication(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.subscribeToApplication(new SubscribeToApplicationRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testSubscribeToApplicationWithBadToken() throws Exception
    {
        setupWithBadToken();

        SubscribeToApplicationRequest request = new SubscribeToApplicationRequest().setToken(userToken);

        assertThrows(() -> instance.subscribeToApplication(request))
            .isInstanceOf(InvalidTokenException.class);
        verifyZeroInteractions(delegate);
    }

    @Test
    public void testGetActivity() throws Exception
    {
        GetActivityRequest request = new GetActivityRequest().setToken(userToken);
        GetActivityResponse expected = new GetActivityResponse();

        when(delegate.getActivity(request))
            .thenReturn(expected);

        GetActivityResponse result = instance.getActivity(request);
        assertThat(result, is(expected));
        verify(delegate).getActivity(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);

    }

    @DontRepeat
    @Test
    public void testGetActivityWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.getActivity(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getActivity(new GetActivityRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testGetActivityWithBadToken() throws Exception
    {
        setupWithBadToken();

        GetActivityRequest request = new GetActivityRequest().setToken(userToken);

        assertThrows(() -> instance.getActivity(request))
            .isInstanceOf(InvalidTokenException.class);
        verifyZeroInteractions(delegate);
    }

    @Test
    public void testGetApplicationInfo() throws Exception
    {
        GetApplicationInfoRequest request = new GetApplicationInfoRequest().setToken(expectedAuthToken);
        GetApplicationInfoResponse expected = new GetApplicationInfoResponse();

        when(delegate.getApplicationInfo(request))
            .thenReturn(expected);

        GetApplicationInfoResponse result = instance.getApplicationInfo(request);
        assertThat(result, is(expected));
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(delegate).getApplicationInfo(request);
    }

    @Test
    public void testGetApplicationInfoWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getApplicationInfo(null))
            .isInstanceOf(InvalidArgumentException.class);

        GetApplicationInfoRequest emptyRequest = new GetApplicationInfoRequest();

        assertThrows(() -> instance.getApplicationInfo(emptyRequest))
            .isInstanceOf(InvalidTokenException.class);

    }

    @Test
    public void testGetApplicaitonInfoWithBadToken() throws Exception
    {
        setupWithBadToken();

        GetApplicationInfoRequest request = new GetApplicationInfoRequest().setToken(expectedAuthToken);

        assertThrows(() -> instance.getApplicationInfo(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    @Test
    public void testGetDashboard() throws Exception
    {
        GetDashboardRequest request = new GetDashboardRequest().setToken(userToken);
        GetDashboardResponse expected = new GetDashboardResponse();
        when(delegate.getDashboard(request))
            .thenReturn(expected);

        GetDashboardResponse result = instance.getDashboard(request);
        assertThat(result, is(expected));
        verify(delegate).getDashboard(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);
    }

    @DontRepeat
    @Test
    public void testGetDashboardWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.getDashboard(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getDashboard(new GetDashboardRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testGetDashboardWithBadToken() throws Exception
    {
        setupWithBadToken();

        GetDashboardRequest request = new GetDashboardRequest().setToken(userToken);

        assertThrows(() -> instance.getDashboard(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    @Test
    public void testGetMessages() throws Exception
    {
        GetMessagesRequest request = new GetMessagesRequest().setToken(userToken);
        GetMessagesResponse expected = new GetMessagesResponse();
        when(delegate.getMessages(request))
            .thenReturn(expected);

        GetMessagesResponse result = instance.getMessages(request);
        assertThat(result, is(expected));
        verify(delegate).getMessages(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);
    }

    @DontRepeat
    @Test
    public void testGetMessagesWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.getMessages(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getMessages(new GetMessagesRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testGetMessagesWithBadToken() throws Exception
    {
        setupWithBadToken();

        GetMessagesRequest request = new GetMessagesRequest()
            .setToken(userToken);

        assertThrows(() -> instance.getMessages(request))
            .isInstanceOf(InvalidTokenException.class);
        verifyZeroInteractions(delegate);
    }

    @Test
    public void testGetFullMessage() throws Exception
    {
        GetFullMessageRequest request = new GetFullMessageRequest().setToken(userToken);
        GetFullMessageResponse expected = new GetFullMessageResponse();
        when(delegate.getFullMessage(request))
            .thenReturn(expected);

        GetFullMessageResponse result = instance.getFullMessage(request);
        assertThat(result, is(expected));
        verify(delegate).getFullMessage(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);
    }

    @DontRepeat
    @Test
    public void testGetFullMessageWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.getFullMessage(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getFullMessage(new GetFullMessageRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testGetFullMessageWithBadToken() throws Exception
    {
        setupWithBadToken();

        GetFullMessageRequest request = new GetFullMessageRequest().setToken(userToken);

        assertThrows(() -> instance.getFullMessage(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    @Test
    public void testGetMyApplications() throws Exception
    {
        GetMyApplicationsRequest request = new GetMyApplicationsRequest().setToken(userToken);
        GetMyApplicationsResponse expected = new GetMyApplicationsResponse();
        when(delegate.getMyApplications(request))
            .thenReturn(expected);

        GetMyApplicationsResponse result = instance.getMyApplications(request);
        assertThat(result, is(expected));
        verify(delegate).getMyApplications(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);
    }

    @DontRepeat
    @Test
    public void testGetMyApplicationsWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.getMyApplications(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getMyApplications(new GetMyApplicationsRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testGetMyApplicationsWithBadToken() throws Exception
    {
        setupWithBadToken();

        GetMyApplicationsRequest request = new GetMyApplicationsRequest().setToken(userToken);
        assertThrows(() -> instance.getMyApplications(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    @Test
    public void testGetMySavedChannels() throws Exception
    {
        GetMySavedChannelsRequest request = new GetMySavedChannelsRequest().setToken(userToken);
        GetMySavedChannelsResponse expected = new GetMySavedChannelsResponse();
        when(delegate.getMySavedChannels(request))
            .thenReturn(expected);

        GetMySavedChannelsResponse result = instance.getMySavedChannels(request);
        assertThat(result, is(expected));
        verify(delegate).getMySavedChannels(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);
    }

    @DontRepeat
    @Test
    public void testGetMySavedChannelsWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.getMySavedChannels(null))
            .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.getMySavedChannels(new GetMySavedChannelsRequest()))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testGetMySavedChannelsWithBadToken() throws Exception
    {
        setupWithBadToken();

        GetMySavedChannelsRequest request = new GetMySavedChannelsRequest().setToken(userToken);

        assertThrows(() -> instance.getMySavedChannels(request))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testSearchForApplications() throws Exception
    {
        SearchForApplicationsRequest request = new SearchForApplicationsRequest().setToken(userToken);
        SearchForApplicationsResponse expected = new SearchForApplicationsResponse();
        when(delegate.searchForApplications(request))
            .thenReturn(expected);

        SearchForApplicationsResponse result = instance.searchForApplications(request);
        assertThat(result, is(expected));
        verify(delegate).searchForApplications(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);
    }

    @Test
    public void testSearchForApplicationsWithBadToken() throws Exception
    {
        setupWithBadToken();

        SearchForApplicationsRequest request = new SearchForApplicationsRequest().setToken(userToken);
        assertThrows(() -> instance.searchForApplications(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    @Test
    public void testGetBuzz() throws Exception
    {
        GetBuzzRequest request = new GetBuzzRequest().setToken(userToken);
        GetBuzzResponse expected = new GetBuzzResponse();
        when(delegate.getBuzz(request))
            .thenReturn(expected);

        GetBuzzResponse result = instance.getBuzz(request);
        assertThat(result, is(expected));
        verify(delegate).getBuzz(request);
    }

    @DontRepeat
    @Test
    public void testGetBuzzWithNoToken() throws Exception
    {
        GetBuzzRequest request = new GetBuzzRequest();
        GetBuzzResponse expected = new GetBuzzResponse();
        when(delegate.getBuzz(request)).thenReturn(expected);

        GetBuzzResponse response = instance.getBuzz(request);
        assertThat(response, is(sameInstance(expected)));
    }

    @Test
    public void testGetBuzzWithBadToken() throws Exception
    {
        setupWithBadToken();
        GetBuzzRequest request = new GetBuzzRequest().setToken(userToken);

        assertThrows(() -> instance.getBuzz(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    @Test
    public void testGetBuzzWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getBuzz(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetUserInfo() throws Exception
    {
        GetUserInfoRequest request = new GetUserInfoRequest().setToken(userToken);
        GetUserInfoResponse expected = new GetUserInfoResponse();
        when(delegate.getUserInfo(request))
            .thenReturn(expected);

        GetUserInfoResponse result = instance.getUserInfo(request);
        assertThat(result, is(expected));
        verify(delegate).getUserInfo(request);
        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);
    }

    @Test
    public void testDeleteMessage() throws Exception
    {
        DeleteMessageRequest request = new DeleteMessageRequest().setToken(userToken);
        DeleteMessageResponse expected = new DeleteMessageResponse();
        when(delegate.deleteMessage(request))
            .thenReturn(expected);

        DeleteMessageResponse response = instance.deleteMessage(request);
        assertThat(response, is(sameInstance(expected)));

        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);
    }

    @Test
    public void testDeleteMessageWithBadToken() throws Exception
    {
        setupWithBadToken();

        DeleteMessageRequest request = new DeleteMessageRequest().setToken(userToken);

        assertThrows(() -> instance.deleteMessage(request))
            .isInstanceOf(InvalidTokenException.class);

        verifyZeroInteractions(delegate);
    }

    @Test
    public void testDeleteMessageWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.deleteMessage(null))
            .isInstanceOf(InvalidArgumentException.class);

        DeleteMessageRequest emptyRequest = new DeleteMessageRequest();
        assertThrows(() -> instance.deleteMessage(emptyRequest))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testDismissMessage() throws Exception
    {
        DismissMessageRequest request = new DismissMessageRequest().setToken(userToken);
        DismissMessageResponse expected = new DismissMessageResponse();
        when(delegate.dismissMessage(request)).thenReturn(expected);

        DismissMessageResponse response = instance.dismissMessage(request);
        assertThat(response, is(sameInstance(expected)));

        verify(authenticationService).verifyToken(expectedVerifyTokenRequest);
        verify(authenticationService).getTokenInfo(expectedGetTokenInfoRequest);
    }

    @Test
    public void testDismissMessageWithBadToken() throws Exception
    {
        setupWithBadToken();

        DismissMessageRequest request = new DismissMessageRequest().setToken(userToken);

        assertThrows(() -> instance.dismissMessage(request))
            .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    public void testDismissMessageWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.dismissMessage(null))
            .isInstanceOf(InvalidArgumentException.class);

        DismissMessageRequest emptyRequest = new DismissMessageRequest();
        assertThrows(() -> instance.dismissMessage(emptyRequest))
            .isInstanceOf(InvalidTokenException.class);
    }

    private void setupWithBadToken() throws TException
    {
        when(authenticationService.verifyToken(expectedVerifyTokenRequest))
            .thenThrow(new InvalidTokenException());
    }

    private void setupMocks() throws Exception
    {
        when(authenticationService.getTokenInfo(expectedGetTokenInfoRequest))
            .thenReturn(new GetTokenInfoResponse(expectedAuthToken));
    }

    private void setupData()
    {
        tokenId = userToken.tokenId;

        expectedAuthToken = TokenFunctions.userTokenToAuthTokenFunction().apply(userToken);
        expectedVerifyTokenRequest = new VerifyTokenRequest(tokenId)
            .setOwnerId(userToken.userId);

        expectedGetTokenInfoRequest = new GetTokenInfoRequest()
            .setTokenType(TokenType.USER)
            .setTokenId(tokenId);
    }

}
