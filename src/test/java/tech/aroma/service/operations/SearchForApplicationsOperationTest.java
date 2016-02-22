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
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.OrganizationRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.SearchForApplicationsRequest;
import tech.aroma.thrift.service.SearchForApplicationsResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.strings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class SearchForApplicationsOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private OrganizationRepository orgRepo;

    @GeneratePojo
    private SearchForApplicationsRequest request;
    
    @GeneratePojo
    private Application app;
    
    @GenerateString(UUID)
    private String appId;
    
    @GenerateString(UUID)
    private String orgId;
    
    @GenerateString(UUID)
    private String userId;
    
    @GenerateList(Application.class)
    private List<Application> apps;
    
    @GenerateString
    private String searchTerm;

    private SearchForApplicationsOperation instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new SearchForApplicationsOperation(appRepo, orgRepo);
        verifyZeroInteractions(appRepo, orgRepo);
        
        setupData();
        setupMocks();
    }

    @Test
    public void testProcess() throws Exception
    {
        SearchForApplicationsResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.applications, is(apps));
    }
    
    @Test
    public void testProcessWhenNoMatches() throws Exception
    {
        request.setApplicationName(one(strings()));
        
        SearchForApplicationsResponse response = instance.process(request);
        assertThat(response, notNullValue());
        assertThat(response.applications, is(empty()));
    }

    @DontRepeat
    @Test
    public void testWithBadRequest() throws Exception
    {
        assertThrows(() -> instance.process(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    private void setupData()
    {
        request.token.userId = userId;
        request.organizationId = orgId;
        request.applicationName = searchTerm;
        
        app.organizationId = orgId;
        app.applicationId = appId;
        
        apps = apps.stream()
            .map(app -> app.setName(app.name + searchTerm))
            .collect(toList());
    }

    private void setupMocks() throws TException
    {
        when(appRepo.getById(appId)).thenReturn(app);
        
        when(orgRepo.containsOrganization(orgId)).thenReturn(true);
        
        when(orgRepo.isMemberInOrganization(orgId, userId))
            .thenReturn(true);
        
        when(appRepo.getApplicationsByOrg(orgId))
            .thenReturn(apps);
    }
}
