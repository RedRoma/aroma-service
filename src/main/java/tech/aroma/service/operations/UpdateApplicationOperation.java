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

import java.util.UUID;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UnauthorizedException;
import tech.aroma.thrift.service.UpdateApplicationRequest;
import tech.aroma.thrift.service.UpdateApplicationResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.lang.String.format;
import static tech.aroma.data.assertions.RequestAssertions.validApplication;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.nonEmptySet;

/**
 *
 * @author SirWellington
 */
final class UpdateApplicationOperation implements ThriftOperation<UpdateApplicationRequest, UpdateApplicationResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(UpdateApplicationOperation.class);

    private final ApplicationRepository appRepo;
    private final MediaRepository mediaRepo;
    private final UserRepository userRepo;

    @Inject
    UpdateApplicationOperation(ApplicationRepository appRepo, MediaRepository mediaRepo, UserRepository userRepo)
    {
        checkThat(appRepo, mediaRepo, userRepo)
            .are(notNull());

        this.appRepo = appRepo;
        this.mediaRepo = mediaRepo;
        this.userRepo = userRepo;
    }

    @Override
    public UpdateApplicationResponse process(UpdateApplicationRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        String appId = request.updatedApplication.applicationId;
        
        //Throws if the app does not exist
        Application latestApp = appRepo.getById(appId);

        checkThat(request.token.userId)
            .throwing(UnauthorizedException.class)
            .is(ownerOf(latestApp));
        
        if (hasIcon(request))
        {
            String newIconId = saveNewAppIcon(request);
            deleteOldIcon(latestApp);
            setNewIconIdToApp(newIconId, request.updatedApplication);
        }

        appRepo.saveApplication(request.updatedApplication);

        return new UpdateApplicationResponse(request.updatedApplication);
    }

    private AlchemyAssertion<UpdateApplicationRequest> good()
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
            
            checkThat(request.updatedApplication)
                .usingMessage("request missing updated application")
                .is(notNull());
            
            checkThat(request.updatedApplication)
                .is(validApplication());
            
            checkThat(request.updatedApplication.owners)
                .usingMessage("Application must have at least 1 owner")
                .is(nonEmptySet());
            
            for (String ownerId : request.updatedApplication.owners)
            {
                checkThat(ownerId)
                    .usingMessage("Owner ID is Invalid: " + ownerId)
                    .is(validUserId())
                    .usingMessage("Application Owner Does not exist: " + ownerId)
                    .is(existingUser());
            }
        };
    }

    private AlchemyAssertion<String> ownerOf(Application application)
    {
        String appId = application.applicationId;

        return userId ->
        {
            if (Sets.isEmpty(application.owners))
            {
                throw new FailedAssertionException(format("Application with ID [%s] has no Owners", appId));
            }
            
            if (!application.owners.contains(userId))
            {
                throw new FailedAssertionException(format("User [%s] is not an Owner of Application with ID [%s]", userId,
                                                                                                                       appId));
            }
        };
    }

    private AlchemyAssertion<String> existingUser()
    {
        return userId ->
        {
            try
            {
                if (!userRepo.containsUser(userId))                
                {
                    throw new FailedAssertionException(format("User [%s] does not exist", userId));
                }
            }
            catch (TException ex)
            {
                LOG.error("Failed to check if user [{}] exists", userId, ex);
                throw new FailedAssertionException(format("Could not check if user exists: [%s]", userId));
            }
        };
    }


    private boolean hasIcon(UpdateApplicationRequest request)
    {
        Application app = request.updatedApplication;

        if (app == null)
        {
            return false;
        }

        Image icon = app.icon;
        if (icon == null)
        {
            return false;
        }

        byte[] data = icon.getData();

        return data != null && data.length > 0;
    }

    private String saveNewAppIcon(UpdateApplicationRequest request) throws TException
    {
        String newId = UUID.randomUUID().toString();
        Image newIcon = request.updatedApplication.icon;

        try
        {
            mediaRepo.saveMedia(newId, newIcon);
        }
        catch (TException ex)
        {
            LOG.warn("Failed to save App's new Icon: {}", request.updatedApplication, ex);
            throw ex;
        }

        return newId;
    }

    private void deleteOldIcon(Application app)
    {
        String existingIconId = app.applicationIconMediaId;

        try
        {
            if (mediaRepo.containsMedia(existingIconId))
            {
                mediaRepo.deleteMedia(existingIconId);
            }
        }
        catch (TException ex)
        {
            LOG.warn("Failed to delete old Application Icon: {} for App {}", existingIconId, app, ex);
        }
    }

    private void setNewIconIdToApp(String newIconId, Application updatedApplication)
    {
        updatedApplication.setApplicationIconMediaId(newIconId);
        updatedApplication.unsetIcon();
    }


}
