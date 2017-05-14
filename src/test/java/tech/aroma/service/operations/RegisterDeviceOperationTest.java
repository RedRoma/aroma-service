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
import tech.aroma.thrift.service.RegisterDeviceRequest;
import tech.aroma.thrift.service.RegisterDeviceResponse;
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
public class RegisterDeviceOperationTest 
{
   
    @Mock
    private UserPreferencesRepository userPreferencesRepo;

    @Mock
    private UserRepository userRepo;

    private RegisterDeviceOperation instance;
    
    @GenerateString(UUID)
    private String userId;
    
    @GeneratePojo
    private RegisterDeviceRequest request;

    private MobileDevice device;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();

        instance = new RegisterDeviceOperation(userRepo, userPreferencesRepo);
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
        assertThrows(() -> new RegisterDeviceOperation(null, userPreferencesRepo));
        assertThrows(() -> new RegisterDeviceOperation(userRepo, null));
    }

    @Test
    public void testProcess() throws Exception
    {
        RegisterDeviceResponse response = instance.process(request);
        assertThat(response, notNullValue());

        verify(userRepo).containsUser(userId);
        verify(userPreferencesRepo).saveMobileDevice(userId, device);
        
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
            .saveMobileDevice(userId, device);
        
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
        
        RegisterDeviceRequest requestWithoutDevice = new RegisterDeviceRequest(request);
        requestWithoutDevice.unsetDevice();
        assertThrows(() -> instance.process(requestWithoutDevice)).isInstanceOf(InvalidArgumentException.class);
        
        RegisterDeviceRequest emptyRequest = new RegisterDeviceRequest();
        assertThrows(() -> instance.process(emptyRequest)).isInstanceOf(InvalidArgumentException.class);
        
        RegisterDeviceRequest requestWithoutToken = new RegisterDeviceRequest(request);
        requestWithoutToken.unsetToken();
        assertThrows(() -> instance.process(requestWithoutToken)).isInstanceOf(InvalidArgumentException.class);
    }
 
}