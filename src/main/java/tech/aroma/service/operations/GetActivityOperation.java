/*
 * Copyright 2015 Aroma Tech.
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
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetActivityRequest;
import tech.aroma.thrift.service.GetActivityResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class GetActivityOperation implements ThriftOperation<GetActivityRequest, GetActivityResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(GetActivityOperation.class);

    private final ActivityRepository activityRepo;
    private final UserRepository userRepo;

    @Inject
    GetActivityOperation(ActivityRepository activityRepo, UserRepository userRepo)
    {
        checkThat(activityRepo, userRepo)
            .are(notNull());
        
        this.activityRepo = activityRepo;
        this.userRepo = userRepo;
    }

    @Override
    public GetActivityResponse process(GetActivityRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        String userId = request.token.userId;
        User user = new User().setUserId(userId);
        
        List<Event> events = activityRepo.getAllEventsFor(user);
        
        LOG.debug("Found {} events for User {}", events.size(), user);
        
        return new GetActivityResponse(events);
    }

    private AlchemyAssertion<GetActivityRequest> good()
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
                .is(validUserId());
        };
    }

}
