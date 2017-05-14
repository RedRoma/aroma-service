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
import tech.aroma.data.*;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.service.GetBuzzRequest;
import tech.aroma.thrift.service.GetBuzzResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class GetBuzzOperationTest
{

    @Mock
    private ApplicationRepository appRepo;

    @Mock
    private OrganizationRepository orgRepo;

    @Mock
    private UserRepository userRepo;

    @GeneratePojo
    private GetBuzzRequest request;

    private GetBuzzOperation instance;
    
    @GenerateList(Application.class)
    private List<Application> recentApps;
    
    @GenerateList(User.class)
    private List<User> recentUsers;

    @Before
    public void setUp() throws Exception
    {
        instance = new GetBuzzOperation(appRepo, orgRepo, userRepo);
        verifyZeroInteractions(appRepo, orgRepo, userRepo);
        
        setupMocks();
    }
    
    private void setupMocks() throws Exception
    {
        when(appRepo.getRecentlyCreated())
            .thenReturn(recentApps);
        
        when(userRepo.getRecentlyCreatedUsers()).thenReturn(recentUsers);
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GetBuzzOperation(null, orgRepo, userRepo))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new GetBuzzOperation(appRepo, null, userRepo))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new GetBuzzOperation(appRepo, orgRepo, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testProcess() throws Exception
    {
        GetBuzzResponse result = instance.process(request);
        assertThat(result, notNullValue());
        assertThat(result.freshApplications, is(recentApps));
        assertThat(result.freshUsers, is(recentUsers));
        
        verify(appRepo).getRecentlyCreated();
        verify(userRepo).getRecentlyCreatedUsers();
    }

}
