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

package tech.aroma.banana.service.operations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.service.GetApplicationInfoRequest;
import tech.aroma.banana.thrift.service.GetApplicationInfoResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class GetApplicationInfoOperationTest 
{
    @Mock
    private ApplicationRepository appRepo;
    
    @GeneratePojo
    private GetApplicationInfoRequest request;
    
    @GeneratePojo
    private Application app;
    
    @GenerateString(UUID)
    private String appId;
    
    private GetApplicationInfoOperation instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new GetApplicationInfoOperation(appRepo);
        verifyZeroInteractions(appRepo);
        
        setupData();
        setupMocks();
    }

    @Test
    public void testProcess() throws Exception
    {
        GetApplicationInfoResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.applicationInfo, is(app));
    }
    
    @DontRepeat
    @Test
    public void testProcessWhenAppDoesNotExist() throws Exception
    {
        when(appRepo.getById(appId))
            .thenThrow(new ApplicationDoesNotExistException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(ApplicationDoesNotExistException.class);
    }
    
    @Test
    public void testProcessWithBadRequest()
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.process(new GetApplicationInfoRequest()))
            .isInstanceOf(InvalidArgumentException.class);
        
        GetApplicationInfoRequest requestWithoutToken = new GetApplicationInfoRequest(request);
        requestWithoutToken.unsetToken();
        assertThrows(() -> instance.process(requestWithoutToken))
            .isInstanceOf(InvalidArgumentException.class);
    }

    private void setupData()
    {
        app.applicationId = appId;
        request.applicationId = appId;
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getById(appId)).thenReturn(app);
    }

}