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
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.Provides;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import tech.aroma.banana.data.memory.ModuleMemoryDataRepositories;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class BananaServiceOperationsModuleTest
{

    private ModuleMemoryDataRepositories dataModule;
    private BananaServiceOperationsModule instance;
    
    
    private Module mockModule = new AbstractModule()
    {
        @Override
        protected void configure()
        {
        }
        
        @Provides
        AuthenticationService.Iface provideAuthenticationService()
        {
            return Mockito.mock(AuthenticationService.Iface.class);
        }
        
    };

    @Before
    public void setUp()
    {
        dataModule = new ModuleMemoryDataRepositories();
        instance = new BananaServiceOperationsModule();
    }

    @Test
    public void testConfigure()
    {
        Guice.createInjector(dataModule, mockModule, instance);
    }

    @Test
    public void testProvideTokenMapper()
    {
        Function<AuthenticationToken, UserToken> tokenMapper = instance.provideTokenMapper();
        assertThat(tokenMapper, notNullValue());
    }

}
