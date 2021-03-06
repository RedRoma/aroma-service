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
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.ApplicationDoesNotExistException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetApplicationInfoRequest;
import tech.aroma.thrift.service.GetApplicationInfoResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class GetApplicationInfoOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private FollowerRepository followingRepo;

    @GeneratePojo
    private GetApplicationInfoRequest request;

    @GeneratePojo
    private Application app;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String userId;

    private GetApplicationInfoOperation instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new GetApplicationInfoOperation(appRepo, followingRepo);
        verifyZeroInteractions(appRepo, followingRepo);

        setupData();
        setupMocks();
    }

    @Test
    public void testProcess() throws Exception
    {
        app.isFollowing = true;

        GetApplicationInfoResponse response = instance.process(request);
        assertThat(response, notNullValue());

        assertThat(response.applicationInfo, is(app));
    }

    @Test
    public void testWhenUserNotFollowingApp() throws Exception
    {
        app.isFollowing = false;

        GetApplicationInfoResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.applicationInfo, is(app));
    }

    @Test
    public void testWhenFollowingInfoNotRequested() throws Exception
    {
        request.includeFollowingInfo = false;
        app.isFollowing = false;

        GetApplicationInfoResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.applicationInfo, is(app));
        verifyZeroInteractions(followingRepo);

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
        request.token.ownerId = userId;
        request.includeFollowingInfo = true;
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getById(appId)).thenReturn(app);
        when(followingRepo.followingExists(userId, appId))
                .thenReturn(false);
    }

}
