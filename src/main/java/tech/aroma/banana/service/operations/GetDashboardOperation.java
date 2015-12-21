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

package tech.aroma.banana.service.operations;

import java.time.Instant;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.Urgency;
import tech.aroma.banana.thrift.service.GetDashboardRequest;
import tech.aroma.banana.thrift.service.GetDashboardResponse;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.EnumGenerators;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.generator.PeopleGenerators;
import tech.sirwellington.alchemy.generator.StringGenerators;
import tech.sirwellington.alchemy.generator.TimeGenerators;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.service.BananaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;

/**
 *
 * @author SirWellington
 */
final class GetDashboardOperation implements ThriftOperation<GetDashboardRequest, GetDashboardResponse>
{
    
    private final static Logger LOG = LoggerFactory.getLogger(GetDashboardOperation.class);
    
    @Override
    public GetDashboardResponse process(GetDashboardRequest request) throws TException
    {
        checkNotNull(request);
        
        LOG.debug("Received request to get Dashboard: {}", request);
        
        return responses.get();
    }
    
    private final AlchemyGenerator<String> names = PeopleGenerators.names();
    
    private final AlchemyGenerator<Integer> messageCounters = NumberGenerators.integers(0, 3_000);
    
    private final AlchemyGenerator<Integer> numberOfMessages = NumberGenerators.integers(0, 6);
    
    private final AlchemyGenerator<Instant> times = TimeGenerators.pastInstants();
    
    private final AlchemyGenerator<Urgency> urgencies = EnumGenerators.enumValueOf(Urgency.class);
    
    private final AlchemyGenerator<String> hostnames = () ->
    {
        AlchemyGenerator<String> networkNames = StringGenerators.alphabeticString(5);
        
        return new StringBuilder()
            .append(networkNames.get())
            .append(".")
            .append(networkNames.get())
            .append(".")
            .append(networkNames.get())
            .append(".")
            .toString();
    };
    
    private final AlchemyGenerator<Message> messages = () ->
    {
        
        return new Message()
            .setApplicationName(names.get())
            .setBody(StringGenerators.alphabeticString().get())
            .setTimeMessageReceived(times.get().toEpochMilli())
            .setMessageId(one(uuids))
            .setTimeMessageSent(times.get().toEpochMilli())
            .setUrgency(urgencies.get())
            .setHostname(hostnames.get())
            .setMacAddress(StringGenerators.hexadecimalString(10).get())
            ;
        
    };
    
    private final AlchemyGenerator<GetDashboardResponse> responses = () ->
    {
        return new GetDashboardResponse()
            .setTotalMessagesLast24hrs(messageCounters.get())
            .setTotalMessagesLastHour(messageCounters.get())
            .setUnreadMessageCount(messageCounters.get())
            .setRecentMessages(listOf(messages, numberOfMessages.get()))
            ;
    };
    
}
