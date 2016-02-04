
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
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.InboxRepository;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.Message;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.service.GetMessagesRequest;
import tech.aroma.banana.thrift.service.GetMessagesResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.util.stream.Collectors.toList;
import static tech.aroma.banana.data.assertions.RequestAssertions.validAppId;
import static tech.aroma.banana.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;

/**
 *
 * @author SirWellington
 */
final class GetMessagesOperation implements ThriftOperation<GetMessagesRequest, GetMessagesResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(GetMessagesOperation.class);

    private final ApplicationRepository appRepo;
    private final InboxRepository inboxRepo;
    private final MessageRepository messageRepo;
    private final UserRepository userRepo;

    @Inject
    GetMessagesOperation(ApplicationRepository appRepo,
                         InboxRepository inboxRepo,
                         MessageRepository messageRepo,
                         UserRepository userRepo)
    {
        checkThat(appRepo, inboxRepo, messageRepo, userRepo)
            .are(notNull());
        
        this.appRepo = appRepo;
        this.inboxRepo = inboxRepo;
        this.messageRepo = messageRepo;
        this.userRepo = userRepo;
    }

    @Override
    public GetMessagesResponse process(GetMessagesRequest request) throws TException
    {
        
        LOG.debug("Received request to get messages: {}", request);
        
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        String appId = request.applicationId;
        String userId = request.token.userId;

        Application app = appRepo.getById(appId);

        List<Message> messages = inboxRepo.getMessagesForUser(userId)
            .stream()
            .limit(1000)
            .collect(toList());

        LOG.debug("Found {} messages for user [{}] and App [{}]", messages.size(), userId, app);

        return new GetMessagesResponse(messages);
    }
    
    private AlchemyAssertion<GetMessagesRequest> good()
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
            
            checkThat(request.token.userId)
                .usingMessage("token UserID is invalid")
                .is(validUserId());
            
            if (request.isSetApplicationId())
            {
                checkThat(request.applicationId)
                    .is(validAppId());
            }
        };
    }

}
