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


import java.io.*;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.Antialiasing;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.*;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.trueStatement;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;

/**
 *
 * @author SirWellington
 */
final class ThumbnailCreatorImpl implements ThumbnailCreator
{
    private final static Logger LOG = LoggerFactory.getLogger(ThumbnailCreatorImpl.class);

    @Override
    public Image createThumbnail(Image originalImage, Dimension desiredSize) throws TException
    {
        
        checkThat(originalImage, desiredSize)
            .throwing(InvalidArgumentException.class)
            .usingMessage("missing arguments")
            .are(notNull());
        
        checkThat(desiredSize.width, desiredSize.height)
            .throwing(InvalidArgumentException.class)
            .usingMessage("Thumbnail dimensions must be > 0")
            .are(greaterThan(0));
        
        checkThat(originalImage.isSetData())
            .throwing(InvalidArgumentException.class)
            .usingMessage("original image must its data set")
            .is(trueStatement());
        
        try (ByteArrayInputStream istream = new ByteArrayInputStream(originalImage.getData());
             ByteArrayOutputStream ostream = new ByteArrayOutputStream();)
        {
            Thumbnails.of(istream)
                .antialiasing(Antialiasing.ON)
                .outputQuality(0.9)
                .height(desiredSize.height)
                .width(desiredSize.width)
                .useExifOrientation(true)
                .toOutputStream(ostream);

            Image thumbnail = new Image()
                .setData(ostream.toByteArray())
                .setDimension(desiredSize)
                .setImageType(ImageType.JPEG);

            LOG.info("Successfully created thumbnail of Size {}", desiredSize);

            return thumbnail;
        }
        catch (IOException ex)
        {
            LOG.error("Failed to create thumbnail for {} of size {}", originalImage, desiredSize, ex);
            throw new OperationFailedException("Could not create thumbnail for " + originalImage + " of size " + desiredSize);
        }
    }

}
