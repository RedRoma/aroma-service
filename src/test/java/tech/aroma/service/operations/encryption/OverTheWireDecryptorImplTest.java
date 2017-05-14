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

import org.apache.thrift.TException;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.thrift.exceptions.InvalidCredentialsException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class OverTheWireDecryptorImplTest 
{

    @Mock
    private PBEStringEncryptor decryptor;
    
    private OverTheWireDecryptorImpl instance;
    
    @GenerateString(ALPHABETIC)
    private String message;
    
    @GenerateString(HEXADECIMAL)
    private String encrypted;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {
        instance = new OverTheWireDecryptorImpl(decryptor);
    }

    private void setupMocks() throws Exception
    {
        when(decryptor.decrypt(encrypted))
            .thenReturn(message);
    }

    @Test
    public void testDecrypt() throws Exception
    {
        String result = instance.decrypt(encrypted);
        assertThat(result, is(message));
    }
    
    @DontRepeat
    @Test
    public void testDecryptWhenFails() throws Exception
    {
        when(decryptor.decrypt(encrypted))
            .thenThrow(new RuntimeException());
        
        assertThrows(() -> instance.decrypt(encrypted))
            .isInstanceOf(TException.class);
    }
    
    @DontRepeat
    @Test
    public void testDecryptWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.decrypt(null))
            .isInstanceOf(InvalidCredentialsException.class);
        
        assertThrows(() -> instance.decrypt(""))
            .isInstanceOf(InvalidCredentialsException.class);
    }

}
