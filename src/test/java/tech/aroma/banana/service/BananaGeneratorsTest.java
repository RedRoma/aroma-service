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

import java.nio.ByteBuffer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.events.Event;
import tech.aroma.banana.thrift.events.EventType;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class BananaGeneratorsTest
{

    @Before
    public void setUp()
    {
    }

    @Test
    public void testEventTypes()
    {
        AlchemyGenerator<EventType> generator = BananaGenerators.eventTypes();
        assertThat(generator, notNullValue());
        assertThat(generator.get(), notNullValue());
    }

    @Test
    public void testEvents()
    {
        AlchemyGenerator<Event> generator = BananaGenerators.events();
        assertThat(generator, notNullValue());
        assertThat(generator.get(), notNullValue());

    }

    @Test
    public void testUsers()
    {
        AlchemyGenerator<User> generator = BananaGenerators.users();
        assertThat(generator, notNullValue());
        assertThat(generator.get(), notNullValue());
    }

    @Test
    public void testProfileImages()
    {
        AlchemyGenerator<ByteBuffer> generator = BananaGenerators.profileImages();
        assertThat(generator, notNullValue());
        assertThat(generator.get(), notNullValue());
    }

}
