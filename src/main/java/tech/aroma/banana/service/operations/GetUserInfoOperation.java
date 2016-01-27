/*
 * Copyright 2016 Aroma Tech.
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

 
package tech.aroma.banana.service.operations;


import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.service.GetUserInfoRequest;
import tech.aroma.banana.thrift.service.GetUserInfoResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class GetUserInfoOperation implements ThriftOperation<GetUserInfoRequest, GetUserInfoResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetUserInfoOperation.class);
    
    private final UserRepository userRepo;

    @Inject
    GetUserInfoOperation(UserRepository userRepo)
    {
        checkThat(userRepo).is(notNull());
        
        this.userRepo = userRepo;
    }

    @Override
    public GetUserInfoResponse process(GetUserInfoRequest request) throws TException
    {
        checkThat(request)
            .throwing(InvalidArgumentException.class)
            .is(good());
        
        String userId = request.userId;
        
        User user = userRepo.getUser(userId);
        
        return new GetUserInfoResponse().setUserInfo(user);
    }

    private AlchemyAssertion<GetUserInfoRequest> good()
    {
        return request ->
        {
            checkThat(request)
                .is(notNull());
            
            checkThat(request.token)
                .is(notNull());
            
            checkThat(request.userId)
                .is(validUserId());
        };
    }

}
