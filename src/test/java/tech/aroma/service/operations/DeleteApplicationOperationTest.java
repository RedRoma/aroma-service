/*
 * Copyright 2016 RedRoma.
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
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.service.DeleteApplicationRequest;
import tech.aroma.thrift.service.DeleteApplicationResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class DeleteApplicationOperationTest
{

    @Mock
    private ApplicationRepository appRepo;
    
    @Mock
    private MediaRepository mediaRepo;
    
    @Mock
    private UserRepository userRepo;

    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String userId;
    
    @GeneratePojo
    private Application app;
    
    @GeneratePojo
    private DeleteApplicationRequest request;
    
    private DeleteApplicationOperation instance;
    
    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
        
        instance = new DeleteApplicationOperation(appRepo, mediaRepo, userRepo);
    }

    private void setupData() throws Exception
    {
        app.applicationId = appId;
        app.owners.add(userId);
        
        request.applicationId = appId;
        request.token.userId = userId;
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getById(appId)).thenReturn(app);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new DeleteApplicationOperation(null, mediaRepo, userRepo));
        assertThrows(() -> new DeleteApplicationOperation(appRepo, null, userRepo));
        assertThrows(() -> new DeleteApplicationOperation(appRepo, mediaRepo, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        
        DeleteApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(appRepo).deleteApplication(appId);
        
        verify(mediaRepo).deleteMedia(app.applicationIconMediaId);
        verify(mediaRepo).deleteAllThumbnails(app.applicationIconMediaId);
        
        verify(mediaRepo).deleteMedia(appId);
        verify(mediaRepo).deleteAllThumbnails(appId);
    }

}
