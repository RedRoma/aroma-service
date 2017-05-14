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

import org.jasypt.util.password.PasswordEncryptor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class AromaPasswordEncryptorTest
{

    @Mock
    private PasswordEncryptor encryptor;

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

    }

    @Test
    public void testNewInstance()
    {
        AromaPasswordEncryptor result = AromaPasswordEncryptor.newInstance(encryptor);
        assertThat(result, notNullValue());
    }

    @DontRepeat
    @Test
    public void testNewInstanceWithBadArgs()
    {
        assertThrows(() -> AromaPasswordEncryptor.newInstance(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

}
