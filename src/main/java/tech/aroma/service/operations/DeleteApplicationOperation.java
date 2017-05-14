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
import java.util.Set;
import java.util.function.Function;
import javax.inject.Inject;

import com.google.common.base.Strings;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.*;
import tech.aroma.service.AromaAnnotations.SuperUsers;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.InvalidateTokenRequest;
import tech.aroma.thrift.events.*;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.service.DeleteApplicationRequest;
import tech.aroma.thrift.service.DeleteApplicationResponse;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.time.Instant.now;
import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.elementInCollection;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;

/**
 *
 * @author SirWellington
 */
@Internal
final class DeleteApplicationOperation implements ThriftOperation<DeleteApplicationRequest, DeleteApplicationResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(DeleteApplicationOperation.class);

    private final ActivityRepository activityRepo;
    private final ApplicationRepository appRepo;
    private final AuthenticationService.Iface authenticationService;
    private final FollowerRepository followerRepo;
    private final MediaRepository mediaRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;
    private final Function<UserToken, AuthenticationToken> tokenMapper;
    private final Set<String> superUsers;

    @Inject
    DeleteApplicationOperation(ActivityRepository activityRepo,
                               ApplicationRepository appRepo,
                               FollowerRepository followerRepo,
                               MediaRepository mediaRepo,
                               MessageRepository messageRepo,
                               UserRepository userRepo,
                               AuthenticationService.Iface authenticationService,
                               Function<UserToken, AuthenticationToken> tokenMapper,
                               @SuperUsers Set<String> superUsers)
    {
        checkThat(activityRepo,
                  appRepo,
                  followerRepo,
                  mediaRepo,
                  messageRepo,
                  userRepo,
                  authenticationService,
                  tokenMapper,
                  superUsers)
            .are(notNull());

        this.activityRepo = activityRepo;
        this.appRepo = appRepo;
        this.authenticationService = authenticationService;
        this.followerRepo = followerRepo;
        this.mediaRepo = mediaRepo;
        this.messageRepo = messageRepo;
        this.superUsers = superUsers;
        this.tokenMapper = tokenMapper;
        this.userRepo = userRepo;
    }

    @Override
    public DeleteApplicationResponse process(DeleteApplicationRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        Application app = appRepo.getById(request.applicationId);
        String userId = request.token.userId;

        checkThat(userId)
            .throwing(UnauthorizedException.class)
            .is(aUserAuthorizedToDelete(app));

        User user = userRepo.getUser(userId);

        deleteAllTokensBelongingToApp(app, request.token);
        deleteAllDataAssociatedWithApp(app, user);

        appRepo.deleteApplication(app.applicationId);
        LOG.debug("Successfully Deleted Application {}", app);

        return new DeleteApplicationResponse();
    }

    private AlchemyAssertion<DeleteApplicationRequest> good()
    {
        return request ->
        {
            checkThat(request).is(notNull());
            checkThat(request.applicationId).is(validApplicationId());
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
            
            checkThat(request.token.userId).is(validUserId());
        };
    }

    private AlchemyAssertion<String> aUserAuthorizedToDelete(Application app)
    {
        return userId ->
        {
            Set<String> allowedUsers = Sets.unionOf(app.owners, superUsers);
            
            checkThat(userId)
                .usingMessage("User is not an owner and cannot delete this Application")
                .is(elementInCollection(allowedUsers));
        };
    }

    private void deleteAllDataAssociatedWithApp(Application app, User userDeleting)
    {
        List<User> followers = tryToRemoveAllFollowersFor(app);
        tryToDeleteAllMessagesFor(app);
        tryToDeleteMediaFor(app);
        tryToSendNotificationThatAppWasDeletedBy(userDeleting, app, followers);
    }

    private void tryToDeleteMediaFor(Application app)
    {
        String iconLink = app.applicationIconMediaId;

        if (!Strings.isNullOrEmpty(iconLink))
        {
            deleteIcon(app, iconLink);
        }

        deleteIcon(app, app.applicationId);
    }

    private void deleteIcon(Application app, String iconLink)
    {
        try
        {
            mediaRepo.deleteMedia(iconLink);
            mediaRepo.deleteAllThumbnails(iconLink);
        }
        catch (TException ex)
        {
            LOG.info("Could not delete icon [{}] for application {}", iconLink, app, ex);
        }
    }

    private List<User> tryToRemoveAllFollowersFor(Application app)
    {
        try
        {
            return removeAllFollowersFor(app);
        }
        catch (TException ex)
        {
            LOG.error("Failed to remove all followers for Application {}", app, ex);
            return Lists.emptyList();
        }
    }

    private List<User> removeAllFollowersFor(Application app) throws TException
    {
        String appId = app.applicationId;

        List<User> followers = followerRepo.getApplicationFollowers(appId);

        followers.parallelStream()
            .map(User::getUserId)
            .forEach(userId -> this.deleteFollowing(userId, appId));

        return followers;
    }

    private void deleteFollowing(String userId, String applicationId)
    {
        try
        {
            followerRepo.deleteFollowing(userId, applicationId);
        }
        catch (TException ex)
        {
            LOG.warn("Failed to remove Following of App [{}] By User [{]]", applicationId, userId, ex);
        }
    }

    private void tryToDeleteAllMessagesFor(Application app)
    {
        try
        {
            messageRepo.deleteAllMessages(app.applicationId);
        }
        catch (TException ex)
        {
            LOG.warn("Failed to delete all Messages for {}", app, ex);
        }
    }

    private void tryToSendNotificationThatAppWasDeletedBy(User user, Application app, List<User> followers)
    {
        try
        {
            sendNotificationThatAppWasDeletedBy(user, app, followers);
        }
        catch (Exception ex)
        {
            LOG.warn("Failed to send notification that App {} was deleted by {}", app, user, ex);
        }
    }

    private void sendNotificationThatAppWasDeletedBy(User personDeleting, Application app, List<User> followers) throws TException
    {
        Event event = createEventThatAppWasDeletedBy(personDeleting, app);

        List<User> usersToNotify = getOwners(app);
        usersToNotify.addAll(followers);

        usersToNotify.parallelStream()
            .forEach(user -> this.tryToSave(event, user));
    }

    private List<User> getOwners(Application app) throws TException
    {
        return app.owners.stream()
            .map(id -> new User().setUserId(id))
            .collect(toList());
    }

    private Event createEventThatAppWasDeletedBy(User actor, Application app)
    {
        EventType eventType = createAppDeleted(app);

        return new Event()
            .setEventId(one(uuids))
            .setApplication(app)
            .setApplicationId(app.applicationId)
            .setUserIdOfActor(actor.userId)
            .setActor(actor)
            .setTimestamp(now().toEpochMilli())
            .setEventType(eventType);
    }

    private void tryToSave(Event event, User user)
    {
        try
        {
            activityRepo.saveEvent(event, user);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to save Event {} for User {}", event, user, ex);
        }
    }

    private EventType createAppDeleted(Application app)
    {
        ApplicationDeleted appDeleted = new ApplicationDeleted()
            .setMessage(app.name + " has been deleted");

        EventType eventType = new EventType();
        eventType.setApplicationDeleted(appDeleted);
        return eventType;
    }

    private void deleteAllTokensBelongingToApp(Application app, UserToken usingToken) throws TException
    {
        AuthenticationToken token = tokenMapper.apply(usingToken);

        InvalidateTokenRequest request = new InvalidateTokenRequest()
            .setToken(token)
            .setBelongingTo(app.applicationId);

        LOG.debug("Making request to invalidate all tokens belonging to: {}", app);

        try
        {
            authenticationService.invalidateToken(request);
        }
        catch (TException ex)
        {
            LOG.error("Failed to invalidate tokens belonging to {}", app, ex);
            throw ex;
        }
    }

}
