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

package tech.aroma.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.ProvisionApplicationRequest;
import tech.aroma.thrift.service.SignInRequest;
import tech.sirwellington.alchemy.arguments.ExceptionMapper;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphanumericString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class AromaAssertionsTest
{

    @Before
    public void setUp()
    {
    }

    @DontRepeat
    @Test
    public void testCannotInstantiate()
    {
        assertThrows(() -> AromaAssertions.class.newInstance())
                .isInstanceOf(IllegalAccessException.class);
    }

    @Test
    public void testNotMissing()
    {
        SignInRequest request = pojos(SignInRequest.class).get();
        AromaAssertions.notMissing().check(request);

        assertThrows(() -> AromaAssertions.notMissing().check(null))
                .isInstanceOf(FailedAssertionException.class);
    }


    @Test
    public void testCheckNotNull() throws Exception
    {
        ProvisionApplicationRequest request = pojos(ProvisionApplicationRequest.class).get();
        AromaAssertions.checkNotNull(request);

        assertThrows(() -> AromaAssertions.checkNotNull(null))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testWithMessage()
    {
        String message = one(alphanumericString());

        ExceptionMapper<InvalidArgumentException> result = AromaAssertions.withMessage(message);
        assertThat(result, notNullValue());

        InvalidArgumentException ex = result.apply(null);
        assertThat(ex, notNullValue());
        assertThat(ex.getMessage(), is(message));
    }

}
