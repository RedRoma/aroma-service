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

package tech.aroma.banana.service;

import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.Image;
import tech.aroma.banana.thrift.ImageType;
import tech.aroma.banana.thrift.ProgrammingLanguage;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.events.ApplicationSentMessage;
import tech.aroma.banana.thrift.events.ApplicationTokenRegenerated;
import tech.aroma.banana.thrift.events.ApplicationTokenRenewed;
import tech.aroma.banana.thrift.events.Event;
import tech.aroma.banana.thrift.events.EventType;
import tech.aroma.banana.thrift.events.HealthCheckBackToNormal;
import tech.aroma.banana.thrift.events.HealthCheckFailed;
import tech.aroma.banana.thrift.events.OwnerApprovedRequest;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.EnumGenerators;

import static sir.wellington.alchemy.collections.sets.Sets.toSet;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveLongs;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.generator.PeopleGenerators.emails;
import static tech.sirwellington.alchemy.generator.PeopleGenerators.names;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphanumericString;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.generator.TimeGenerators.pastInstants;

/**
 * This is a temporary class that will be around as long as we are still generating
 * false data.
 * 
 * @author SirWellington
 */
@NonInstantiable
@Internal
public final class BananaGenerators
{

    private final static Logger LOG = LoggerFactory.getLogger(BananaGenerators.class);

    public static AlchemyGenerator<EventType> eventTypes()
    {
        return () ->
        {
            EventType eventType = new EventType();
            
            int number = one(integers(1, 7));
            
            switch (number)
            {
                case 1:
                    ApplicationSentMessage applicationSentMessage = pojos(ApplicationSentMessage.class).get();
                    applicationSentMessage.unsetMessage();
                    eventType.setApplicationSentMessage(applicationSentMessage);
                    break;
                case 2:
                    ApplicationTokenRegenerated applicationTokenRegenerated = pojos(ApplicationTokenRegenerated.class).get();
                    applicationTokenRegenerated.setUser(one(users()));
                    applicationTokenRegenerated.unsetMessage();
                    eventType.setApplicationTokenRegenerated(applicationTokenRegenerated);
                    break;
                case 3:
                    ApplicationTokenRenewed applicationTokenRenewed = pojos(ApplicationTokenRenewed.class).get();
                    applicationTokenRenewed.setUser(one(users()));
                    applicationTokenRenewed.unsetMessage();
                    eventType.setApplicationTokenRenewed(applicationTokenRenewed);
                    break;
                case 4:
                    HealthCheckBackToNormal healthCheckBackToNormal = pojos(HealthCheckBackToNormal.class).get();
                    healthCheckBackToNormal.unsetMessage();
                    eventType.setHealthCheckBackToNormal(healthCheckBackToNormal);
                    break;
                case 5:
                    HealthCheckFailed healthCheckFailed = pojos(HealthCheckFailed.class).get();
                    healthCheckFailed.unsetMessage();
                    eventType.setHealthCheckFailed(healthCheckFailed);
                    break;
                case 6:
                    OwnerApprovedRequest ownerApprovedRequest = pojos(OwnerApprovedRequest.class).get();
                    ownerApprovedRequest.setOwner(one(users()));
                    ownerApprovedRequest.unsetMessage();
                    eventType.setOwnerApprovedRequest(ownerApprovedRequest);
                    break;
            }
            
            return eventType;
        };
    }
    
    public static AlchemyGenerator<Long> pastTimes()
    {
        return () -> pastInstants().get().toEpochMilli();
    }

    public static AlchemyGenerator<Event> events()
    {
        return () ->
        {
            Event event = new Event();
            
            event.setEventType(one(eventTypes()))
                .setTimestamp(one(pastTimes()));
            
            return event;
        };
    }
    
    public static AlchemyGenerator<User> users()
    {
        return () ->
        {
            return new User().setName(names().get())
                .setEmail(one(emails()))
                .setUserId(one(alphanumericString()))
                .setProfileImage(one(profileImages()));
        };
    }
    
    public static AlchemyGenerator<User> usersWithoutProfileImages()
    {
        return () ->
        {
            return users().get().setProfileImage(null);
        };
    }
    
    static AlchemyGenerator<Image> profileImages()
    {
        List<String> images = Arrays.asList("Male-1.png",
                                            "Male-2.png",
                                            "Male-3.png",
                                            "Male-4.png",
                                            "Male-5.png",
                                            "Male-6.png",
                                            "Male-7.png",
                                            "Female-1.png",
                                            "Female-2.png",
                                            "Female-3.png",
                                            "Female-4.png",
                                            "Female-5.png",
                                            "Female-6.png");
        
        return () ->
        {
            Image profileImage = new Image();
            
            String image = Lists.oneOf(images);
            URL resource = Resources.getResource("images/" + image);
            byte[] binary;
            try
            {
                binary = Resources.toByteArray(resource);
                profileImage.setImageType(ImageType.PNG)
                    .setData(binary);
            }
            catch (IOException ex)
            {
                LOG.error("Failed to load Resource {}", resource, ex);
            }
            
            return profileImage;
        };
    }
    
    
    
    private static final AlchemyGenerator<ProgrammingLanguage> LANGUAGES = EnumGenerators.enumValueOf(ProgrammingLanguage.class);
    
    public static AlchemyGenerator<Application> applications()
    {
        return () ->
        {
            int numberOfOwners = one(integers(1, 4));
            int numberOfFollowers = one(integers(0, 1000));
            
            return new Application()
                .setApplicationId(one(uuids))
                .setName(names().get())
                .setProgrammingLanguage(LANGUAGES.get())
                .setFollowers(toSet(listOf(uuids, numberOfFollowers)))
                .setOwners(toSet(listOf(uuids, numberOfOwners)))
                .setTotalMessagesSent(one(positiveLongs()))
                .setTimeOfProvisioning(one(pastTimes()));
            
        };
    }
}
