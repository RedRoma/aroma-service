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

 
package tech.aroma.service.operations;


import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.FollowApplicationRequest;
import tech.aroma.thrift.service.FollowApplicationResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
final class FollowApplicationOperation implements ThriftOperation<FollowApplicationRequest, FollowApplicationResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(FollowApplicationOperation.class);
    
    private final ApplicationRepository appRepo;
    private final FollowerRepository followRepo;
    private final UserRepository userRepo;

    @Inject
    FollowApplicationOperation(ApplicationRepository appRepo, FollowerRepository followRepo, UserRepository userRepo)
    {
        checkThat(appRepo, followRepo, userRepo)
            .are(notNull());
        
        this.appRepo = appRepo;
        this.followRepo = followRepo;
        this.userRepo = userRepo;
    }
    
    @Override
    public FollowApplicationResponse process(FollowApplicationRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        String appId = request.applicationId;
        Application app = appRepo.getById(appId);
        
        String userId = request.token.userId;
        User user = userRepo.getUser(userId);
        
        followRepo.saveFollowing(user, app);
        
        LOG.debug("Following of App {} by User {} successfully saved", app, user);
        
        return new FollowApplicationResponse();
    }

    private AlchemyAssertion<FollowApplicationRequest> good()
    {
        return request ->
        {
            checkThat(request).is(notNull());
            
            checkThat(request.applicationId)
                .usingMessage("missing applicationId")
                .is(nonEmptyString())
                .usingMessage("applicationId must be a UUID")
                .is(validUUID());
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
            
            checkThat(request.token.userId)
                .usingMessage("User Token missing userId")
                .is(nonEmptyString())
                .usingMessage("userId in token must be a UUID")
                .is(validUUID());
        };
    }

}