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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.ActivityRepository;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.FollowerRepository;
import tech.aroma.data.MessageRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.events.ApplicationMessagesDeleted;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.events.EventType;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.service.DeleteMessageRequest;
import tech.aroma.thrift.service.DeleteMessageResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.lang.String.format;
import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validMessageId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.elementInCollection;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;

/**
 *
 * @author SirWellington
 */
final class DeleteMessageOperation implements ThriftOperation<DeleteMessageRequest, DeleteMessageResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(DeleteMessageOperation.class);
    
    private final ActivityRepository activityRepo;
    private final ApplicationRepository appRepo;
    private final FollowerRepository followerRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;

    @Inject
    DeleteMessageOperation(ActivityRepository activityRepo,
                           ApplicationRepository appRepo,
                           FollowerRepository followerRepo,
                           MessageRepository messageRepo,
                           UserRepository userRepo)
    {
        checkThat(activityRepo, appRepo, followerRepo, messageRepo, userRepo)
            .are(notNull());
        
        this.activityRepo = activityRepo;
        this.appRepo = appRepo;
        this.followerRepo = followerRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
    }
    
    @Override
    public DeleteMessageResponse process(DeleteMessageRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        String appId = request.applicationId;
        String userId = request.token.userId;
        Application app = appRepo.getById(appId);

        checkThat(userId)
            .usingMessage("Not Authorized to delete messages for App")
            .throwing(UnauthorizedException.class)
            .is(elementInCollection(app.owners));

        int count;
        if (request.deleteAll)
        {
            count = deleteAllMessages(appId);
            saveActivityThatAppMessagesDeletedBy(userId, app, count);
        }
        else
        {
            count = deleteWithOptions(request);
        }

        return new DeleteMessageResponse().setMessagesDeleted(count);

    }

    private AlchemyAssertion<DeleteMessageRequest> good()
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
                .usingMessage("request missing userId in Token")
                .is(nonEmptyString());
            
            checkThat(request.applicationId)
                .is(validApplicationId());
            
            if (request.isSetMessageId())
            {
                checkThat(request.messageId)
                    .is(validMessageId());
            }
            
            if (request.isSetMessageIds())
            {
                for (String messageId : request.messageIds)
                {
                    checkThat(messageId)
                        .is(validMessageId());
                }
            }
        };
    }

    private int deleteWithOptions(DeleteMessageRequest request)
    {
        String appId = request.applicationId;

        Set<String> messagesToDelete = Sets.create();

        if (request.isSetMessageId())
        {
            messagesToDelete.add(request.messageId);
        }

        if (request.isSetMessageIds())
        {
            messagesToDelete.addAll(request.messageIds);
        }

        messagesToDelete.parallelStream()
            .forEach(msg -> this.deleteMessage(appId, msg));
        LOG.debug("Deleted {} messages for App [{]]", messagesToDelete.size(), appId);

        return messagesToDelete.size();
    }

    private int deleteAllMessages(String appId) throws TException
    {
        Long count = messageRepo.getCountByApplication(appId);
        messageRepo.deleteAllMessages(appId);

        LOG.debug("Deleted all {} messages for App {}", count, appId);
        return count.intValue();
    }

    private void deleteMessage(String appId, String messageId)
    {
        try
        {
            messageRepo.deleteMessage(appId, messageId);
        }
        catch (TException ex)
        {
            //Ignoring this is not good long-term behavior
            LOG.error("Could not delete message with ID [{}] for App [{}]", messageId, appId, ex);
        }
    }

    private void saveActivityThatAppMessagesDeletedBy(String userId, Application app, int count) throws TException
    {
        User userDeleting = userRepo.getUser(userId);
        
        Event event = createEventRememberingAppMessagesDeleted(userDeleting, app, count);
        
        getUsersToNotifyFor(app)
            .parallelStream()
            .forEach(user -> this.tryToSaveEvent(event, user));
    }
    
    private Event createEventRememberingAppMessagesDeleted(User actor, Application app, int totalMessagesDeleted)
    {
        ApplicationMessagesDeleted messagesDeleted = new ApplicationMessagesDeleted()
            .setMessage(format("%d messages deleted for App %s", totalMessagesDeleted, app.name))
            .setTotalMessagesDeleted(totalMessagesDeleted);
            
        EventType eventType = createEventTypeFor(messagesDeleted);
        
        Event event = new Event()
            .setActor(actor)
            .setUserIdOfActor(actor.userId)
            .setApplication(app)
            .setApplicationId(app.applicationId)
            .setTimestamp(now().toEpochMilli())
            .setEventId(one(uuids))
        .setEventType(eventType);
        
        return event;
    }
    
 

    private EventType createEventTypeFor(ApplicationMessagesDeleted messagesDeleted)
    {
        EventType eventType = new EventType();
        eventType.setApplicationMessageDeleted(messagesDeleted);
        return eventType;
    }
    
    private List<User> getUsersToNotifyFor(Application app) throws TException
    {
        String appId = app.applicationId;

        List<User> owners = app.owners.stream()
            .map(this::toUser)
            .filter(Objects::nonNull)
            .collect(toList());

        List<User> followers = followerRepo.getApplicationFollowers(appId);

        return Lists.combine(owners, followers);
    }

    private User toUser(String userId)
    {
        try
        {
            return userRepo.getUser(userId);
        }
        catch (Exception ex)
        {
            LOG.warn("Failed to load User with ID [{}]", userId, ex);
            return null;
        }
    }

    private void tryToSaveEvent(Event event, User forUser)
    {
        try
        {
            activityRepo.saveEvent(event, forUser);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to save Event {} for User {}", event, forUser, ex);
        }
    }

}
