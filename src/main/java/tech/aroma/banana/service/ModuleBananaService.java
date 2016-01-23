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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Singleton;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.service.BananaService;
import tech.sirwellington.alchemy.http.AlchemyHttp;

/**
 *
 * @author SirWellington
 */
public class ModuleBananaService extends AbstractModule
{

    private final static Logger LOG = LoggerFactory.getLogger(ModuleBananaService.class);
    
    @Override
    protected void configure()
    {
        bind(BananaService.Iface.class).to(BananaServiceImpl.class);
        
        bind(ExecutorService.class).toInstance(Executors.newWorkStealingPool(15));
    }
    
    @Singleton
    AlchemyHttp provideAlchemyHttpClient()
    {
        HttpClient apacheHttpClient = HttpClientBuilder.create()
            .build();

        return AlchemyHttp.newBuilder()
            .usingApacheHttpClient(apacheHttpClient)
            .enableAsyncCallbacks()
            .build();
    }
    
}
