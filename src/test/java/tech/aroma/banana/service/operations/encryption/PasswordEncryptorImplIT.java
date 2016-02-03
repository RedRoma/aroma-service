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
import org.jasypt.digest.StringDigester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class PasswordEncryptorImplIT
{

    private StringDigester digester;

    private PasswordEncryptorImpl instance;

    @GenerateString(ALPHABETIC)
    private String rawPassword;

    private String digestedPassword;

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
        digester = TestMaterials.newDigester();

        instance = new PasswordEncryptorImpl(digester);
    }

    @Test
    public void testEncryptPassword() throws Exception
    {
        this.digestedPassword = instance.encryptPassword(rawPassword);

        assertThat(digestedPassword.isEmpty(), is(false));
        assertThat(digestedPassword, is(not(rawPassword)));

        assertThat(instance.match(rawPassword, digestedPassword), is(true));
    }

    @Test
    public void testMatch() throws Exception
    {
        this.digestedPassword = instance.encryptPassword(rawPassword);
        
        String otherPassword = one(alphabeticString());
        String otherDigest = instance.encryptPassword(otherPassword);

        boolean result = instance.match(rawPassword, otherDigest);
        assertThat(result, is(false));

        result = instance.match(otherPassword, digestedPassword);
        assertThat(result, is(false));

        result = instance.match(otherPassword, otherDigest);
        assertThat(result, is(true));
    }
    
    @Test
    public void testConsistencyAccrossRuns() throws TException
    {
        int iterations = one(integers(10, 100));
        
        digestedPassword = instance.encryptPassword(rawPassword);
        
        for(int i = 0; i < iterations; ++i)
        {
            recreateInstance();
            
            boolean matches = instance.match(rawPassword, digestedPassword);
            assertThat(matches, is(true));
        }
    }

    private void recreateInstance()
    {
        this.digester = TestMaterials.newDigester();
        this.instance = new PasswordEncryptorImpl(digester);
    }

}
