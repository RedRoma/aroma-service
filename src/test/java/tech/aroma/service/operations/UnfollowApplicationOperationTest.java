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
import tech.aroma.data.FollowerRepository;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.UnfollowApplicationRequest;
import tech.aroma.thrift.service.UnfollowApplicationResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class UnfollowApplicationOperationTest
{
    
    @Mock
    private FollowerRepository followerRepo;

    @GeneratePojo
    private UnfollowApplicationRequest request;

    @GenerateString(UUID)
    private String appId;

    @GenerateString(UUID)
    private String userId;

    @GenerateString(ALPHABETIC)
    private String badId;

    private UnfollowApplicationOperation instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new UnfollowApplicationOperation(followerRepo);

        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {
        request.token.setUserId(userId);
        request.setApplicationId(appId);

    }

    private void setupMocks() throws Exception
    {

    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new UnfollowApplicationOperation(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testProcess() throws Exception
    {
        UnfollowApplicationResponse response = instance.process(request);
        assertThat(response, notNullValue());
        
        verify(followerRepo).deleteFollowing(userId, appId);
    }

    @Test
    public void testWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);

        UnfollowApplicationRequest emptyRequest = new UnfollowApplicationRequest();
        assertThrows(() -> instance.process(emptyRequest))
            .isInstanceOf(InvalidArgumentException.class);

        UnfollowApplicationRequest requestMissingToken = new UnfollowApplicationRequest(request);
        requestMissingToken.unsetToken();
        assertThrows(() -> instance.process(requestMissingToken))
            .isInstanceOf(InvalidArgumentException.class);

        UnfollowApplicationRequest requestWithBadAppId = new UnfollowApplicationRequest(request)
            .setApplicationId(badId);
        assertThrows(() -> instance.process(requestWithBadAppId))
            .isInstanceOf(InvalidArgumentException.class);
        
        UserToken badToken = new UserToken(request.token).setUserId(badId);
        UnfollowApplicationRequest requestWithBadToken = new UnfollowApplicationRequest(request)
            .setToken(badToken);
        assertThrows(() -> instance.process(requestWithBadToken))
            .isInstanceOf(InvalidArgumentException.class);
    }

}
