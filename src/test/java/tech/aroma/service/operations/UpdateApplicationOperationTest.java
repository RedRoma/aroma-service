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
import tech.aroma.data.MediaRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.ProgrammingLanguage;
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
import static tech.aroma.thrift.generators.ApplicationGenerators.applications;
import static tech.aroma.thrift.generators.ImageGenerators.appIcons;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class UpdateApplicationOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private MediaRepository mediaRepo;

    @Mock
    private UserRepository userRepo;

    private UpdateApplicationOperation instance;

    @GeneratePojo
    private UpdateApplicationRequest request;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String userId;

    private Application oldApp;

    private Application newApp;

    @GenerateString(ALPHABETIC)
    private String badId;

    @Captor
    private ArgumentCaptor<Application> captor;

    @Before
    public void setUp() throws Exception
    {
        instance = new UpdateApplicationOperation(appRepo, mediaRepo, userRepo);
        verifyZeroInteractions(appRepo, mediaRepo, userRepo);

        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {
        oldApp = one(applications());

        appId = oldApp.applicationId;
        oldApp.unsetOrganizationId();
        oldApp.owners.add(userId);
        oldApp.unsetIcon();

        newApp = new Application(oldApp);

        request.updatedApplication = newApp;
        request.token.userId = userId;
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.containsApplication(appId))
            .thenReturn(true);

        when(appRepo.getById(appId)).thenReturn(oldApp);

        for (String ownerId : oldApp.owners)
        {
            when(userRepo.containsUser(ownerId)).thenReturn(true);
        }

        when(mediaRepo.containsMedia(oldApp.applicationIconMediaId))
            .thenReturn(false);
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new UpdateApplicationOperation(null, null, userRepo))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new UpdateApplicationOperation(appRepo, null, userRepo))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new UpdateApplicationOperation(appRepo, mediaRepo, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testProcess() throws Exception
    {

        UpdateApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());

        verify(appRepo).saveApplication(captor.capture());

        Application savedApp = captor.getValue();
        assertThat(savedApp, notNullValue());
        assertThat(savedApp, is(newApp));
    }

    @Test
    public void testWhenDescriptionChanges() throws Exception
    {
        String newDescription = one(alphabeticString());
        newApp.setApplicationDescription(newDescription);

        Application savedApp = instance.process(request).getApplication();

        verify(appRepo).saveApplication(newApp);
        assertThat(savedApp, is(newApp));
    }
    
    @Test
    public void testWhenLanguageChanges() throws Exception
    {
        ProgrammingLanguage newLanguage = enumValueOf(ProgrammingLanguage.class).get();
        newApp.setProgrammingLanguage(newLanguage);
        
        UpdateApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(appRepo).saveApplication(newApp);
        
    }

    @Test
    public void testWhenNotAuthorized() throws Exception
    {
        oldApp.owners.remove(userId);

        assertThrows(() -> instance.process(request))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    public void testWhenAppIconChanges() throws Exception
    {
        Image newIcon = one(appIcons());
        newApp.setIcon(newIcon);

        UpdateApplicationResponse response = instance.process(request);

        verify(appRepo).saveApplication(captor.capture());

        Application savedApp = captor.getValue();
        assertThat(savedApp, is(newApp));

        String newIconId = savedApp.applicationIconMediaId;
        verify(mediaRepo).saveMedia(newIconId, newIcon);
    }

    @Test
    public void testWhenNoOwnersIncluded() throws Exception
    {
        newApp.owners.clear();

        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
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
}
