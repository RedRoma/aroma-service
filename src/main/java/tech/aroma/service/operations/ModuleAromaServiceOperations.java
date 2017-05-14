 /*
  * Copyright 2017 RedRoma, Inc.
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

 import java.util.function.Function;

 import com.google.inject.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import tech.aroma.thrift.authentication.*;
 import tech.aroma.thrift.functions.TokenFunctions;
 import tech.aroma.thrift.service.*;
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
        
        bind(new TypeLiteral<ThriftOperation<DeleteApplicationRequest, DeleteApplicationResponse>>(){})
            .to(DeleteApplicationOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<DeleteMessageRequest, DeleteMessageResponse>>(){})
            .to(DeleteMessageOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<DismissMessageRequest, DismissMessageResponse>>(){})
            .to(DismissMessageOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<FollowApplicationRequest, FollowApplicationResponse>>(){})
            .to(FollowApplicationOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse>>(){})
            .to(ProvisionApplicationOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse>>(){})
            .to(RenewApplicationTokenOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<RegisterHealthCheckRequest, RegisterHealthCheckResponse>>(){})
            .to(RegisterHealthCheckOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<RecreateApplicationTokenRequest, RecreateApplicationTokenResponse>>(){})
            .to(RecreateApplicationTokenOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<SearchForApplicationsRequest, SearchForApplicationsResponse>>(){})
            .to(SearchForApplicationsOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<SignInRequest, SignInResponse>>(){})
            .to(SignInOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<SignUpRequest, SignUpResponse>>(){})
            .to(SignUpOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<UnfollowApplicationRequest, UnfollowApplicationResponse>>(){})
            .to(UnfollowApplicationOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<UpdateApplicationRequest, UpdateApplicationResponse>>(){})
            .to(UpdateApplicationOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<UpdateReactionsRequest, UpdateReactionsResponse>>(){})
            .to(UpdateReactionsOperation.class);
        
        
        
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
        
        bind(new TypeLiteral<ThriftOperation<GetApplicationsFollowedByRequest, GetApplicationsFollowedByResponse>>(){})
            .to(GetApplicationsFollowedByOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetApplicationsOwnedByRequest, GetApplicationsOwnedByResponse>>(){})
            .to(GetApplicationsOwnedByOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetReactionsRequest, GetReactionsResponse>>(){})
            .to(GetReactionsOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetUserInfoRequest, GetUserInfoResponse>>(){})
            .to(GetUserInfoOperation.class);
  
        
        //DEVICE REGISTRATION OPERATIONS
        //=========================================
                
        bind(new TypeLiteral<ThriftOperation<CheckIfDeviceIsRegisteredRequest, CheckIfDeviceIsRegisteredResponse>>(){})
            .to(CheckIfDeviceIsRegisteredOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetRegisteredDevicesRequest, GetRegisteredDevicesResponse>>(){})
            .to(GetRegisteredDevicesOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<RegisterDeviceRequest, RegisterDeviceResponse>>(){})
            .to(RegisterDeviceOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<UnregisterDeviceRequest, UnregisterDeviceResponse>>(){})
            .to(UnregisterDeviceOperation.class);

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
