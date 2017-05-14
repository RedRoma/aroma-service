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
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetApplicationInfoRequest;
import tech.aroma.thrift.service.GetApplicationInfoResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * @author SirWellington
 */
final class GetApplicationInfoOperation implements ThriftOperation<GetApplicationInfoRequest, GetApplicationInfoResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetApplicationInfoOperation.class);

    private final ApplicationRepository appRepo;
    private final FollowerRepository followerRepo;

    @Inject
    GetApplicationInfoOperation(ApplicationRepository appRepo, FollowerRepository followerRepo)
    {
        checkThat(appRepo, followerRepo)
                .are(notNull());

        this.appRepo = appRepo;
        this.followerRepo = followerRepo;
    }

    @Override
    public GetApplicationInfoResponse process(GetApplicationInfoRequest request) throws TException
    {
        checkThat(request)
                .throwing(InvalidArgumentException.class)
                .is(good());

        String appId = request.applicationId;
        Application app = appRepo.getById(appId);

        String userId = request.token.ownerId;

        if (request.includeFollowingInfo)
        {
            boolean isFollowing = tryToDetermineIfUserFollowingApp(userId, appId);
            app.setIsFollowing(isFollowing);
        }

        return new GetApplicationInfoResponse().setApplicationInfo(app);
    }

    private AlchemyAssertion<GetApplicationInfoRequest> good()
    {
        return request ->
        {
            checkThat(request)
                    .usingMessage("request is null")
                    .is(notNull());

            checkThat(request.applicationId)
                    .is(validApplicationId());

            checkThat(request.token)
                    .usingMessage("request missing token")
                    .is(notNull());
        };
    }

    private boolean tryToDetermineIfUserFollowingApp(String userId, String appId)
    {
        try
        {
            return followerRepo.followingExists(userId, appId);
        }
        catch (TException ex)
        {
            LOG.error("Failed to determine if user [{]] follows app [{}]", userId, appId, ex);
            return false;
        }

    }

}
