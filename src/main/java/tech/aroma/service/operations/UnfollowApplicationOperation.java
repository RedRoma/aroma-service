/*
 * Copyright 2016 RedRoma.
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
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.UnfollowApplicationRequest;
import tech.aroma.thrift.service.UnfollowApplicationResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class UnfollowApplicationOperation implements ThriftOperation<UnfollowApplicationRequest, UnfollowApplicationResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(UnfollowApplicationOperation.class);
    
    private final ActivityRepository activityRepo;
    private final FollowerRepository followerRepo;

    @Inject
    UnfollowApplicationOperation(ActivityRepository activityRepo, FollowerRepository followerRepo)
    {
        checkThat(activityRepo, followerRepo)
            .are(notNull());
        
        this.activityRepo = activityRepo;
        this.followerRepo = followerRepo;
    }

    @Override
    public UnfollowApplicationResponse process(UnfollowApplicationRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        String userId = request.token.userId;
        String appId = request.applicationId;
        
        followerRepo.deleteFollowing(userId, appId);

        return new UnfollowApplicationResponse();
    }

    private AlchemyAssertion<UnfollowApplicationRequest> good()
    {
        return request ->
        {
            checkThat(request)
                .usingMessage("request missing")
                .is(notNull());
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
            
            checkThat(request.token.userId)
                .is(validUserId());
            
            checkThat(request.applicationId)
                .is(validApplicationId());
        };
    }

}
