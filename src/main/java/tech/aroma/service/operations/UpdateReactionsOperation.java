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


import java.time.Instant;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.ReactionRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.ApplicationReactionsUpdated;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.events.EventType;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.service.UpdateReactionsRequest;
import tech.aroma.thrift.service.UpdateReactionsResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.trueStatement;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;

/**
 *
 * @author SirWellington
 */
final class UpdateReactionsOperation implements ThriftOperation<UpdateReactionsRequest, UpdateReactionsResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(UpdateReactionsOperation.class);

    private final ActivityRepository activityRepo;
    private final ApplicationRepository appRepo;
    private final ReactionRepository reactionsRepo;
    private final UserRepository userRepo;

    @Inject
    UpdateReactionsOperation(ActivityRepository activityRepo,
                             ApplicationRepository appRepo, 
                             ReactionRepository reactionsRepo, 
                             UserRepository userRepo)
    {
        checkThat(activityRepo, appRepo, reactionsRepo, userRepo)
            .are(notNull());
        
        this.activityRepo = activityRepo;
        this.appRepo = appRepo;
        this.reactionsRepo = reactionsRepo;
        this.userRepo = userRepo;
    }
    
    @Override
    public UpdateReactionsResponse process(UpdateReactionsRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        String userId = request.token.userId;
        
        if (request.isSetForAppId())
        {
            String appId = request.forAppId;
            
            Application app = appRepo.getById(appId);
            
            checkThat(userId)
                .throwing(UnauthorizedException.class)
                .usingMessage("Only Owners can update an App's Reactions")
                .is(ownerOf(app));
            
            reactionsRepo.saveReactionsForApplication(appId, request.reactions);

            User userPerformingUpdate = userRepo.getUser(userId);
            notifyOwnersOfReactionsUpdate(app, userPerformingUpdate);
        }
        else
        {
            checkThat(userRepo.containsUser(userId))
                .throwing(UserDoesNotExistException.class)
                .is(trueStatement());
            
            reactionsRepo.saveReactionsForUser(userId, request.reactions);
        }
        
        
        return new UpdateReactionsResponse().setReactions(request.reactions);
    }

    private AlchemyAssertion<UpdateReactionsRequest> good()
    {
        return request ->
        {
            checkThat(request).is(notNull());
            checkThat(request.token).is(notNull());
            
            checkThat(request.token.userId)
                .is(validUserId());
            
            if (request.isSetForAppId())
            {
                checkThat(request.forAppId)
                    .is(validApplicationId());
            }
        };
    }

    private AlchemyAssertion<String> ownerOf(Application app)
    {
        return userId ->
        {
            Set<String> owners = Sets.nullToEmpty(app.owners);
            
            if (!owners.contains(userId))
            {
                throw new FailedAssertionException(format("User %s is not an owner of app %s", userId, app.applicationId));
            }
        };
    }

    private void notifyOwnersOfReactionsUpdate(Application app, User userPerformingUpdate)
    {
        Event event = createEventToNotifyOwners(app, userPerformingUpdate);

        List<User> owners = Sets.nullToEmpty(app.owners)
            .stream()
            .map(id -> new User().setUserId(id))
            .collect(toList());

        try
        {
            activityRepo.saveEvents(event, owners);
        }
        catch (Exception ex)
        {
            LOG.warn("Failed to save Event in Activity Repo: {}", event, ex);
        }
    }

    private Event createEventToNotifyOwners(Application app, User userPerformingUpdate)
    {
        EventType eventType = new EventType();
        eventType.setApplicationReactionsUpdated(new ApplicationReactionsUpdated().setMessage(app.name + " Reactions Updated"));

        return new Event()
            .setEventId(one(uuids))
            .setActor(userPerformingUpdate)
            .setUserIdOfActor(userPerformingUpdate.userId)
            .setApplication(app)
            .setApplicationId(app.applicationId)
            .setEventType(eventType)
            .setTimestamp(Instant.now().toEpochMilli());

    }

}
