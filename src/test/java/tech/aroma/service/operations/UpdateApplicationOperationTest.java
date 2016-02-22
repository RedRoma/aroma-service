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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.service.UpdateApplicationRequest;
import tech.aroma.thrift.service.UpdateApplicationResponse;
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
public class UpdateApplicationOperationTest
{

    @Mock
    private ApplicationRepository appRepo;
    @Mock
    private UserRepository userRepo;

    private UpdateApplicationOperation instance;

    @GeneratePojo
    private UpdateApplicationRequest request;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String userId;

    @GeneratePojo
    private Application app;

    @GenerateString(ALPHABETIC)
    private String badId;

    @Captor
    private ArgumentCaptor<Application> captor;

    @Before
    public void setUp() throws Exception
    {
        instance = new UpdateApplicationOperation(appRepo, userRepo);
        verifyZeroInteractions(appRepo, userRepo);

        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {
        app.applicationId = appId;
        app.unsetOrganizationId();
        app.owners.add(userId);

        request.updatedApplication = app;
        request.token.userId = userId;
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.containsApplication(appId))
            .thenReturn(true);

        when(appRepo.getById(appId)).thenReturn(app);
        
        for (String ownerId : app.owners)
        {
            when(userRepo.containsUser(ownerId)).thenReturn(true);
        }
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new UpdateApplicationOperation(null, userRepo))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new UpdateApplicationOperation(appRepo, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);

        UpdateApplicationRequest requestWithMissingToken = new UpdateApplicationRequest(request)
            .setToken(null);
        assertThrows(() -> instance.process(requestWithMissingToken))
            .isInstanceOf(InvalidArgumentException.class);

        UpdateApplicationRequest requestMissingApp = new UpdateApplicationRequest(request)
            .setUpdatedApplication(null);
        assertThrows(() -> instance.process(requestMissingApp))
            .isInstanceOf(InvalidArgumentException.class);

        UpdateApplicationRequest requestWithBadAppId = new UpdateApplicationRequest(request);
        requestWithBadAppId.updatedApplication.setApplicationId(badId);
        assertThrows(() -> instance.process(requestMissingApp))
            .isInstanceOf(InvalidArgumentException.class);

    }

    @Test
    public void testProcess() throws Exception
    {
        
        UpdateApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(appRepo).saveApplication(captor.capture());
        
        Application savedApp = captor.getValue();
        assertThat(savedApp, notNullValue());
        assertThat(savedApp, is(app));
    }

    @Test
    public void testWhenNotAuthorized() throws Exception
    {
        app.owners.remove(userId);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(UnauthorizedException.class);
    }
}
