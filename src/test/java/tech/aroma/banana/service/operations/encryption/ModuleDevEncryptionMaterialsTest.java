/*
 * Copyright 2016 Aroma Tech.
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

package tech.aroma.banana.service.operations.encryption;

import org.jasypt.digest.config.DigesterConfig;
import org.jasypt.digest.config.SimpleStringDigesterConfig;
import org.jasypt.salt.SaltGenerator;
import org.jasypt.util.password.PasswordEncryptor;
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
public class ModuleDevEncryptionMaterialsTest 
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
        
    }

    @Test
    public void testProvidePasswordConfig()
    {
        DigesterConfig result = instance.providePasswordConfig();
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvidePasswordEncryptor()
    {
        DigesterConfig config = instance.providePasswordConfig();
        PasswordEncryptor result = instance.providePasswordEncryptor(config);
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideSaltGenerator()
    {
        SaltGenerator result = instance.provideSaltGenerator();
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideStringDigesterConfig()
    {
        SaltGenerator salt = instance.provideSaltGenerator();
        SimpleStringDigesterConfig result = instance.provideStringDigesterConfig(salt);
        assertThat(result, notNullValue());
    }

    @Test
    public void testProvideIdentityHashingFunction()
    {
    }

    @Test
    public void testProvideOverTheWireDecryptor()
    {
    }

}