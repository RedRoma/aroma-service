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
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.events.EventType;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.time.Instant.now;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class AromaGeneratorsTest
{

    @Before
    public void setUp()
    {
    }

    @Test
    public void testEventTypes()
    {
        AlchemyGenerator<EventType> generator = AromaGenerators.eventTypes();
        assertThat(generator, notNullValue());
        assertThat(generator.get(), notNullValue());
    }

    @Test
    public void testEvents()
    {
        AlchemyGenerator<Event> generator = AromaGenerators.events();
        assertThat(generator, notNullValue());
        
        Event event = generator.get();
        assertThat(event, notNullValue());
        assertThat(event.timestamp, greaterThan(0L));

    }

    @Test
    public void testUsers()
    {
        AlchemyGenerator<User> generator = AromaGenerators.users();
        assertThat(generator, notNullValue());
        
        User user = generator.get();
        assertThat(user, notNullValue());
        assertThat(user.name, not(isEmptyString()));
        assertThat(user.email, not(isEmptyString()));
        assertThat(user.userId, not(isEmptyString()));
        assertThat(user.profileImage, notNullValue());
    }
    
    @Test
    public void testUsersWithoutProfileImages()
    {
        AlchemyGenerator<User> generator = AromaGenerators.usersWithoutProfileImages();
        assertThat(generator, notNullValue());
        
        User user = generator.get();
        assertThat(user, notNullValue());
        assertThat(user.name, not(isEmptyString()));
        assertThat(user.email, not(isEmptyString()));
        assertThat(user.userId, not(isEmptyString()));
        assertThat(user.profileImage, is(nullValue()));
    }

    @Test
    public void testProfileImages()
    {
        AlchemyGenerator<Image> generator = AromaGenerators.profileImages();
        assertThat(generator, notNullValue());
        
        Image image = generator.get();
        assertThat(image, notNullValue());
        assertThat(image.isSetData(), is(true));
    }

    @Test
    public void testPastTimes()
    {
        AlchemyGenerator<Long> generator = AromaGenerators.pastTimes();
        assertThat(generator, notNullValue());
        
        Long time = generator.get();
        Long now = now().toEpochMilli();
        assertThat(time, lessThan(now));
    }

    @Test
    public void testApplications()
    {
        AlchemyGenerator<Application> generator = AromaGenerators.applications();
        assertThat(generator, notNullValue());
 
        Application application = generator.get();
        assertThat(application, notNullValue());
        assertThat(application.applicationId, not(isEmptyOrNullString()));
        assertThat(application.name, not(isEmptyOrNullString()));
    }


}
