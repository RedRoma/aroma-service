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

package tech.aroma.service;

import java.util.Set;

import com.google.inject.*;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.memory.ModuleMemoryDataRepositories;
import tech.aroma.service.operations.encryption.ModuleEncryptionMaterialsDev;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.email.service.EmailService;
import tech.aroma.thrift.service.AromaService;
import tech.aroma.thrift.services.NoOpEmailService;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * This Test Class can be considered an Integration level test, because it tests the validity of
 * the Dependency Injection Framework and Object Graph.
 * 
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class ModuleAromaServiceTest 
{
    
    private ModuleMemoryDataRepositories dataModule;
    private ModuleEncryptionMaterialsDev encryptionModule;
    private ModuleAromaService instance;
    
    @Before
    public void setUp()
    {
        encryptionModule = new ModuleEncryptionMaterialsDev();
        dataModule = new ModuleMemoryDataRepositories();
        instance = new ModuleAromaService();
    }

    @Test
    public void testConfigure() throws TException
    {
        Injector injector = Guice.createInjector(dataModule,
                                                 encryptionModule,
                                                 instance,
                                                 restOfDependencies);

        assertThat(injector, notNullValue());
        
        AromaService.Iface service = injector.getInstance(AromaService.Iface.class);
        assertThat(service, notNullValue());
        service.getApiVersion();
    }

    @Test
    public void testProvideAlchemyHttpClient()
    {
        AlchemyHttp result = instance.provideAlchemyHttpClient();
        assertThat(result, notNullValue());
    }
    
    private final Module restOfDependencies = new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind(AuthenticationService.Iface.class)
                .toInstance(mock(AuthenticationService.Iface.class));
            
            bind(EmailService.Iface.class)
                .toInstance(NoOpEmailService.newInstance());
        }
        
        @Provides
        @AromaAnnotations.SuperUsers
        Set<String> provideSuperUsers()
        {
            return Sets.emptySet();
        }
    };

}
