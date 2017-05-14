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
import tech.aroma.data.UserPreferencesRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.service.UnregisterDeviceRequest;
import tech.aroma.thrift.service.UnregisterDeviceResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.ChannelGenerators.mobileDevices;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class UnregisterDeviceOperationTest 
{
  
    @Mock
    private UserRepository userRepo;

    @Mock
    private UserPreferencesRepository userPreferencesRepo;

    
    private UnregisterDeviceOperation instance;
    
    @GenerateString(UUID)
    private String userId;
    
    @GeneratePojo
    private UnregisterDeviceRequest request;

    private MobileDevice device;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new UnregisterDeviceOperation(userRepo, userPreferencesRepo);
        verifyZeroInteractions(userRepo, userPreferencesRepo);
    }


    private void setupData() throws Exception
    {
        device = one(mobileDevices());
        
        request.device = device;
        request.token.userId = userId;
    }

    private void setupMocks() throws Exception
    {
        when(userRepo.containsUser(userId)).thenReturn(true);
        
    }
    
      @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new UnregisterDeviceOperation(null, userPreferencesRepo));
        assertThrows(() -> new UnregisterDeviceOperation(userRepo, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        UnregisterDeviceResponse response = instance.process(request);
        assertThat(response, notNullValue());

        verify(userRepo).containsUser(userId);
        verify(userPreferencesRepo).deleteMobileDevice(userId, device);
        
    }
    
    @Test
    public void testProcessWhenUserDoesNotExist() throws Exception
    {
        when(userRepo.containsUser(userId))
            .thenReturn(false);
        
        assertThrows(() -> instance.process(request)).isInstanceOf(UserDoesNotExistException.class);
        verifyZeroInteractions(userPreferencesRepo);
    }
    
    @Test
    public void testWhenDeviceRepoFails() throws Exception
    {
        doThrow(new OperationFailedException())
            .when(userPreferencesRepo)
            .deleteMobileDevice(userId, device);
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
    }
    
    @Test
    public void testWhenUserRepoFails() throws Exception
    {
        when(userRepo.containsUser(userId))
            .thenThrow(new OperationFailedException());
        
        assertThrows(() -> instance.process(request))
            .isInstanceOf(OperationFailedException.class);
        
        verifyZeroInteractions(userPreferencesRepo);
    }
    
    @DontRepeat
    @Test
    public void testProcessWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null)).isInstanceOf(InvalidArgumentException.class);
        
        UnregisterDeviceRequest requestWithoutDevice = new UnregisterDeviceRequest(request);
        requestWithoutDevice.unsetDevice();
        assertThrows(() -> instance.process(requestWithoutDevice)).isInstanceOf(InvalidArgumentException.class);
        
        UnregisterDeviceRequest emptyRequest = new UnregisterDeviceRequest();
        assertThrows(() -> instance.process(emptyRequest)).isInstanceOf(InvalidArgumentException.class);
        
        UnregisterDeviceRequest requestWithoutToken = new UnregisterDeviceRequest(request);
        requestWithoutToken.unsetToken();
        assertThrows(() -> instance.process(requestWithoutToken)).isInstanceOf(InvalidArgumentException.class);
    }
 
}