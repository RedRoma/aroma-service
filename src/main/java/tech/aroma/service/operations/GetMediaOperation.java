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

 
package tech.aroma.service.operations;


import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.MediaRepository;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetMediaRequest;
import tech.aroma.thrift.service.GetMediaResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
final class GetMediaOperation implements ThriftOperation<GetMediaRequest, GetMediaResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetMediaOperation.class);
    
    private final MediaRepository mediaRepo;

    @Inject
    GetMediaOperation(MediaRepository mediaRepo)
    {
        checkThat(mediaRepo).is(notNull());
        
        this.mediaRepo = mediaRepo;
    }

    @Override
    public GetMediaResponse process(GetMediaRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        String mediaId = request.mediaId;
        Image image = mediaRepo.getMedia(mediaId);
        
        return new GetMediaResponse(image);
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
            
            if(request.isSetDesiredThumbnailSize())
            {
                checkThat(request.desiredThumbnailSize.width, request.desiredThumbnailSize.height)
                    .usingMessage("Thumbnail dimensions are off")
                    .are(greaterThan(0));
            }
        };
    }

}
