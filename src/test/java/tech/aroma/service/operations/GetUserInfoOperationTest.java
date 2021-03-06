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

import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.service.GetUserInfoRequest;
import tech.aroma.thrift.service.GetUserInfoResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.Get.one;
import static tech.sirwellington.alchemy.generator.PeopleGenerators.emails;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class GetUserInfoOperationTest
{

    @Mock
    private UserRepository userRepo;

    @GeneratePojo
    private GetUserInfoRequest request;

    @GeneratePojo
    private User user;

    @GenerateString(UUID)
    private String userId;

    private String email;

    private GetUserInfoOperation instance;

    @Before
    public void setUp() throws TException
    {
        instance = new GetUserInfoOperation(userRepo);
        verifyZeroInteractions(userRepo);

        setupData();
        setupMocks();
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new GetUserInfoOperation(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testProcessWithUserId() throws Exception
    {
        request.unsetEmail();

        GetUserInfoResponse result = instance.process(request);

        assertThat(result, notNullValue());
        assertThat(result.userInfo, is(user));

        verify(userRepo).getUser(userId);
        verify(userRepo, never()).getUserByEmail(email);
    }

    @Test
    public void testProcessWithEmail() throws Exception
    {
        request.unsetUserId();

        GetUserInfoResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.userInfo, is(user));

        verify(userRepo).getUserByEmail(email);
        verify(userRepo, never()).getUser(userId);
    }

    @Test
    public void testWhenBothAreSet() throws Exception
    {
        request.setEmail(email)
               .setUserId(userId);

        GetUserInfoResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.userInfo, is(user));
    }

    @Test
    public void testProcessWhenUserDoesNotExist() throws Exception
    {
        request.unsetEmail();

        when(userRepo.getUser(userId))
                .thenThrow(new UserDoesNotExistException());

        assertThrows(() -> instance.process(request))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    @Test
    public void testProcessWhenEmailDoesNotExist() throws Exception
    {
        request.unsetUserId();

        when(userRepo.getUserByEmail(email))
                .thenThrow(new UserDoesNotExistException());

        assertThrows(() -> instance.process(request))
                .isInstanceOf(UserDoesNotExistException.class);
    }

    private void setupData() throws TException
    {
        request.userId = userId;
        request.unsetEmail();
        user.userId = userId;

        email = one(emails());
        request.email = email;
    }

    private void setupMocks() throws TException
    {
        when(userRepo.getUser(userId)).thenReturn(user);
        when(userRepo.getUserByEmail(email)).thenReturn(user);
    }

}
