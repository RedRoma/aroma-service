/*
 * Copyright 2016 RedRoma, Inc.
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

import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.CheckIfDeviceIsRegisteredRequest;
import tech.aroma.thrift.service.CheckIfDeviceIsRegisteredResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.RequestAssertions.validMobileDevice;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

import tech.aroma.data.UserPreferencesRepository;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 *
 * @author SirWellington
 */
final class CheckIfDeviceIsRegisteredOperation implements ThriftOperation<CheckIfDeviceIsRegisteredRequest, CheckIfDeviceIsRegisteredResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(CheckIfDeviceIsRegisteredOperation.class);

    private final UserPreferencesRepository userPreferencesRepo;

    @Inject
    CheckIfDeviceIsRegisteredOperation(UserPreferencesRepository userPreferencesRepo)
    {
        checkThat(userPreferencesRepo).is(notNull());

        this.userPreferencesRepo = userPreferencesRepo;
    }

    @Override
    public CheckIfDeviceIsRegisteredResponse process(CheckIfDeviceIsRegisteredRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        String userId = request.token.userId;

        boolean exists = userPreferencesRepo.containsMobileDevice(userId, request.device);

        return new CheckIfDeviceIsRegisteredResponse(exists);
    }

    private AlchemyAssertion<CheckIfDeviceIsRegisteredRequest> good()
    {
        return request ->
        {
            checkThat(request).is(notNull());
            
            checkThat(request.token)
                .usingMessage("request token missing token")
                .is(notNull());
            
            checkThat(request.token.userId)
                .is(validUserId());
            
            checkThat(request.device)
                .is(validMobileDevice());
        };
    }

}
