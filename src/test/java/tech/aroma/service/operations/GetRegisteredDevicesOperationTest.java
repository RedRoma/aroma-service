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
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.UserPreferencesRepository;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.service.GetRegisteredDevicesRequest;
import tech.aroma.thrift.service.GetRegisteredDevicesResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.ChannelGenerators.mobileDevices;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class GetRegisteredDevicesOperationTest 
{
    
    @Mock
    private UserPreferencesRepository userPreferencesRepo;
    
    private GetRegisteredDevicesOperation instance;

    @GeneratePojo
    private GetRegisteredDevicesRequest request;
    
    @GenerateString(UUID)
    private String userId;
    
    private List<MobileDevice> devices;
    

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        instance = new GetRegisteredDevicesOperation(userPreferencesRepo);
    }


    private void setupData() throws Exception
    {
        request.token.userId = userId;
        
        devices = listOf(mobileDevices(), 25);
        
        when(userPreferencesRepo.getMobileDevices(userId))
            .thenReturn(Sets.emptySet());
    }

    private void setupMocks() throws Exception
    {
        
    }
    
    @DontRepeat
    @Test
    public void tesConstructor()
    {
        assertThrows(() -> new GetRegisteredDevicesOperation(null));
    }

    @Test
    public void testProcess() throws Exception
    {
        Set<MobileDevice> expected = Sets.copyOf(devices);
        
        when(userPreferencesRepo.getMobileDevices(userId))
            .thenReturn(expected);
            
        GetRegisteredDevicesResponse response = instance.process(request);
        assertThat(Sets.copyOf(response.devices), is(expected));
    }
    
    @Test
    public void testProcessWhenNone() throws Exception
    {
        GetRegisteredDevicesResponse response = instance.process(request);
        assertThat(response.devices, is(empty()));
    }

    @DontRepeat
    @Test
    public void testWhenDeviceRepoFails() throws Exception
    {
        when(userPreferencesRepo.getMobileDevices(userId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @Test
    public void testProcessWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null)).isInstanceOf(InvalidArgumentException.class);
        
        assertThrows(() -> instance.process(new GetRegisteredDevicesRequest())).isInstanceOf(InvalidArgumentException.class);
        
        UserToken badToken = one(pojos(UserToken.class));
        assertThrows(() -> instance.process(new GetRegisteredDevicesRequest(badToken))).isInstanceOf(InvalidArgumentException.class);
    }
}