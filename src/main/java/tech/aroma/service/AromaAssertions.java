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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.ExceptionMapper;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * @author SirWellington
 */
@Internal
@NonInstantiable
public final class AromaAssertions
{
    private final static Logger LOG = LoggerFactory.getLogger(AromaAssertions.class);

    private AromaAssertions() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate");
    }

    public static <T> AlchemyAssertion<T> notMissing()
    {
        return a ->
        {
            checkThat(a)
                    .usingMessage("missing request")
                    .is(notNull());
        };
    }

    public static <T> void checkNotNull(T object) throws InvalidArgumentException
    {
        checkThat(object)
                .throwing(InvalidArgumentException.class)
                .is(notMissing());
    }

    public static ExceptionMapper<InvalidArgumentException> withMessage(String message)
    {
        return ex -> new InvalidArgumentException(message);
    }

}
