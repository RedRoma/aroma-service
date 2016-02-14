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


package tech.aroma.banana.service.operations;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.authentication.ApplicationToken;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.functions.TokenFunctions;
import tech.aroma.banana.thrift.service.DeleteMessageRequest;
import tech.aroma.banana.thrift.service.DeleteMessageResponse;
import tech.aroma.banana.thrift.service.DismissMessageRequest;
import tech.aroma.banana.thrift.service.DismissMessageResponse;
import tech.aroma.banana.thrift.service.FollowApplicationRequest;
import tech.aroma.banana.thrift.service.FollowApplicationResponse;
import tech.aroma.banana.thrift.service.GetActivityRequest;
import tech.aroma.banana.thrift.service.GetActivityResponse;
import tech.aroma.banana.thrift.service.GetApplicationInfoRequest;
import tech.aroma.banana.thrift.service.GetApplicationInfoResponse;
import tech.aroma.banana.thrift.service.GetApplicationMessagesRequest;
import tech.aroma.banana.thrift.service.GetApplicationMessagesResponse;
import tech.aroma.banana.thrift.service.GetBuzzRequest;
import tech.aroma.banana.thrift.service.GetBuzzResponse;
import tech.aroma.banana.thrift.service.GetDashboardRequest;
import tech.aroma.banana.thrift.service.GetDashboardResponse;
import tech.aroma.banana.thrift.service.GetFullMessageRequest;
import tech.aroma.banana.thrift.service.GetFullMessageResponse;
import tech.aroma.banana.thrift.service.GetInboxRequest;
import tech.aroma.banana.thrift.service.GetInboxResponse;
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
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

/**
 * This Module defines the bindings for the implementations of the
 * Banana Service Operations.
 *
 * @author SirWellington
 */
public final class ModuleBananaServiceOperations extends AbstractModule
{
    private final static Logger LOG = LoggerFactory.getLogger(ModuleBananaServiceOperations.class);
    
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
        
        bind(new TypeLiteral<ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse>>(){})
            .to(GetMySavedChannelsOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetMyApplicationsRequest, GetMyApplicationsResponse>>(){})
            .to(GetMyApplicationsOperation.class);
        
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
