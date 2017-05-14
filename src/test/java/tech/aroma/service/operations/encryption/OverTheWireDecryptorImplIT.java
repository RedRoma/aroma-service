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

package tech.aroma.service.operations.encryption;

import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class OverTheWireDecryptorImplIT 
{
    
    private PBEStringEncryptor decryptor;
    
    private OverTheWireDecryptorImpl instance; 
    
    @GenerateString(HEXADECIMAL)
    private String rawMessage;
    
    private String encodedMessage;
    
    @Before
    public void setUp() throws Exception
    {
        setupObjects();
        setupData();
    }

    private void setupData() throws Exception
    {
        
        encodedMessage = decryptor.encrypt(rawMessage);
    }

    private void setupObjects() throws Exception
    {
        decryptor = TestMaterials.newStringEncryptor();
        instance = new OverTheWireDecryptorImpl(decryptor);
    }

    @Test
    public void testDecrypt() throws Exception
    {
        String result = instance.decrypt(encodedMessage);
        assertThat(result, is(rawMessage));
    }
    
    @Test
    public void testDecryptConsistency() throws Exception
    {
        int iterations = one(integers(10, 100));
        
        for(int i = 0; i < iterations; ++i)
        {
            recreateInstance();
            
            String result = instance.decrypt(encodedMessage);
            assertThat(result, is(rawMessage));
        }
    }

    private void recreateInstance()
    {
        this.decryptor = TestMaterials.newStringEncryptor();
        this.instance = new OverTheWireDecryptorImpl(decryptor);
    }

}
