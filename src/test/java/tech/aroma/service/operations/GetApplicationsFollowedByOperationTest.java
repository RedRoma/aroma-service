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
import org.mockito.Mock;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetApplicationsFollowedByRequest;
import tech.aroma.thrift.service.GetApplicationsFollowedByResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
public class GetApplicationsFollowedByOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private FollowerRepository followerRepo;

    @Mock
    private UserRepository userRepo;

    private GetApplicationsFollowedByOperation instance;

    @GenerateList(Application.class)
    private List<Application> apps;
    
    private List<Application> sortedApps;

    @GeneratePojo
    private GetApplicationsFollowedByRequest request;

    @GenerateString(UUID)
    private String userId;

    @GenerateString(UUID)
    private String userIdOfCaller;

    @GenerateString(ALPHABETIC)
    private String badId;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new GetApplicationsFollowedByOperation(appRepo, followerRepo, userRepo);
    }

    private void setupData() throws Exception
    {
        request.token.userId = userIdOfCaller;
        request.userId = userId;
        
        sortedApps = apps.stream()
            .sorted(comparing(app -> app.name))
            .collect(toList());
    }

    private void setupMocks() throws Exception
    {
        when(followerRepo.getApplicationsFollowedBy(userId)).thenReturn(apps);
        when(followerRepo.getApplicationsFollowedBy(userIdOfCaller)).thenReturn(apps);
        
        for (Application app : apps)
        {
            when(appRepo.getById(app.applicationId))
                .thenReturn(app);
        }
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new GetApplicationsFollowedByOperation(null, followerRepo, userRepo))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new GetApplicationsFollowedByOperation(appRepo, null, userRepo))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new GetApplicationsFollowedByOperation(appRepo, followerRepo, null))
            .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void testProcess() throws Exception
    {
        GetApplicationsFollowedByResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.applications, is(sortedApps));

        verify(followerRepo).getApplicationsFollowedBy(userId);
        verify(followerRepo, never()).getApplicationsFollowedBy(userIdOfCaller);
    }

    @Test
    public void testWhenNoUserIdSupplied() throws Exception
    {
        request.unsetUserId();

        GetApplicationsFollowedByResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.applications, is(sortedApps));

    }

    @DontRepeat
    @Test
    public void testWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);

        //Empty Request
        assertThrows(() -> instance.process(new GetApplicationsFollowedByRequest()))
            .isInstanceOf(InvalidArgumentException.class);

        //Request missing token
        GetApplicationsFollowedByRequest requestMissingToken = new GetApplicationsFollowedByRequest(request);
        requestMissingToken.unsetToken();
        assertThrows(() -> instance.process(requestMissingToken))
            .isInstanceOf(InvalidArgumentException.class);
        
        //Request with bad userId
        GetApplicationsFollowedByRequest requestWithBadId = new GetApplicationsFollowedByRequest(request);
        requestWithBadId.setUserId(badId);
        assertThrows(() -> instance.process(requestWithBadId))
            .isInstanceOf(InvalidArgumentException.class);
 
        
    }

}
