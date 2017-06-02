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
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.service.CheckIfDeviceIsRegisteredRequest;
import tech.aroma.thrift.service.CheckIfDeviceIsRegisteredResponse;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.aroma.thrift.generators.ChannelGenerators.mobileDevices;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.UUID;


/**
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class CheckIfDeviceIsRegisteredOperationTest
{

    @Mock
    private UserPreferencesRepository userPreferencesRepo;

    private CheckIfDeviceIsRegisteredOperation instance;

    @GeneratePojo
    private CheckIfDeviceIsRegisteredRequest request;

    @GenerateString(UUID)
    private String userId;

    private MobileDevice device;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new CheckIfDeviceIsRegisteredOperation(userPreferencesRepo);
    }

    private void setupData() throws Exception
    {
        device = one(mobileDevices());

        request.setDevice(device);
        request.token.userId = userId;

    }

    private void setupMocks() throws Exception
    {
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new CheckIfDeviceIsRegisteredOperation(null));
    }

    @Test
    public void testProcess() throws Exception
    {
        boolean deviceExists = one(booleans());
        when(userPreferencesRepo.containsMobileDevice(userId, device))
                .thenReturn(deviceExists);

        CheckIfDeviceIsRegisteredResponse response = instance.process(request);
        assertThat(response.isRegistered, is(deviceExists));

    }

    @DontRepeat
    @Test
    public void testProcessWhenDeviceRepoFails() throws Exception
    {
        when(userPreferencesRepo.containsMobileDevice(userId, device))
                .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.process(request))
                .isInstanceOf(OperationFailedException.class);
    }

    @Test
    public void testProcessWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.process(null)).isInstanceOf(InvalidArgumentException.class);
        assertThrows(() -> instance.process(new CheckIfDeviceIsRegisteredRequest())).isInstanceOf(InvalidArgumentException.class);

        CheckIfDeviceIsRegisteredRequest requestWithoutToken = new CheckIfDeviceIsRegisteredRequest();
        assertThrows(() -> instance.process(requestWithoutToken)).isInstanceOf(InvalidArgumentException.class);

        CheckIfDeviceIsRegisteredRequest requestWithoutDevice = new CheckIfDeviceIsRegisteredRequest(request);
        requestWithoutDevice.unsetDevice();
        assertThrows(() -> instance.process(requestWithoutDevice)).isInstanceOf(InvalidArgumentException.class);
    }

}