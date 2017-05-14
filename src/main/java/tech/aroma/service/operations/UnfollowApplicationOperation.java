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
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.*;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.*;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.UnfollowApplicationRequest;
import tech.aroma.thrift.service.UnfollowApplicationResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.time.Instant.now;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;

/**
 *
 * @author SirWellington
 */
final class UnfollowApplicationOperation implements ThriftOperation<UnfollowApplicationRequest, UnfollowApplicationResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(UnfollowApplicationOperation.class);

    private final ActivityRepository activityRepo;
    private final ApplicationRepository appRepo;
    private final FollowerRepository followerRepo;
    private final UserRepository userRepo;

    @Inject
    UnfollowApplicationOperation(ActivityRepository activityRepo,
                                 ApplicationRepository appRepo,
                                 FollowerRepository followerRepo,
                                 UserRepository userRepo)
    {
        checkThat(activityRepo, appRepo, followerRepo, userRepo)
            .are(notNull());

        this.activityRepo = activityRepo;
        this.appRepo = appRepo;
        this.followerRepo = followerRepo;
        this.userRepo = userRepo;
    }

    @Override
    public UnfollowApplicationResponse process(UnfollowApplicationRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        String userId = request.token.userId;
        String appId = request.applicationId;
        
        User user = userRepo.getUser(userId);
        Application app = appRepo.getById(appId);

        followerRepo.deleteFollowing(userId, appId);
        
        sendNotificationThatAppUnfollowedBy(user, app);

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

    private void sendNotificationThatAppUnfollowedBy(User user, Application app)
    {
        Event event = createAppUnfollowedEvent(user, app);
        
        Sets.nullToEmpty(app.owners)
            .stream()
            .map(id -> new User().setUserId(id))
            .forEach(owner -> this.tryToSaveEvent(owner, event));
    }

    private Event createAppUnfollowedEvent(User user, Application app)
    {
        EventType eventType = createEventType();
        
        return new Event()
            .setEventId(one(uuids))
            .setActor(user)
            .setUserIdOfActor(user.userId)
            .setApplication(app)
            .setApplicationId(app.applicationId)
            .setTimestamp(now().toEpochMilli())
            .setEventType(eventType);
    }

    private EventType createEventType()
    {
        ApplicationUnfollowed appUnfollowed = new ApplicationUnfollowed();
        
        EventType eventType = new EventType();
        eventType.setApplicationUnfollowed(appUnfollowed);
        return eventType;
    }
    
    private void tryToSaveEvent(User owner, Event event)
    {
        try
        {
            activityRepo.saveEvent(event, owner);
        }
        catch (TException ex)
        {
            LOG.error("Failed to save Event {} for user {}", event, owner, ex);
        }
    }


}
