/*
 * Copyright 2016 RedRoma, Inc..
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


import java.util.Set;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.data.DeviceRepository;
import tech.aroma.thrift.channels.MobileDevice;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetRegisteredDevicesRequest;
import tech.aroma.thrift.service.GetRegisteredDevicesResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class GetRegisteredDevicesOperation implements ThriftOperation<GetRegisteredDevicesRequest, GetRegisteredDevicesResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetRegisteredDevicesOperation.class);
    
    private final DeviceRepository deviceRepo;

    @Inject
    GetRegisteredDevicesOperation(DeviceRepository deviceRepo)
    {
        checkThat(deviceRepo).is(notNull());
        
        this.deviceRepo = deviceRepo;
    }

    @Override
    public GetRegisteredDevicesResponse process(GetRegisteredDevicesRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        String userId = request.token.userId;
        
        Set<MobileDevice> devices = deviceRepo.getMobileDevices(userId);
        
        return new GetRegisteredDevicesResponse().setDevices(Lists.copy(devices));
    }

    private AlchemyAssertion<GetRegisteredDevicesRequest> good()
    {
        return request ->
        {
            checkThat(request).is(notNull());
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
            
            checkThat(request.token.userId)
                .is(validUserId());
        };
    }

}
