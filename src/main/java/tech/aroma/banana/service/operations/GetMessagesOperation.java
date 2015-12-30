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


import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.Urgency;
import tech.aroma.banana.thrift.service.GetMessagesRequest;
import tech.aroma.banana.thrift.service.GetMessagesResponse;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.service.BananaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.EnumGenerators.enumValueOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.PeopleGenerators.names;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphanumericString;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.generator.TimeGenerators.pastInstants;

/**
 *
 * @author SirWellington
 */
final class GetMessagesOperation implements ThriftOperation<GetMessagesRequest, GetMessagesResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetMessagesOperation.class);

    @Override
    public GetMessagesResponse process(GetMessagesRequest request) throws TException
    {
        checkNotNull(request);
        
        GetMessagesResponse response = new GetMessagesResponse();
        
        int max;
        int numberOfMessages;
        
        if (hasLimit(request))
        {
            max = request.limit;
        }
        else
        {
            max = 1000;
        }
        
        numberOfMessages = one(integers(0, max));
        LOG.debug("Sending back {} messages", numberOfMessages);
        response.setMessages(listOf(messages(), numberOfMessages));
        
        return response;
    }
    
    private AlchemyGenerator<Message> messages()
    {
        return () ->
        {
            int bodyLength = one(integers(10, 1_000));
            
            return new Message()
                .setMessageId(one(hexadecimalString(16)))
                .setApplicationName(one(names()))
                .setBody(one(alphabeticString(bodyLength)))
                .setHostname(one(alphanumericString()))
                .setUrgency(enumValueOf(Urgency.class).get())
                .setTimeMessageReceived(one(pastInstants()).toEpochMilli())
                ;
        };
    }

    private boolean hasLimit(GetMessagesRequest request)
    {
        return request.limit > 0;
    }

}
