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

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import javax.inject.Singleton;
import org.apache.thrift.TBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.service.operations.ProvisionServiceOperation;
import tech.aroma.banana.service.operations.SignInOperation;
import tech.aroma.banana.thrift.service.BananaService;
import tech.aroma.banana.thrift.service.ProvisionServiceRequest;
import tech.aroma.banana.thrift.service.ProvisionServiceResponse;
import tech.aroma.banana.thrift.service.SignInRequest;
import tech.aroma.banana.thrift.service.SignInResponse;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

/**
 *
 * @author SirWellington
 */
public class BananaServiceModule extends AbstractModule
{

    private final static Logger LOG = LoggerFactory.getLogger(BananaServiceModule.class);
    
    @Override
    protected void configure()
    {
        bind(BananaService.Iface.class).to(BananaServiceImpl.class);

        //Service Operations
        bind(new TypeLiteral<ThriftOperation<SignInRequest, SignInResponse>>(){})
            .to(SignInOperation.class);
        
        bind(new TypeLiteral<ThriftOperation<ProvisionServiceRequest, ProvisionServiceResponse>>(){})
            .to(ProvisionServiceOperation.class);
    }
    
    private static <Request extends TBase, Response extends TBase> TypeLiteral<ThriftOperation<Request, Response>> operationType(Class<Request> requestClass,
                                                                                                                                  Class<Response> responseClass)
    {
        return new TypeLiteral<ThriftOperation<Request, Response>>()
        {
        };
    }
    
    
    @Singleton
    AlchemyHttp provideAlchemyHttpClient()
    {
        return AlchemyHttp.newBuilder()
            .enableAsyncCallbacks()
            .build();
    }
    
}
