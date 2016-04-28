
/*
 * Copyright 2016 RedRoma.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import tech.aroma.thrift.AromaConstants;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.generators.TokenGenerators;
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
import tech.aroma.thrift.service.GetMySavedChannelsRequest;
import tech.aroma.thrift.service.GetMySavedChannelsResponse;
import tech.aroma.thrift.service.GetReactionsRequest;
import tech.aroma.thrift.service.GetReactionsResponse;
import tech.aroma.thrift.service.GetUserInfoRequest;
import tech.aroma.thrift.service.GetUserInfoResponse;
import tech.aroma.thrift.service.ProvisionApplicationRequest;
import tech.aroma.thrift.service.ProvisionApplicationResponse;
import tech.aroma.thrift.service.RegenerateApplicationTokenRequest;
import tech.aroma.thrift.service.RegenerateApplicationTokenResponse;
import tech.aroma.thrift.service.RegisterHealthCheckRequest;
import tech.aroma.thrift.service.RegisterHealthCheckResponse;
import tech.aroma.thrift.service.RemoveSavedChannelRequest;
import tech.aroma.thrift.service.RemoveSavedChannelResponse;
import tech.aroma.thrift.service.RenewApplicationTokenRequest;
import tech.aroma.thrift.service.RenewApplicationTokenResponse;
import tech.aroma.thrift.service.SaveChannelRequest;
import tech.aroma.thrift.service.SaveChannelResponse;
import tech.aroma.thrift.service.SearchForApplicationsRequest;
import tech.aroma.thrift.service.SearchForApplicationsResponse;
import tech.aroma.thrift.service.SignInRequest;
import tech.aroma.thrift.service.SignInResponse;
import tech.aroma.thrift.service.SignUpRequest;
import tech.aroma.thrift.service.SignUpResponse;
import tech.aroma.thrift.service.SnoozeChannelRequest;
import tech.aroma.thrift.service.SnoozeChannelResponse;
import tech.aroma.thrift.service.UnfollowApplicationRequest;
import tech.aroma.thrift.service.UnfollowApplicationResponse;
import tech.aroma.thrift.service.UpdateApplicationRequest;
import tech.aroma.thrift.service.UpdateApplicationResponse;
import tech.aroma.thrift.service.UpdateReactionsRequest;
import tech.aroma.thrift.service.UpdateReactionsResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class AromaServiceBaseTest
{
    //Action and Save Operations

    @Mock
    private ThriftOperation<DeleteApplicationRequest, DeleteApplicationResponse> deleteApplicationOperation;

    @Mock
    private ThriftOperation<DeleteMessageRequest, DeleteMessageResponse> deleteMessageOperation;

    @Mock
    private ThriftOperation<DismissMessageRequest, DismissMessageResponse> dismissMessageOperation;

    @Mock
    private ThriftOperation<FollowApplicationRequest, FollowApplicationResponse> followApplicationOperation;

    @Mock
    private ThriftOperation<SignInRequest, SignInResponse> signInOperation;

    @Mock
    private ThriftOperation<SignUpRequest, SignUpResponse> signUpOperation;

    @Mock
    private ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse> provisionApplicationOperation;

    @Mock
    private ThriftOperation<RegenerateApplicationTokenRequest, RegenerateApplicationTokenResponse> regenerateApplicationTokenOperation;

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
  
    @Mock
    private ThriftOperation<UpdateApplicationRequest, UpdateApplicationResponse> updateApplicationOperation;
    
    @Mock
    private ThriftOperation<UpdateReactionsRequest, UpdateReactionsResponse> updateReactionsOperation;
    
    @Mock
    private ThriftOperation<UnfollowApplicationRequest, UnfollowApplicationResponse> unfollowApplicationOperation;

    //Query and GET Operations
    @Mock
    private ThriftOperation<GetActivityRequest, GetActivityResponse> getActivityOperation;

    @Mock
    private ThriftOperation<GetBuzzRequest, GetBuzzResponse> getBuzzOperation;

    @Mock
    private ThriftOperation<GetApplicationsFollowedByRequest, GetApplicationsFollowedByResponse> getApplicationsFollowedByOperation;

    @Mock
    private ThriftOperation<GetApplicationsOwnedByRequest, GetApplicationsOwnedByResponse> getApplicationsOwnedByOperation;

    @Mock
    private ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse> getMySavedChannelsOperation;

    @Mock
    private ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse> getApplicationInfoOperation;

    @Mock
    private ThriftOperation<GetDashboardRequest, GetDashboardResponse> getDashboardOperation;

    @Mock
    private ThriftOperation<GetApplicationMessagesRequest, GetApplicationMessagesResponse> getApplicationMessagesOperation;

    @Mock
    private ThriftOperation<GetInboxRequest, GetInboxResponse> getInboxOperation;

    @Mock
    private ThriftOperation<GetMediaRequest, GetMediaResponse> getMediaOperation;

    @Mock
    private ThriftOperation<GetFullMessageRequest, GetFullMessageResponse> getFullMessageOperation;

    @Mock
    private ThriftOperation<GetReactionsRequest, GetReactionsResponse> getReactionsOperation;
    
    @Mock
    private ThriftOperation<GetUserInfoRequest, GetUserInfoResponse> getUserInfoOperation;

    private AromaServiceBase instance;
    
    private UserToken token;

    @Before
    public void setUp() throws Exception
    {
        instance = new AromaServiceBase(deleteApplicationOperation,
                                        deleteMessageOperation,
                                        dismissMessageOperation,
                                        signInOperation,
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
                                        updateApplicationOperation,
                                        updateReactionsOperation,
                                        unfollowApplicationOperation,
                                        getActivityOperation,
                                        getBuzzOperation,
                                        getApplicationsFollowedByOperation,
                                        getApplicationsOwnedByOperation,
                                        getMySavedChannelsOperation,
                                        getApplicationInfoOperation,
                                        getDashboardOperation,
                                        getInboxOperation,
                                        getMediaOperation,
                                        getApplicationMessagesOperation,
                                        getFullMessageOperation,
                                        getReactionsOperation,
                                        getUserInfoOperation);

        verifyZeroInteractions(deleteApplicationOperation,
                               deleteMessageOperation,
                               dismissMessageOperation,
                               signInOperation,
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
                               updateApplicationOperation,
                               updateReactionsOperation,
                               unfollowApplicationOperation,
                               getActivityOperation,
                               getBuzzOperation,
                               getApplicationsFollowedByOperation,
                               getApplicationsOwnedByOperation,
                               getMySavedChannelsOperation,
                               getApplicationInfoOperation,
                               getDashboardOperation,
                               getInboxOperation,
                               getMediaOperation,
                               getApplicationMessagesOperation,
                               getFullMessageOperation,
                               getReactionsOperation,
                               getUserInfoOperation);

        
        setupData();
    }

    private void setupData() throws Exception
    {
        token = one(TokenGenerators.userTokens());
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
    public void testGetApplicationsFollowedBy() throws Exception
    {
        GetApplicationsFollowedByRequest request = pojos(GetApplicationsFollowedByRequest.class).get();
        GetApplicationsFollowedByResponse expectedResponse = pojos(GetApplicationsFollowedByResponse.class).get();
        when(getApplicationsFollowedByOperation.process(request)).thenReturn(expectedResponse);

        GetApplicationsFollowedByResponse response = instance.getApplicationsFollowedBy(request);
        assertThat(response, is(expectedResponse));
        verify(getApplicationsFollowedByOperation).process(request);

        //Edge cases
        assertThrows(() -> instance.getApplicationsFollowedBy(null))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @DontRepeat
    @Test
    public void testGetApplicationsFollowedByWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getApplicationsFollowedBy(null))
            .isInstanceOf(InvalidArgumentException.class);
        verifyZeroInteractions(getApplicationsFollowedByOperation);
    }

    @Test
    public void testGetApplicationsOwnedBy() throws Exception
    {
        GetApplicationsOwnedByRequest request = pojos(GetApplicationsOwnedByRequest.class).get();
        GetApplicationsOwnedByResponse expectedResponse = pojos(GetApplicationsOwnedByResponse.class).get();
        when(getApplicationsOwnedByOperation.process(request)).thenReturn(expectedResponse);

        GetApplicationsOwnedByResponse response = instance.getApplicationsOwnedBy(request);
        assertThat(response, is(expectedResponse));
        verify(getApplicationsOwnedByOperation).process(request);

        //Edge cases
        assertThrows(() -> instance.getApplicationsOwnedBy(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testGetApplicationsOwnedByWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getApplicationsOwnedBy(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        verifyZeroInteractions(getApplicationsOwnedByOperation);
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
        SaveChannelRequest request = one(pojos(SaveChannelRequest.class));
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
    }

    @DontRepeat
    @Test
    public void testSignInWhenFails() throws Exception
    {
        SignInRequest request = new SignInRequest();
        when(signInOperation.process(request))
            .thenThrow(new UserDoesNotExistException());

        assertThrows(() -> instance.signIn(request))
            .isInstanceOf(UserDoesNotExistException.class);
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
        SignUpRequest request = one(pojos(SignUpRequest.class));
        SignUpResponse response = mock(SignUpResponse.class);
        when(signUpOperation.process(request))
            .thenReturn(response);

        SignUpResponse result = instance.signUp(request);
        assertThat(result, is(response));
        verify(signUpOperation).process(request);

        //Edge cases
        assertThrows(() -> instance.signUp(null))
            .isInstanceOf(InvalidArgumentException.class);

        when(signUpOperation.process(request))
            .thenThrow(new InvalidCredentialsException());

        assertThrows(() -> instance.signUp(request))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    public void testSnoozeChannel() throws Exception
    {
        SnoozeChannelRequest request = one(pojos(SnoozeChannelRequest.class));
        SnoozeChannelResponse response = mock(SnoozeChannelResponse.class);
        when(snoozeChannelOperation.process(request))
            .thenReturn(response);

        SnoozeChannelResponse result = instance.snoozeChannel(request);
        assertThat(result, is(response));

        //Edge cases
        assertThrows(() -> instance.snoozeChannel(null))
            .isInstanceOf(InvalidArgumentException.class);

        when(snoozeChannelOperation.process(request))
            .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.snoozeChannel(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testFollowApplication() throws Exception
    {
        FollowApplicationRequest request = one(pojos(FollowApplicationRequest.class));
        FollowApplicationResponse expected = one(pojos(FollowApplicationResponse.class));

    }

    @Test
    public void testRegenerateToken() throws Exception
    {
        RegenerateApplicationTokenRequest request = one(pojos(RegenerateApplicationTokenRequest.class));
        RegenerateApplicationTokenResponse expected = one(pojos(RegenerateApplicationTokenResponse.class));
        when(regenerateApplicationTokenOperation.process(request)).thenReturn(expected);

        RegenerateApplicationTokenResponse response = instance.regenerateToken(request);
        assertThat(response, is(sameInstance(expected)));
    }

    @DontRepeat
    @Test
    public void testRegenerateTokenWithBadArgs()
    {
        assertThrows(() -> instance.regenerateToken(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetApiVersion() throws Exception
    {
        double apiVersion = instance.getApiVersion();
        assertThat(apiVersion, is(AromaConstants.API_VERSION));
    }

    @Test
    public void testGetActivity() throws Exception
    {
        GetActivityRequest request = one(pojos(GetActivityRequest.class));
        GetActivityResponse expected = one(pojos(GetActivityResponse.class));
        when(getActivityOperation.process(request)).thenReturn(expected);

        GetActivityResponse response = instance.getActivity(request);
        assertThat(response, is(sameInstance(response)));
    }

    @DontRepeat
    @Test
    public void testGetActivityWhenFails() throws Exception
    {
        GetActivityRequest request = one(pojos(GetActivityRequest.class));

        when(getActivityOperation.process(request))
            .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.getActivity(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testGetApplicationMessages() throws Exception
    {
        GetApplicationMessagesRequest request = one(pojos(GetApplicationMessagesRequest.class));
        GetApplicationMessagesResponse expected = one(pojos(GetApplicationMessagesResponse.class));
        when(getApplicationMessagesOperation.process(request)).thenReturn(expected);

        GetApplicationMessagesResponse response = instance.getApplicationMessages(request);
        assertThat(response, is(sameInstance(response)));
    }

    @DontRepeat
    @Test
    public void testGetApplicationMessagesWhenFails() throws Exception
    {
        GetApplicationMessagesRequest request = one(pojos(GetApplicationMessagesRequest.class));
        when(getApplicationMessagesOperation.process(request))
            .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.getApplicationMessages(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testGetInbox() throws Exception
    {
        GetInboxRequest request = one(pojos(GetInboxRequest.class));
        GetInboxResponse expected = one(pojos(GetInboxResponse.class));
        when(getInboxOperation.process(request)).thenReturn(expected);

        GetInboxResponse response = instance.getInbox(request);
        assertThat(response, is(sameInstance(response)));
    }

    @DontRepeat
    @Test
    public void testGetInboxWhenFails() throws Exception
    {
        GetInboxRequest request = one(pojos(GetInboxRequest.class));
        when(getInboxOperation.process(request))
            .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.getInbox(request))
            .isInstanceOf(OperationFailedException.class);
    }


    @Test
    public void testGetMedia() throws Exception
    {
        GetMediaRequest request = one(pojos(GetMediaRequest.class));
        GetMediaResponse expected = one(pojos(GetMediaResponse.class));
        when(getMediaOperation.process(request)).thenReturn(expected);

        GetMediaResponse response = instance.getMedia(request);
        assertThat(response, is(sameInstance(response)));
    }

    @DontRepeat
    @Test
    public void testGetMediaWhenFails() throws Exception
    {
        GetMediaRequest request = one(pojos(GetMediaRequest.class));
        when(getMediaOperation.process(request))
            .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.getMedia(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testGetFullMessage() throws Exception
    {
        GetFullMessageRequest request = one(pojos(GetFullMessageRequest.class));
        GetFullMessageResponse expected = one(pojos(GetFullMessageResponse.class));
        when(getFullMessageOperation.process(request)).thenReturn(expected);

        GetFullMessageResponse response = instance.getFullMessage(request);
        assertThat(response, is(sameInstance(expected)));
    }

    @Test
    public void testGetFullMessageWhenFails() throws Exception
    {
        GetFullMessageRequest request = one(pojos(GetFullMessageRequest.class));
        when(getFullMessageOperation.process(request))
            .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.getFullMessage(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testGetBuzz() throws Exception
    {
        GetBuzzRequest request = pojos(GetBuzzRequest.class).get();
        GetBuzzResponse expectedResponse = mock(GetBuzzResponse.class);
        when(getBuzzOperation.process(request)).thenReturn(expectedResponse);

        GetBuzzResponse result = instance.getBuzz(request);
        assertThat(result, is(expectedResponse));
        verify(getBuzzOperation).process(request);
    }

    @DontRepeat
    @Test
    public void testGetBuzzWhenThrows() throws Exception
    {
        GetBuzzRequest request = pojos(GetBuzzRequest.class).get();

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

    }

    @DontRepeat
    @Test
    public void testGetUserInfoWhenThrows() throws Exception
    {
        GetUserInfoRequest request = pojos(GetUserInfoRequest.class).get();

        assertThrows(() -> instance.getUserInfo(null))
            .isInstanceOf(InvalidArgumentException.class);

        when(getUserInfoOperation.process(request))
            .thenThrow(new OperationFailedException());
        assertThrows(() -> instance.getUserInfo(request))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testDeleteApplication() throws Exception
    {
        DeleteApplicationRequest request = one(pojos(DeleteApplicationRequest.class));
        DeleteApplicationResponse expected = one(pojos(DeleteApplicationResponse.class));
        when(deleteApplicationOperation.process(request)).thenReturn(expected);

        DeleteApplicationResponse response = instance.deleteApplication(request);
        assertThat(response, is(sameInstance(expected)));
    }

    @DontRepeat
    @Test
    public void testDeleteApplicationWhenThrows() throws Exception
    {
        when(deleteApplicationOperation.process(Mockito.any()))
            .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.deleteApplication(new DeleteApplicationRequest()))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @Test
    public void testDeleteMessage() throws Exception
    {
        DeleteMessageRequest request = one(pojos(DeleteMessageRequest.class));
        DeleteMessageResponse expected = one(pojos(DeleteMessageResponse.class));
        when(deleteMessageOperation.process(request)).thenReturn(expected);

        DeleteMessageResponse response = instance.deleteMessage(request);
        assertThat(response, is(sameInstance(expected)));
    }

    @DontRepeat
    @Test
    public void testDeleteMessageWhenThrows() throws Exception
    {
        when(deleteMessageOperation.process(Mockito.any()))
            .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.deleteMessage(new DeleteMessageRequest()))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testDismissMessage() throws Exception
    {
        DismissMessageRequest request = one(pojos(DismissMessageRequest.class));
        DismissMessageResponse expected = one(pojos(DismissMessageResponse.class));
        when(dismissMessageOperation.process(request)).thenReturn(expected);

        DismissMessageResponse response = instance.dismissMessage(request);
        assertThat(response, is(sameInstance(response)));
    }

    @DontRepeat
    @Test
    public void testDismissMessageWhenThrows() throws Exception
    {
        when(dismissMessageOperation.process(Mockito.any()))
            .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.dismissMessage(new DismissMessageRequest()))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testUpdateApplication() throws Exception
    {
        UpdateApplicationRequest request = one(pojos(UpdateApplicationRequest.class));
        UpdateApplicationResponse expected = one(pojos(UpdateApplicationResponse.class));
        when(updateApplicationOperation.process(request)).thenReturn(expected);
        
        UpdateApplicationResponse response = instance.updateApplication(request);
        assertThat(response, is(expected));
        verify(updateApplicationOperation).process(request);
    }
    
    @Test
    public void testUpdateApplicationWhenFails() throws Exception
    {
        when(updateApplicationOperation.process(Mockito.any()))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.updateApplication(new UpdateApplicationRequest()))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testUnfollowApplication() throws Exception
    {
        UnfollowApplicationRequest request = one(pojos(UnfollowApplicationRequest.class));
        UnfollowApplicationResponse expected = one(pojos(UnfollowApplicationResponse.class));
        when(unfollowApplicationOperation.process(request)).thenReturn(expected);
        
        UnfollowApplicationResponse response = instance.unfollowApplication(request);
        assertThat(response, is(expected));
        
        verify(unfollowApplicationOperation).process(request);
    }

    @Test
    public void testUnfollowApplicationWhenFails() throws Exception
    {
        when(unfollowApplicationOperation.process(Mockito.any()))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.unfollowApplication(new UnfollowApplicationRequest()))
            .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testUpdateReactions() throws Exception
    {
        UpdateReactionsRequest request = new UpdateReactionsRequest().setToken(token);
        UpdateReactionsResponse expected = new UpdateReactionsResponse().setReactions(listOf(reactions(), 3));
        when(updateReactionsOperation.process(request)).thenReturn(expected);
        
        UpdateReactionsResponse response = instance.updateReactions(request);
        assertThat(response, is(expected));
    }

    @Test
    public void testUpdateReactionsWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.updateReactions(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testUpdateReactionsWhenFails() throws Exception
    {
        UpdateReactionsRequest request = new UpdateReactionsRequest().setToken(token);
        
        when(updateReactionsOperation.process(request))
            .thenThrow(new DoesNotExistException());
        
        assertThrows(() -> instance.updateReactions(request))
            .isInstanceOf(DoesNotExistException.class);
    }

    @Test
    public void testGetReactions() throws Exception
    {
        GetReactionsRequest request = new GetReactionsRequest(token);
        GetReactionsResponse expected = new GetReactionsResponse(listOf(reactions(), 4));
        when(getReactionsOperation.process(request)).thenReturn(expected);
        
        GetReactionsResponse response = instance.getReactions(request);
        assertThat(response, is(expected));
    }

    @Test
    public void testGetReactionsWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getReactions(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testGetReactionsWhenFails() throws Exception
    {
        GetReactionsRequest request = new GetReactionsRequest().setToken(token);
        when(getReactionsOperation.process(request))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.getReactions(request))
            .isInstanceOf(OperationFailedException.class);
    }

 
}
