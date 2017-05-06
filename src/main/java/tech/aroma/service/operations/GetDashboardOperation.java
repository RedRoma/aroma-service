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

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.InboxRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.Urgency;
import tech.aroma.thrift.service.GetDashboardRequest;
import tech.aroma.thrift.service.GetDashboardResponse;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.EnumGenerators;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.generator.PeopleGenerators;
import tech.sirwellington.alchemy.generator.StringGenerators;
import tech.sirwellington.alchemy.generator.TimeGenerators;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.util.stream.Collectors.toList;
import static tech.aroma.service.AromaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
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
    private final InboxRepository inboxRepo;

    @Inject
    GetDashboardOperation(InboxRepository inboxRepo)
    {
        checkThat(inboxRepo)
            .is(notNull());
        
        this.inboxRepo = inboxRepo;
    }
    
    @Override
    public GetDashboardResponse process(GetDashboardRequest request) throws TException
    {
        checkNotNull(request);
        
        LOG.debug("Received request to get Dashboard: {}", request);
        
        String userId = request.token.userId;
        
        List<Message> recentMessages = inboxRepo.getMessagesForUser(userId)
            .stream()
            .sorted(Comparator.comparingLong(Message::getTimeMessageReceived).reversed())
            .limit(3)
            .collect(toList());
        
        GetDashboardResponse response = responses.get();
        response.setRecentMessages(recentMessages);
        
        return response;
    }
    
    private final AlchemyGenerator<String> names = PeopleGenerators.names();
    
    private final AlchemyGenerator<Integer> messageCounters = NumberGenerators.integers(0, 150);
    
    private final AlchemyGenerator<Integer> numberOfMessages = NumberGenerators.integers(0, 6);
    
    private final AlchemyGenerator<Instant> times = TimeGenerators.pastInstants();
    
    private final AlchemyGenerator<Urgency> urgencies = EnumGenerators.enumValueOf(Urgency.class);
    
    private final AlchemyGenerator<String> hostnames = () ->
    {
        AlchemyGenerator<String> domains = PeopleGenerators.popularEmailDomains();
        
        return new StringBuilder()
            .append(StringGenerators.alphabeticString(7).get())
            .append(".")
            .append(domains.get())
            .toString();
    };
    
    private final AlchemyGenerator<Message> messages = () ->
    {
        
        return new Message()
            .setApplicationName(names.get())
            .setBody(StringGenerators.alphabeticString().get())
            .setTimeMessageReceived(times.get().toEpochMilli())
            .setMessageId(one(uuids))
            .setTimeOfCreation(times.get().toEpochMilli())
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
            .setNumberOfHighUrgencyMessages(messageCounters.get())
            .setNumberOfMediumUrgencyMessages(messageCounters.get())
            .setNumberOfLowUrgencyMessages(messageCounters.get())
            .setRecentMessages(listOf(messages, numberOfMessages.get()))
            ;
    };
    
}
