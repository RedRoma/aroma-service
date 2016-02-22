/*
 * Copyright 2015 Aroma Tech.
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
import tech.aroma.thrift.service.GetMyApplicationsRequest;
import tech.aroma.thrift.service.GetMyApplicationsResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class GetMyApplicationsOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @GeneratePojo
    private GetMyApplicationsRequest request;

    private GetMyApplicationsOperation instance;

    @GenerateString(UUID)
    private String userId;

    @GenerateList(Application.class)
    private List<Application> apps;

    @GenerateString(ALPHABETIC)
    private String badId;

    @Before
    public void setUp() throws Exception
    {
        request = pojos(GetMyApplicationsRequest.class).get();
        instance = new GetMyApplicationsOperation(appRepo);

        setupData();
        setupMocks();
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GetMyApplicationsOperation(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testProcess() throws Exception
    {
        GetMyApplicationsResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.applications, is(apps));

        verify(appRepo).getApplicationsOwnedBy(userId);
    }

    @Test
    public void testWhenNoAppsOwned() throws Exception
    {
        when(appRepo.getApplicationsOwnedBy(userId))
            .thenReturn(Lists.emptyList());

        GetMyApplicationsResponse response = instance.process(request);
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

        GetMyApplicationsRequest emptyRequest = new GetMyApplicationsRequest();
        assertThrows(() -> instance.process(emptyRequest))
            .isInstanceOf(InvalidArgumentException.class);

    }

    private void setupData()
    {
        request.token.userId = userId;
    }

    private void setupMocks() throws Exception
    {
        when(appRepo.getApplicationsOwnedBy(userId))
            .thenReturn(apps);
    }

}
