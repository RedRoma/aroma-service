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


package tech.aroma.service.operations;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.authentication.ApplicationToken;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.functions.TokenFunctions;
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
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

/**
 * This Module defines the bindings for the implementations of the
 * Aroma Service Operations.
 *
 * @author SirWellington
 */
public final class ModuleAromaServiceOperations extends AbstractModule
{
    private final static Logger LOG = LoggerFactory.getLogger(ModuleAromaServiceOperations.class);
    
    @Override
    protected void configure()
    {
        //SERVICE OPERATIONS
        
        //ACTIONS AND SAVE OPERATIONS
        //=========================================
        
        bind(new TypeLiteral<ThriftOperation<DeleteMessageRequest, DeleteMessageResponse>>(){})
            .to(DeleteMessageOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<DismissMessageRequest, DismissMessageResponse>>(){})
            .to(DismissMessageOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<FollowApplicationRequest, FollowApplicationResponse>>(){})
            .to(FollowApplicationOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse>>(){})
            .to(ProvisionApplicationOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<RegenerateApplicationTokenRequest, RegenerateApplicationTokenResponse>>(){})
            .to(RegenerateApplicationTokenOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse>>(){})
            .to(RegisterHealthCheckOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<RemoveSavedChannelRequest, RemoveSavedChannelResponse>>(){})
            .to(RemoveSavedChannelOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse>>(){})
            .to(RenewApplicationTokenOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<SaveChannelRequest, SaveChannelResponse>>(){})
            .to(SaveChannelOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<SearchForApplicationsRequest, SearchForApplicationsResponse>>(){})
            .to(SearchForApplicationsOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<SignInRequest, SignInResponse>>(){})
            .to(SignInOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<SignUpRequest, SignUpResponse>>(){})
            .to(SignUpOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<SnoozeChannelRequest, SnoozeChannelResponse>>(){})
            .to(SnoozeChannelOperation.class);
        
        
        bind(new TypeLiteral<ThriftOperation<UnfollowApplicationRequest, UnfollowApplicationResponse>>(){})
            .to(UnfollowApplicationOperation.class);
        
        
        bind(new TypeLiteral<ThriftOperation<UpdateApplicationRequest, UpdateApplicationResponse>>(){})
            .to(UpdateApplicationOperation.class);
        
        
        //QUERY OPERATIONS
        //=========================================
        
        bind(new TypeLiteral<ThriftOperation<GetActivityRequest, GetActivityResponse>>() {})
            .to(GetActivityOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse>>() {})
            .to(GetApplicationInfoOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetApplicationMessagesRequest, GetApplicationMessagesResponse>>() {})
            .to(GetApplicationMessagesOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetBuzzRequest, GetBuzzResponse>>() {})
            .to(GetBuzzOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetDashboardRequest, GetDashboardResponse>>() {})
            .to(GetDashboardOperation.class);
                
        bind(new TypeLiteral<ThriftOperation<GetFullMessageRequest, GetFullMessageResponse>>() {})
            .to(GetFullMessageOperation.class);
              
        bind(new TypeLiteral<ThriftOperation<GetInboxRequest, GetInboxResponse>>() {})
            .to(GetInboxOperation.class);
        
              
        bind(new TypeLiteral<ThriftOperation<GetMediaRequest, GetMediaResponse>>() {})
            .to(GetMediaOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse>>(){})
            .to(GetMySavedChannelsOperation.class);
        
        
        bind(new TypeLiteral<ThriftOperation<GetApplicationsFollowedByRequest, GetApplicationsFollowedByResponse>>(){})
            .to(GetApplicationsFollowedByOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetApplicationsOwnedByRequest, GetApplicationsOwnedByResponse>>(){})
            .to(GetApplicationsOwnedByOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetUserInfoRequest, GetUserInfoResponse>>(){})
            .to(GetUserInfoOperation.class);
  
    }
    
    @Provides
    Function<AuthenticationToken, UserToken> provideAuthToUserTokenMapper()
    {
        return TokenFunctions.authTokenToUserTokenFunction();
    }
    
    @Provides
    Function<UserToken, AuthenticationToken> provideUserToAuthTokenMapper()
    {
        return TokenFunctions.userTokenToAuthTokenFunction();
    }
    
    @Provides
    Function<AuthenticationToken, ApplicationToken> provideAuthToAppTokenMapper()
    {
        return TokenFunctions.authTokenToAppTokenFunction();
    }
}
