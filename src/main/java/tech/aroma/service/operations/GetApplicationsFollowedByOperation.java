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

import java.util.List;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetApplicationsFollowedByRequest;
import tech.aroma.thrift.service.GetApplicationsFollowedByResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.isNullOrEmpty;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class GetApplicationsFollowedByOperation implements ThriftOperation<GetApplicationsFollowedByRequest, GetApplicationsFollowedByResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(GetApplicationsFollowedByOperation.class);
    
    private final ApplicationRepository appRepo;
    private final FollowerRepository followerRepo;
    private final UserRepository userRepo;
    
    @Inject
    GetApplicationsFollowedByOperation(ApplicationRepository appRepo,
                                       FollowerRepository followerRepo,
                                       UserRepository userRepo)
    {
        checkThat(appRepo, followerRepo, userRepo)
            .are(notNull());
        
        this.appRepo = appRepo;
        this.followerRepo = followerRepo;
        this.userRepo = userRepo;
    }
    
    @Override
    public GetApplicationsFollowedByResponse process(GetApplicationsFollowedByRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        String userId = request.userId;
        
        if (isNullOrEmpty(userId))
        {
            userId = request.token.userId;
        }
        
        checkThat(userId)
            .throwing(InvalidArgumentException.class)
            .usingMessage("userID is invalid")
            .is(validUserId());
        
        List<Application> apps = followerRepo.getApplicationsFollowedBy(userId)
            .stream()
            .sorted(comparing(app -> app.name))
            .collect(toList());
        
        LOG.debug("Found {} apps followed by [{]]", apps.size(), userId);
        
        return new GetApplicationsFollowedByResponse(apps);
    }
    
    private AlchemyAssertion<GetApplicationsFollowedByRequest> good()
    {
        return request ->
        {
            checkThat(request)
                .usingMessage("request is null")
                .is(notNull());
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
            
            checkThat(request.token.userId)
                .usingMessage("userId in token is invalid")
                .is(validUserId());
            
            if (request.isSetUserId())
            {
                checkThat(request.userId)
                    .is(validUserId());
            }
        };
    }
    
}
