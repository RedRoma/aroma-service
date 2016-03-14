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
import java.util.stream.Collectors;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.service.AromaAssertions;
import tech.aroma.thrift.events.Event;
import tech.aroma.thrift.service.GetActivityRequest;
import tech.aroma.thrift.service.GetActivityResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.thrift.generators.EventGenerators.events;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;

/**
 *
 * @author SirWellington
 */
final class GetActivityOperation implements ThriftOperation<GetActivityRequest, GetActivityResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetActivityOperation.class);

    @Override
    public GetActivityResponse process(GetActivityRequest request) throws TException
    {
        AromaAssertions.checkNotNull(request);
        
        int numberOfEvents = one(integers(0, 100));
        List<Event> events = listOf(events(), numberOfEvents).stream()
            .sorted((a, b) -> Long.compare(a.getTimestamp(), b.getTimestamp()))
            .collect(Collectors.toList());
        
        LOG.debug("Sending {} events", numberOfEvents);
        
        return new GetActivityResponse()
            .setEvents(events);
    }
    

}
