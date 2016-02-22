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

import org.apache.thrift.TException;
import org.jasypt.util.password.PasswordEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.thrift.exceptions.InvalidCredentialsException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.HEXADECIMAL;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class AromaPasswordEncryptorImplTest 
{
    
    @Mock
    private PasswordEncryptor encryptor;
    
    @GenerateString(ALPHABETIC)
    private String password;
    
    @GenerateString(HEXADECIMAL)
    private String encrypted;

    private AromaPasswordEncryptorImpl instance;
    
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {
        when(encryptor.encryptPassword(password))
            .thenReturn(encrypted);
        
        when(encryptor.checkPassword(password, encrypted))
            .thenReturn(true);
    }

    private void setupMocks() throws Exception
    {
        instance = new AromaPasswordEncryptorImpl(encryptor);
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new AromaPasswordEncryptorImpl(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testEncryptPassword() throws Exception
    {
        String result = instance.encryptPassword(password);
        assertThat(result, is(encrypted));
    }
    
    @DontRepeat
    @Test
    public void testEncryptPasswordWhenFails() throws Exception
    {
        when(encryptor.encryptPassword(password))
            .thenThrow(new RuntimeException());
        
        assertThrows(() -> instance.encryptPassword(password))
            .isInstanceOf(TException.class);
    }
    
    @DontRepeat
    @Test
    public void testEncryptPasswordWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.encryptPassword(null))
            .isInstanceOf(InvalidCredentialsException.class);
        
        assertThrows(() -> instance.encryptPassword(""))
            .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    public void testMatch() throws Exception
    {
        boolean result = instance.match(password, encrypted);
        assertThat(result, is(true));
    }
    
    @Test
    public void testMatchWhenDontMatch() throws Exception
    {
        when(encryptor.checkPassword(password, encrypted))
            .thenReturn(false);
        
        boolean result = instance.match(password, encrypted);
        assertThat(result, is(false));
    }
    
    @DontRepeat
    @Test
    public void testMatchWhenFails() throws Exception
    {
        when(encryptor.checkPassword(password, encrypted))
            .thenThrow(new RuntimeException());
        
        assertThrows(() -> instance.match(password, encrypted))
            .isInstanceOf(TException.class);
    }

}
