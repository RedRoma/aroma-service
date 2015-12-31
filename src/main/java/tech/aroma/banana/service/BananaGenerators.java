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
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.thrift.Image;
import tech.aroma.banana.thrift.ImageType;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.notifications.ApplicationSentMessage;
import tech.aroma.banana.thrift.notifications.ApplicationTokenRegenerated;
import tech.aroma.banana.thrift.notifications.ApplicationTokenRenewed;
import tech.aroma.banana.thrift.notifications.Event;
import tech.aroma.banana.thrift.notifications.EventType;
import tech.aroma.banana.thrift.notifications.HealthCheckBackToNormal;
import tech.aroma.banana.thrift.notifications.HealthCheckFailed;
import tech.aroma.banana.thrift.notifications.OwnerApprovedRequest;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.BinaryGenerators;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.generator.TimeGenerators.pastInstants;

/**
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
                    eventType.setApplicationSentMessage(applicationSentMessage);
                    break;
                case 2:
                    ApplicationTokenRegenerated applicationTokenRegenerated = pojos(ApplicationTokenRegenerated.class).get();
                    applicationTokenRegenerated.setUser(one(users()));
                    eventType.setApplicationTokenRegenerated(applicationTokenRegenerated);
                    break;
                case 3:
                    ApplicationTokenRenewed applicationTokenRenewed = pojos(ApplicationTokenRenewed.class).get();
                    applicationTokenRenewed.setUser(one(users()));
                    eventType.setApplicationTokenRenewed(applicationTokenRenewed);
                    break;
                case 4:
                    HealthCheckBackToNormal healthCheckBackToNormal = pojos(HealthCheckBackToNormal.class).get();
                    eventType.setHealthCheckBackToNormal(healthCheckBackToNormal);
                    break;
                case 5:
                    HealthCheckFailed healthCheckFailed = pojos(HealthCheckFailed.class).get();
                    eventType.setHealthCheckFailed(healthCheckFailed);
                    break;
                case 6:
                    OwnerApprovedRequest ownerApprovedRequest = pojos(OwnerApprovedRequest.class).get();
                    ownerApprovedRequest.setOwner(one(users()));
                    eventType.setOwnerApprovedRequest(ownerApprovedRequest);
                    break;
            }
            
            return eventType;
        };
    }

    public static AlchemyGenerator<Event> events()
    {
        return () ->
        {
            Event event = new Event();
            
            event.setEventType(one(eventTypes()))
                .setTimestamp(pastInstants().get().toEpochMilli());
            
            return event;
        };
    }
    
    public static AlchemyGenerator<User> users()
    {
        return () ->
        {
            Image profileImage = new Image()
                .setImageType(ImageType.PNG)
                .setData(profileImages().get());
            
            User user = pojos(User.class).get();
            user.setProfileImage(profileImage);
            return user;
        };
    }
    
    static AlchemyGenerator<ByteBuffer> profileImages()
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
            String image = Lists.oneOf(images);
            URL resource = Resources.getResource("images/" + image);
            byte[] binary;
            try
            {
                binary = Resources.toByteArray(resource);
            }
            catch (IOException ex)
            {
                LOG.error("Failed to load Resource {}", resource);
                return BinaryGenerators.byteBuffers(1024).get();
            }

            return ByteBuffer.wrap(binary);
        };
    }
}
