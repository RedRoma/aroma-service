/*
 * Copyright 2016 RedRoma, Inc.
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

package tech.aroma.service.operations.encryption;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.salt.SaltGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class ModuleEncryptionMaterialsDevTest 
{
    
    private ModuleEncryptionMaterialsDev instance;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {
        instance = new ModuleEncryptionMaterialsDev();
    }

    @Test
    public void testConfigure()
    {
        Injector injector = Guice.createInjector(instance);
        AromaPasswordEncryptor encryptor = injector.getInstance(AromaPasswordEncryptor.class);
        assertThat(encryptor, notNullValue());
        
        OverTheWireDecryptor decryptor = injector.getInstance(OverTheWireDecryptor.class);
        assertThat(decryptor, notNullValue());
    }

    @Test
    public void testProvideSaltGenerator()
    {
        SaltGenerator result = instance.provideSaltGenerator();
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideOverTheWireDecryptor()
    {
        PBEStringEncryptor result = instance.provideOverTheWireDecryptor();
        assertThat(result, notNullValue());
    }

}
