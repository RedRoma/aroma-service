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

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import tech.aroma.data.*;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.reactions.Reaction;
import tech.aroma.thrift.service.UpdateReactionsRequest;
import tech.aroma.thrift.service.UpdateReactionsResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.ReactionGenerators.reactions;
import static tech.aroma.thrift.generators.UserGenerators.users;
import static tech.aroma.thrift.service.AromaServiceConstants.MAXIMUM_REACTIONS;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.equalTo;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;
import static tech.sirwellington.alchemy.arguments.assertions.TimeAssertions.epochNowWithinDelta;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class UpdateReactionsOperationTest
{

    @Mock
    private ActivityRepository activityRepo;

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private ReactionRepository reactionsRepo;

    @Mock
    private UserRepository userRepo;

    @GeneratePojo
    private Application app;

    @GeneratePojo
    private UpdateReactionsRequest request;

    private List<Reaction> reactions;

    @GenerateString(UUID)
    private String appId;

    private String userId;
    private User user;

    @GenerateString(ALPHABETIC)
    private String badId;


    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    private UpdateReactionsOperation instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new UpdateReactionsOperation(activityRepo, appRepo, reactionsRepo, userRepo);
    }

    private void setupData() throws Exception
    {
        reactions = listOf(reactions(), 10);

        user = one(users());
        userId = user.userId;

        request.token.userId = userId;
        request.unsetForAppId();
        request.setReactions(reactions);

        app.applicationId = appId;
        app.owners.add(userId);
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getById(appId)).thenReturn(app);
        when(userRepo.containsUser(userId)).thenReturn(true);
        when(userRepo.getUser(userId)).thenReturn(user);
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new UpdateReactionsOperation(null, appRepo, reactionsRepo, userRepo));
        assertThrows(() -> new UpdateReactionsOperation(activityRepo, null, reactionsRepo, userRepo));
        assertThrows(() -> new UpdateReactionsOperation(activityRepo, appRepo, null, userRepo));
        assertThrows(() -> new UpdateReactionsOperation(activityRepo, appRepo, reactionsRepo, null));
    }

    @Test
    public void testProcessForUser() throws Exception
    {
        UpdateReactionsResponse response = instance.process(request);
        assertThat(response.reactions, is(reactions));

        verify(reactionsRepo).saveReactionsForUser(userId, reactions);
        verify(reactionsRepo, never()).saveReactionsForApplication(anyString(), any());
        verifyZeroInteractions(activityRepo, appRepo);
    }

    @Test
    public void testProcessForApp() throws Exception
    {
        request.forAppId = appId;
        UpdateReactionsResponse response = instance.process(request);
        assertThat(response.reactions, is(reactions));

        verify(reactionsRepo).saveReactionsForApplication(appId, reactions);
        verify(reactionsRepo, never()).saveReactionsForUser(anyString(), any());

        List<User> owners = app.owners
                .stream()
                .map(id -> new User().setUserId(id))
                .collect(toList());

        verify(activityRepo).saveEvents(eventCaptor.capture(), eq(owners));

        Event event = eventCaptor.getValue();
        checkEvent(event);
    }

    @Test
    public void testWhenActivityRepoFails() throws Exception
    {
        doThrow(new OperationFailedException())
                .when(activityRepo)
                .saveEvents(any(), any());

        request.forAppId = appId;

        UpdateReactionsResponse response = instance.process(request);
        assertThat(response.reactions, is(reactions));

        verify(reactionsRepo).saveReactionsForApplication(appId, reactions);
    }

    @Test
    public void testWhenUserDoesNotExist() throws Exception
    {
        when(userRepo.containsUser(userId)).thenReturn(false);

        assertThrows(() -> instance.process(request))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    public void testWhenUserRepoFails() throws Exception
    {
        when(userRepo.containsUser(userId))
                .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.process(request))
                .isInstanceOf(OperationFailedException.class);

        verifyZeroInteractions(reactionsRepo);
    }

    @Test
    public void testWhenAppDoesNotExist() throws Exception
    {
        when(appRepo.getById(appId))
                .thenThrow(new ApplicationDoesNotExistException());

        request.forAppId = appId;
        assertThrows(() -> instance.process(request))
                .isInstanceOf(ApplicationDoesNotExistException.class);

        verifyZeroInteractions(reactionsRepo);
    }

    @Test
    public void testWhenAppRepoFails() throws Exception
    {
        when(appRepo.getById(appId))
                .thenThrow(new OperationFailedException());

        request.forAppId = appId;
        assertThrows(() -> instance.process(request))
                .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testWhenReactionsRepoFails() throws Exception
    {
        doThrow(new OperationFailedException())
                .when(reactionsRepo)
                .saveReactionsForUser(userId, reactions);

        assertThrows(() -> instance.process(request))
                .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testProcessWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null))
                .isInstanceOf(InvalidArgumentException.class);

        //bad app id
        request.forAppId = badId;
        assertThrows(() -> instance.process(request))
                .isInstanceOf(InvalidArgumentException.class);

        //Missing token.
        request.unsetToken();
        assertThrows(() -> instance.process(request))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testProcessWhenUserIsNotAnOwner() throws Exception
    {
        app.owners.remove(userId);
        request.forAppId = appId;

        assertThrows(() -> instance.process(request))
                .isInstanceOf(UnauthorizedException.class);

        verifyZeroInteractions(reactionsRepo);
    }

    private void checkEvent(Event event)
    {
        checkThat(event).is(notNull());
        checkThat(event.eventId).is(validUUID());
        checkThat(event.actor).is(equalTo(user));
        checkThat(event.userIdOfActor).is(equalTo(userId));
        checkThat(event.application).is(equalTo(app));
        checkThat(event.applicationId).is(equalTo(appId));
        checkThat(event.timestamp).is(epochNowWithinDelta(4000L));
    }

    @Test
    public void testProcessWhenReactionsExceedLimit() throws Exception
    {
        int numberOfReactions = one(integers(MAXIMUM_REACTIONS + 1, MAXIMUM_REACTIONS * 2));
        request.reactions = listOf(reactions(), numberOfReactions);

        assertThrows(() -> instance.process(request))
                .isInstanceOf(InvalidArgumentException.class);
    }

}
