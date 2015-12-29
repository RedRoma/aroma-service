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
import com.google.inject.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.service.GetActivityRequest;
import tech.aroma.banana.thrift.service.GetActivityResponse;
import tech.aroma.banana.thrift.service.GetApplicationInfoRequest;
import tech.aroma.banana.thrift.service.GetApplicationInfoResponse;
import tech.aroma.banana.thrift.service.GetDashboardRequest;
import tech.aroma.banana.thrift.service.GetDashboardResponse;
import tech.aroma.banana.thrift.service.GetFullMessageRequest;
import tech.aroma.banana.thrift.service.GetFullMessageResponse;
import tech.aroma.banana.thrift.service.GetMyApplicationsRequest;
import tech.aroma.banana.thrift.service.GetMyApplicationsResponse;
import tech.aroma.banana.thrift.service.GetMySavedChannelsRequest;
import tech.aroma.banana.thrift.service.GetMySavedChannelsResponse;
import tech.aroma.banana.thrift.service.ProvisionApplicationRequest;
import tech.aroma.banana.thrift.service.ProvisionApplicationResponse;
import tech.aroma.banana.thrift.service.SignInRequest;
import tech.aroma.banana.thrift.service.SignInResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

/**
 * This Module defines the bindings for the implementations of the
 * Banana Service Operations.
 * 
 * @author SirWellington
 */
public final class BananaServiceOperationsModule extends AbstractModule
{
    private final static Logger LOG = LoggerFactory.getLogger(BananaServiceOperationsModule.class);

    @Override
    protected void configure()
    {
        //SERVICE OPERATIONS
        
        bind(new TypeLiteral<ThriftOperation<GetDashboardRequest, GetDashboardResponse>>() {})
            .to(GetDashboardOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetMySavedChannelsRequest, GetMySavedChannelsResponse>>(){})
            .to(GetMySavedChannelsOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetMyApplicationsRequest, GetMyApplicationsResponse>>(){})
            .to(GetMyApplicationsOperation.class);
                
        bind(new TypeLiteral<ThriftOperation<ProvisionApplicationRequest, ProvisionApplicationResponse>>(){})
            .to(ProvisionApplicationOperation.class);
                
        bind(new TypeLiteral<ThriftOperation<SignInRequest, SignInResponse>>(){})
            .to(SignInOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse>>() {})
            .to(GetApplicationInfoOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<GetActivityRequest, GetActivityResponse>>() {})
            .to(GetActivityOperation.class);
        
        
        bind(new TypeLiteral<ThriftOperation<GetFullMessageRequest, GetFullMessageResponse>>() {})
            .to(GetFullMessageOperation.class);
        
    }

}
