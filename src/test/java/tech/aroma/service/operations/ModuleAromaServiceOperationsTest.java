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

import java.util.Set;
import java.util.function.Function;

import com.google.inject.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.memory.ModuleMemoryDataRepositories;
import tech.aroma.service.AromaAnnotations;
import tech.aroma.service.operations.encryption.ModuleEncryptionMaterialsDev;
import tech.aroma.thrift.authentication.*;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.email.service.EmailService;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class ModuleAromaServiceOperationsTest
{
    private ModuleEncryptionMaterialsDev encryptionMaterials;
    private ModuleMemoryDataRepositories dataModule;
    private ModuleAromaServiceOperations instance;
    
    private final Module mockDependencies = new AbstractModule()
    {
        @Override
        protected void configure()
        {
            bind(AuthenticationService.Iface.class)
                .toInstance(mock(AuthenticationService.Iface.class));
            
            bind(EmailService.Iface.class)
                .toInstance(mock(EmailService.Iface.class));
        }
        
        @Provides
        @AromaAnnotations.SuperUsers
        Set<String> provideSuperUsers()
        {
            return Sets.emptySet();
        }
        
    };
    
    
    @Before
    public void setUp()
    {
        dataModule = new ModuleMemoryDataRepositories();
        encryptionMaterials = new ModuleEncryptionMaterialsDev();
        instance = new ModuleAromaServiceOperations();
    }

    @Test
    public void testConfigure()
    {
        Injector injector = Guice.createInjector(dataModule, encryptionMaterials, mockDependencies, instance);
        
    }

    @Test
    public void testProvideAuthToUserTokenMapper()
    {
        Function<AuthenticationToken, UserToken> result = instance.provideAuthToUserTokenMapper();
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideUserToAuthTokenMapper()
    {
        Function<UserToken, AuthenticationToken> result = instance.provideUserToAuthTokenMapper();
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideAuthToAppTokenMapper()
    {
        Function<AuthenticationToken, ApplicationToken> result = instance.provideAuthToAppTokenMapper();
        assertThat(result, notNullValue());
    }

}
