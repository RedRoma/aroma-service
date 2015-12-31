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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.notifications.ApplicationSentMessage;
import tech.aroma.banana.thrift.notifications.ApplicationTokenRegenerated;
import tech.aroma.banana.thrift.notifications.ApplicationTokenRenewed;
import tech.aroma.banana.thrift.notifications.Event;
import tech.aroma.banana.thrift.notifications.EventType;
import tech.aroma.banana.thrift.notifications.HealthCheckBackToNormal;
import tech.aroma.banana.thrift.notifications.HealthCheckFailed;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;

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
            
            int number = one(integers(1, 6));
            
            switch (number)
            {
                case 1:
                    ApplicationSentMessage applicationSentMessage = pojos(ApplicationSentMessage.class).get();
                    eventType.setApplicationSentMessage(applicationSentMessage);
                    break;
                case 2:
                    ApplicationTokenRegenerated applicationTokenRegenerated = pojos(ApplicationTokenRegenerated.class).get();
                    eventType.setApplicationTokenRegenerated(applicationTokenRegenerated);
                    break;
                case 3:
                    ApplicationTokenRenewed applicationTokenRenewed = pojos(ApplicationTokenRenewed.class).get();
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
}
