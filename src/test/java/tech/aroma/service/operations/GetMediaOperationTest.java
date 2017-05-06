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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.MediaRepository;
import tech.aroma.service.operations.thumbnails.ThumbnailCreator;
import tech.aroma.thrift.Dimension;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.service.GetMediaRequest;
import tech.aroma.thrift.service.GetMediaResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class GetMediaOperationTest
{

    @Mock
    private MediaRepository mediaRepo;
    
    @Mock
    private ThumbnailCreator thumbnailCreator;

    @GenerateString(UUID)
    private String mediaId;

    @GenerateString(ALPHABETIC)
    private String badId;

    @GeneratePojo
    private Image image;
    
    @GeneratePojo
    private Image thumbnail;

    @GeneratePojo
    private GetMediaRequest request;
    
    @GeneratePojo
    private Dimension thumbnailSize;
    
    private GetMediaOperation instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new GetMediaOperation(mediaRepo, thumbnailCreator);
        verifyZeroInteractions(mediaRepo);
    }

    private void setupData() throws Exception
    {
        request.mediaId = mediaId;
        request.unsetDesiredThumbnailSize();
    }

    private void setupMocks() throws Exception
    {
        when(mediaRepo.getMedia(mediaId))
            .thenReturn(image);
        
        when(mediaRepo.getThumbnail(mediaId, thumbnailSize))
            .thenReturn(thumbnail);
        when(mediaRepo.containsThumbnail(mediaId, thumbnailSize))
            .thenReturn(true);
        
        when(thumbnailCreator.createThumbnail(image, thumbnailSize))
            .thenReturn(thumbnail);
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GetMediaOperation(null, thumbnailCreator));
        assertThrows(() -> new GetMediaOperation(mediaRepo, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        GetMediaResponse response = instance.process(request);
        assertThat(response, notNullValue());

        assertThat(response.image, is(image));

        verify(mediaRepo).getMedia(mediaId);
    }
    
    @DontRepeat
    @Test
    public void testWhenMediaDoesNotExist() throws Exception
    {
        when(mediaRepo.getMedia(mediaId))
            .thenThrow(new DoesNotExistException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(DoesNotExistException.class);
    }

    @Test
    public void testWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);

        GetMediaRequest emptyRequest = new GetMediaRequest();

        assertThrows(() -> instance.process(emptyRequest))
            .isInstanceOf(InvalidArgumentException.class);

        GetMediaRequest requestWithBadId = new GetMediaRequest(request)
            .setMediaId(badId);

        assertThrows(() -> instance.process(requestWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
    }

    
    @Test
    public void testWithBadThumbnailRequest() throws Exception
    {
        Dimension dimension = new Dimension();
        dimension.setHeight(one(negativeIntegers()))
            .setWidth(one(negativeIntegers()));
        
        request.setDesiredThumbnailSize(dimension);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
    }
    
    @Test
    public void testWhenThumbnailExists() throws Exception
    {
        request.setDesiredThumbnailSize(thumbnailSize);
        
        GetMediaResponse response = instance.process(request);
        assertThat(response.image, is(thumbnail));
        
        verify(mediaRepo).getThumbnail(mediaId, thumbnailSize);
    }
    
    @Test
    public void testWhenThumbnailDoesNotExist() throws Exception
    {
        request.setDesiredThumbnailSize(thumbnailSize);
        
        when(mediaRepo.containsThumbnail(mediaId, thumbnailSize)).thenReturn(false);
        when(mediaRepo.getThumbnail(mediaId, thumbnailSize))
            .thenThrow(new DoesNotExistException());
        
        GetMediaResponse response = instance.process(request);
        assertThat(response.image, is(thumbnail));
    }
    
    @Test
    public void testWhenThumbnailCreationFails() throws Exception
    {
        request.setDesiredThumbnailSize(thumbnailSize);

        when(mediaRepo.getThumbnail(mediaId, thumbnailSize))
            .thenThrow(new DoesNotExistException());
        when(thumbnailCreator.createThumbnail(image, thumbnailSize))
            .thenThrow(new OperationFailedException());
        
        GetMediaResponse response = instance.process(request);
        assertThat(response.image, is(image));
    }
    
    @Test
    public void testWhenSavingThumbnailFails() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(mediaRepo)
            .saveThumbnail(mediaId, thumbnailSize, thumbnail);

        request.setDesiredThumbnailSize(thumbnailSize);

        GetMediaResponse response = instance.process(request);
        assertThat(response.image, is(thumbnail));
    }
}
