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

package tech.aroma.banana.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.service.ProvisionApplicationRequest;
import tech.aroma.banana.thrift.service.SendMessageRequest;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class BananaAssertionsTest 
{

    @Before
    public void setUp()
    {
    }
    
    @DontRepeat
    @Test
    public void testCannotInstantiate()
    {
        assertThrows(() -> BananaAssertions.class.newInstance())
            .isInstanceOf(IllegalAccessException.class);
    }

    @Test
    public void testNotMissing()
    {
        SendMessageRequest request = pojos(SendMessageRequest.class).get();
        BananaAssertions.notMissing().check(request);
        
        assertThrows(() -> BananaAssertions.notMissing().check(null))
            .isInstanceOf(FailedAssertionException.class);
    }

   
    @Test
    public void testCheckNotNull() throws Exception
    {
        ProvisionApplicationRequest request = pojos(ProvisionApplicationRequest.class).get();
        BananaAssertions.checkNotNull(request);
        
        assertThrows(() -> BananaAssertions.checkNotNull(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

}