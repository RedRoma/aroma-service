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

package tech.aroma.service.operations.thumbnails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.thrift.*;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.aroma.thrift.generators.UserGenerators.usersWithProfileImages;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class ThumbnailCreatorImplTest
{

    private User user;

    private Image image;

    @GeneratePojo
    private Dimension thumbnailSize;

    private ThumbnailCreatorImpl instance;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();

        instance = new ThumbnailCreatorImpl();
    }

    private void setupData() throws Exception
    {
        user = one(usersWithProfileImages());
        image = user.profileImage;

    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testCreateThumbnail() throws Exception
    {
        Image thumbnail = instance.createThumbnail(image, thumbnailSize);
        assertThat(thumbnail, notNullValue());
        assertThat(thumbnail.data, notNullValue());
        assertThat(thumbnail.dimension, is(thumbnailSize));
    }

    @DontRepeat
    @Test
    public void testWithBadArgs()
    {
        assertThrows(() -> instance.createThumbnail(image, null))
                .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.createThumbnail(null, thumbnailSize))
                .isInstanceOf(InvalidArgumentException.class);

        Dimension badDimension = new Dimension().setHeight(0).setWidth(0);
        assertThrows(() -> instance.createThumbnail(image, badDimension))
                .isInstanceOf(InvalidArgumentException.class);

        Image badImage = new Image(image);
        badImage.unsetData();

        assertThrows(() -> instance.createThumbnail(badImage, thumbnailSize))
                .isInstanceOf(InvalidArgumentException.class);
    }

}