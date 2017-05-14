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
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetApplicationsOwnedByRequest;
import tech.aroma.thrift.service.GetApplicationsOwnedByResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class GetApplicationsOwnedByOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @GeneratePojo
    private GetApplicationsOwnedByRequest request;

    private GetApplicationsOwnedByOperation instance;

    @GenerateString(UUID)
    private String userId;

    @GenerateList(Application.class)
    private List<Application> apps;
    
    private List<Application> sortedApps;

    @GenerateString(ALPHABETIC)
    private String badId;

    @Before
    public void setUp() throws Exception
    {
        request = pojos(GetApplicationsOwnedByRequest.class).get();
        instance = new GetApplicationsOwnedByOperation(appRepo);

        setupData();
        setupMocks();
    }

    private void setupData()
    {
        request.token.userId = userId;
        apps.forEach((Application app) -> app.setIsFollowingIsSet(true));
        
        sortedApps = apps.stream()
            .sorted((first, second) -> first.name.compareTo(second.name))
            .collect(toList());
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getApplicationsOwnedBy(userId))
            .thenReturn(apps);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GetApplicationsOwnedByOperation(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testProcess() throws Exception
    {
        GetApplicationsOwnedByResponse response = instance.process(request);
        assertThat(response, notNullValue());

        List<Application> sortedApps = apps.stream()
            .sorted((left, right) -> left.name.compareTo(right.name))
            .collect(toList());

        assertThat(response.applications, is(sortedApps));

        verify(appRepo).getApplicationsOwnedBy(userId);
    }

    @Test
    public void testWhenNoAppsOwned() throws Exception
    {
        when(appRepo.getApplicationsOwnedBy(userId))
            .thenReturn(Lists.emptyList());

        GetApplicationsOwnedByResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.applications, is(empty()));
    }

    @Test
    public void testWithBadUserId() throws Exception
    {
        request.token.userId = badId;

        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testWhenTokenIsMissing() throws Exception
    {
        request.unsetToken();

        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testWhenTokenMissingUserId() throws Exception
    {
        request.token.unsetUserId();

        assertThrows(() -> instance.process(request))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testProcessEdgeCases()
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);

        GetApplicationsOwnedByRequest emptyRequest = new GetApplicationsOwnedByRequest();
        assertThrows(() -> instance.process(emptyRequest))
            .isInstanceOf(InvalidArgumentException.class);

    }


}
