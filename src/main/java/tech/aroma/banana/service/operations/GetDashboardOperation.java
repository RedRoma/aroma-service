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

import java.util.List;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.service.GetDashboardRequest;
import tech.aroma.banana.thrift.service.GetDashboardResponse;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.generator.ObjectGenerators;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;

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
        checkThat(request)
            .throwing(InvalidArgumentException.class)
            .is(notNull());

        LOG.debug("Received request to get Dashboard: {}", request);

        GetDashboardResponse response = new GetDashboardResponse();

        AlchemyGenerator<Integer> integers = NumberGenerators.integers(0, 100);

        response.setTotalMessagesLastHour(integers.get())
            .setTotalMessagesLast24hrs(integers.get())
            .setUnreadMessageCount(integers.get());

        int numberOfMessages = one(integers(0, 10));

        if (numberOfMessages > 0)
        {
            List<Message> messages = listOf(ObjectGenerators.pojos(Message.class), numberOfMessages);
            response.setRecentMessages(messages);
        }

        return response;
    }

}
