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

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.ProgrammingLanguage;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.service.UpdateApplicationRequest;
import tech.aroma.thrift.service.UpdateApplicationResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.aroma.thrift.generators.ApplicationGenerators.applications;
import static tech.aroma.thrift.generators.ImageGenerators.appIcons;
import static tech.aroma.thrift.generators.UserGenerators.users;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.equalTo;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.epochNowWithinDelta;
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
    private ActivityRepository activityRepo;
    
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
    
    private String userId;
    
    private User user;
    
    private Application oldApp;
    
    private Application newApp;
    
    @GenerateString(ALPHABETIC)
    private String badId;
    
    @Captor
    private ArgumentCaptor<Application> captor;
    
    @Captor
    private ArgumentCaptor<Event> eventCaptor;
    
    @Before
    public void setUp() throws Exception
    {
        instance = new UpdateApplicationOperation(activityRepo, appRepo, mediaRepo, userRepo);
        verifyZeroInteractions(activityRepo, appRepo, mediaRepo, userRepo);
        
        setupData();
        setupMocks();
    }
    
    private void setupData() throws Exception
    {
        oldApp = one(applications());
        user = one(users());
        userId = user.userId;
        
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
        
        when(userRepo.getUser(userId)).thenReturn(user);
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new UpdateApplicationOperation(null, appRepo, mediaRepo, userRepo));
        assertThrows(() -> new UpdateApplicationOperation(activityRepo, null, mediaRepo, userRepo));
        assertThrows(() -> new UpdateApplicationOperation(activityRepo, appRepo, null, userRepo));
        assertThrows(() -> new UpdateApplicationOperation(activityRepo, appRepo, mediaRepo, null));
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
        
        List<User> owners = Sets.copyOf(oldApp.owners)
            .stream()
            .map(id -> new User().setUserId(id))
            .collect(toList());
        
        verify(activityRepo).saveEvents(eventCaptor.capture(), eq(owners));
        
        Event event = eventCaptor.getValue();
        checkEvent(event);
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
    
    @Test
    public void testWhenActivityRepoFails() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(activityRepo)
            .saveEvents(any(), any());
        
        UpdateApplicationResponse response = instance.process(request);
        assertThat(response.application, is(newApp));
        
        verify(appRepo).saveApplication(newApp);
    }
    
    private void checkEvent(Event event)
    {
        checkThat(event).is(notNull());
        checkThat(event.eventId).is(validUUID());
        checkThat(event.timestamp).is(epochNowWithinDelta(2000L));
        checkThat(event.userIdOfActor).is(equalTo(userId));
        checkThat(event.actor).is(equalTo(user));
        checkThat(event.application).is(equalTo(oldApp));
        checkThat(event.applicationId).is(equalTo(appId));
    }
}
