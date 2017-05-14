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


import javax.inject.Inject;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetUserInfoRequest;
import tech.aroma.thrift.service.GetUserInfoResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.trueStatement;
import static tech.sirwellington.alchemy.arguments.assertions.PeopleAssertions.validEmailAddress;

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
            
        User user;
        if (shouldFindByEmail(request))
        {
            String email = request.email;
            user = userRepo.getUserByEmail(email);
        }
        else
        {
            String userId = request.userId;
            user = userRepo.getUser(userId);
        }
        
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
            
            checkThat(request.isSetEmail() || request.isSetUserId())
                .usingMessage("Request must have either email or userId set")
                .is(trueStatement());
            
            if (request.isSetUserId())
            {
                checkThat(request.userId)
                    .is(validUserId());
            }
            
            if(request.isSetEmail())
            {
                checkThat(request.email)
                    .is(validEmailAddress());
            }
            
        };
    }

    private boolean shouldFindByEmail(GetUserInfoRequest request)
    {
        return !isNullOrEmpty(request.email);
    }

}
