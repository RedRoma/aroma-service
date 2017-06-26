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
import tech.aroma.data.*;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.service.GetFullMessageRequest;
import tech.aroma.thrift.service.GetFullMessageResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.ApplicationGenerators.applications;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class GetFullMessageOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private FollowerRepository followerRepo;

    @Mock
    private MessageRepository messageRepo;

    @GeneratePojo
    private GetFullMessageRequest request;

    private Application app;

    @GeneratePojo
    private Message message;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String messageId;

    @GenerateString(ALPHABETIC)
    private String badId;

    @GenerateString(UUID)
    private String userId;

    private GetFullMessageOperation instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new GetFullMessageOperation(appRepo, followerRepo, messageRepo);
        verifyZeroInteractions(appRepo, followerRepo, messageRepo);

        setupData();
        setupMocks();
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GetFullMessageOperation(null, followerRepo, messageRepo));
        assertThrows(() -> new GetFullMessageOperation(appRepo, null, messageRepo));
        assertThrows(() -> new GetFullMessageOperation(appRepo, followerRepo, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        GetFullMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.fullMessage, is(message));
    }

    @Test
    public void testWhenUserIsOnlyAFollower() throws Exception
    {
        setupWhereUserIsFollowerButNotOwner();

        GetFullMessageResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.fullMessage, is(message));
    }

    @DontRepeat
    @Test
    public void testWhenUserIsNeitherOwnerNorFollower() throws Exception
    {
        setupWhereUserIsNotAFollowerOrOwner();

        assertThrows(() -> instance.process(request))
                .isInstanceOf(MessageDoesNotExistException.class);
    }

    @DontRepeat
    @Test
    public void testWhenAppDoesNotExist() throws Exception
    {
        setupWhereAppDoesNotExist();

        assertThrows(() -> instance.process(request))
                .isInstanceOf(ApplicationDoesNotExistException.class);
    }

    @Test
    public void testWhenMessageNotExists() throws Exception
    {
        when(messageRepo.getMessage(appId, messageId))
                .thenThrow(new MessageDoesNotExistException());

        assertThrows(() -> instance.process(request))
                .isInstanceOf(MessageDoesNotExistException.class);
    }

    @DontRepeat
    @Test
    public void testWithEmptyRequest() throws Exception
    {
        assertThrows(() -> instance.process(null))
                .isInstanceOf(InvalidArgumentException.class);

        assertThrows(() -> instance.process(new GetFullMessageRequest()))
                .isInstanceOf(InvalidArgumentException.class);

    }

    @Test
    public void testWithBadIds() throws Exception
    {
        request.messageId = badId;

        assertThrows(() -> instance.process(request))
                .isInstanceOf(InvalidArgumentException.class);

        request.messageId = messageId;
        request.applicationId = badId;

        assertThrows(() -> instance.process(request))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testWhenFollowerRepoFails() throws Exception
    {
        app.owners.remove(userId);
        setupWhereFollowerRepoFails();

        assertThrows(() -> instance.process(request))
                .isInstanceOf(OperationFailedException.class);
    }

    private void setupData() throws Exception
    {
        app = one(applications());
        app.applicationId = appId;
        app.owners.add(userId);

        request.messageId = messageId;
        request.applicationId = appId;
        request.token.userId = userId;

        message.messageId = messageId;
        message.applicationId = appId;
    }

    private void setupMocks() throws Exception
    {
        when(messageRepo.getMessage(appId, messageId))
                .thenReturn(message);

        when(appRepo.getById(appId)).thenReturn(app);
    }

    private void setupWhereAppDoesNotExist() throws Exception
    {
        when(appRepo.getById(appId))
                .thenThrow(new ApplicationDoesNotExistException());
    }

    private void setupWhereUserIsNotAFollowerOrOwner() throws Exception
    {
        app.owners.remove(userId);

        when(followerRepo.followingExists(userId, appId))
                .thenReturn(false);
    }

    private void setupWhereUserIsFollowerButNotOwner() throws Exception
    {
        app.owners.remove(userId);

        when(followerRepo.followingExists(userId, appId))
                .thenReturn(true);
    }

    private void setupWhereFollowerRepoFails() throws Exception
    {
        when(followerRepo.followingExists(userId, appId))
                .thenThrow(new OperationFailedException());
    }

}
