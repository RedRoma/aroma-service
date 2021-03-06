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


package tech.aroma.service.operations;


import javax.inject.Inject;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.MediaRepository;
import tech.aroma.service.operations.thumbnails.ThumbnailCreator;
import tech.aroma.thrift.Dimension;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetMediaRequest;
import tech.aroma.thrift.service.GetMediaResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 * @author SirWellington
 */
final class GetMediaOperation implements ThriftOperation<GetMediaRequest, GetMediaResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetMediaOperation.class);

    private final MediaRepository mediaRepo;
    private final ThumbnailCreator thumbnailCreator;

    @Inject
    GetMediaOperation(MediaRepository mediaRepo, ThumbnailCreator thumbnailCreator)
    {
        checkThat(mediaRepo, thumbnailCreator)
                .are(notNull());

        this.mediaRepo = mediaRepo;
        this.thumbnailCreator = thumbnailCreator;
    }

    @Override
    public GetMediaResponse process(GetMediaRequest request) throws TException
    {
        checkThat(request)
                .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
                .is(good());

        String mediaId = request.mediaId;

        if (request.isSetDesiredThumbnailSize() &&
                mediaRepo.containsThumbnail(mediaId, request.desiredThumbnailSize))
        {
            Image thumbnail = getThumbnail(mediaId, request.desiredThumbnailSize);

            if (thumbnail != null)
            {
                return new GetMediaResponse(thumbnail);
            }
        }

        Image image = mediaRepo.getMedia(mediaId);

        if (!request.isSetDesiredThumbnailSize())
        {
            return new GetMediaResponse(image);
        }

        Dimension thumbnailDimension = request.desiredThumbnailSize;

        Image thumbnail = tryToCreateThumbnailForImageOfSize(image, thumbnailDimension);

        if (thumbnail != null)
        {
            tryToSaveThumbnail(mediaId, thumbnail, thumbnailDimension);
            return new GetMediaResponse(thumbnail);
        }
        else
        {
            LOG.warn("Could not successfully produce a thumbnail, so returning full image");
            return new GetMediaResponse(image);
        }

    }

    private AlchemyAssertion<GetMediaRequest> good()
    {
        return request ->
        {
            checkThat(request)
                    .usingMessage("request is missing")
                    .is(notNull());

            checkThat(request.mediaId)
                    .usingMessage("request missing mediaId")
                    .is(nonEmptyString())
                    .usingMessage("mediaID must be a valid UUID")
                    .is(validUUID());

            if (request.isSetDesiredThumbnailSize())
            {
                checkThat(request.desiredThumbnailSize.width, request.desiredThumbnailSize.height)
                        .usingMessage("Thumbnail dimensions are off")
                        .are(greaterThan(0));
            }
        };
    }

    private Image getThumbnail(String mediaId, Dimension dimension) throws TException
    {
        try
        {
            return mediaRepo.getThumbnail(mediaId, dimension);
        }
        catch (DoesNotExistException ex)
        {
            return null;
        }
        catch (TException ex)
        {
            throw ex;
        }

    }

    private Image tryToCreateThumbnailForImageOfSize(Image image, Dimension thumbnailSize) throws TException
    {

        try
        {
            return thumbnailCreator.createThumbnail(image, thumbnailSize);
        }
        catch (TException ex)
        {
            LOG.warn("Could not generate a thumbnail for {} of size {}. Returning normail image.", image, thumbnailSize, ex);
            return null;
        }
    }

    private void tryToSaveThumbnail(String mediaId, Image thumbnail, Dimension thumbnailDimension)
    {
        try
        {
            mediaRepo.saveThumbnail(mediaId, thumbnailDimension, thumbnail);
        }
        catch (TException ex)
        {
            LOG.error("Failed to save Thumbnail for {} of size {}", mediaId, thumbnailDimension, ex);
        }
    }

}
