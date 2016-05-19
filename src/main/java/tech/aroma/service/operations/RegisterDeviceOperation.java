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
import tech.aroma.data.UserPreferencesRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.service.RegisterDeviceRequest;
import tech.aroma.thrift.service.RegisterDeviceResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.RequestAssertions.validMobileDevice;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class RegisterDeviceOperation implements ThriftOperation<RegisterDeviceRequest, RegisterDeviceResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(RegisterDeviceOperation.class);

    private final UserRepository userRepo;
    private final UserPreferencesRepository userPreferencesRepo;

    @Inject
    RegisterDeviceOperation(UserRepository userRepo, UserPreferencesRepository userPreferencesRepo)
    {
        checkThat(userRepo, userPreferencesRepo)
            .is(notNull());
        
        this.userPreferencesRepo = userPreferencesRepo;
        this.userRepo = userRepo;
    }
    
    @Override
    public RegisterDeviceResponse process(RegisterDeviceRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        String userId = request.token.userId;
        ensureUserIdExists(userId);
        
        userPreferencesRepo.saveMobileDevice(userId, request.device);
        
        return new RegisterDeviceResponse();
    }

    private AlchemyAssertion<RegisterDeviceRequest> good()
    {
        return request ->
        {
            checkThat(request).is(notNull());
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
            
            checkThat(request.token.userId)
                .is(validUserId());
            
            checkThat(request.device)
                .is(validMobileDevice());
        };
    }

    private void ensureUserIdExists(String userId) throws TException
    {
        if (!userRepo.containsUser(userId))
        {
            throw new UserDoesNotExistException(userId);
        }
    }

}
