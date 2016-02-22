
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

import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.service.GetApplicationMessagesRequest;
import tech.aroma.banana.thrift.service.GetApplicationMessagesResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.util.stream.Collectors.toList;
import static tech.aroma.banana.data.assertions.RequestAssertions.validApplicationId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;

/**
 *
 * @author SirWellington
 */
final class GetApplicationMessagesOperation implements ThriftOperation<GetApplicationMessagesRequest, GetApplicationMessagesResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(GetApplicationMessagesOperation.class);

    private final ApplicationRepository appRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;

    @Inject
    GetApplicationMessagesOperation(ApplicationRepository appRepo, MessageRepository messageRepo, UserRepository userRepo)
    {
        checkThat(appRepo, messageRepo, userRepo)
            .are(notNull());
        
        this.appRepo = appRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
    }


    @Override
    public GetApplicationMessagesResponse process(GetApplicationMessagesRequest request) throws TException
    {
        LOG.debug("Received request to get application messages: {}", request);

        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        String appId = request.applicationId;
        int limit = request.limit == 0 ? 2000 : request.limit;

        List<Message> messages = messageRepo.getByApplication(appId)
            .stream()
            .sorted(Comparator.comparingLong(Message::getTimeMessageReceived).reversed())
            .limit(limit)
            .collect(toList());

        LOG.debug("Found {} messages for Application [{}] ", messages.size(), appId);

        return new GetApplicationMessagesResponse(messages);
    }

    private AlchemyAssertion<GetApplicationMessagesRequest> good()
    {
        return request ->
        {
            checkThat(request)
                .usingMessage("request is null")
                .is(notNull());
            
            checkThat(request.limit)
                .usingMessage("Limit must be >= 0")
                .is(greaterThanOrEqualTo(0));
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
            
            checkThat(request.applicationId)
                .usingMessage("applicationId is invalid")
                .is(validApplicationId());
        };
    }

}
