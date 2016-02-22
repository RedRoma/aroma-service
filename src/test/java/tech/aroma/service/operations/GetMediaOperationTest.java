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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.MediaRepository;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.DoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class GetMediaOperationTest
{

    @Mock
    private MediaRepository mediaRepo;

    @GenerateString(UUID)
    private String mediaId;

    @GenerateString(ALPHABETIC)
    private String badId;

    @GeneratePojo
    private Image image;

    @GeneratePojo
    private GetMediaRequest request;

    private GetMediaOperation instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new GetMediaOperation(mediaRepo);
        verifyZeroInteractions(mediaRepo);
    }

    private void setupData() throws Exception
    {
        request.mediaId = mediaId;
    }

    private void setupMocks() throws Exception
    {
        when(mediaRepo.getMedia(mediaId))
            .thenReturn(image);
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

}
