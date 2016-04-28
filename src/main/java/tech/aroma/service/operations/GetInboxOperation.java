
/*
 * Copyright 2016 RedRoma.
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

import java.util.Comparator;
import java.util.List;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.InboxRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetInboxRequest;
import tech.aroma.thrift.service.GetInboxResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;

/**
 *
 * @author SirWellington
 */
final class GetInboxOperation implements ThriftOperation<GetInboxRequest, GetInboxResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(GetInboxOperation.class);

    private final InboxRepository inboxRepo;
    private final UserRepository userRepo;

    @Inject
    GetInboxOperation(InboxRepository inboxRepo,
                      UserRepository userRepo)
    {
        checkThat(inboxRepo, userRepo)
            .are(notNull());

        this.inboxRepo = inboxRepo;
        this.userRepo = userRepo;
    }

    @Override
    public GetInboxResponse process(GetInboxRequest request) throws TException
    {

        LOG.debug("Received request to get messages: {}", request);

        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        String userId = request.token.userId;
        int limit = request.limit == 0 ? 2000 : request.limit;

        List<Message> messages = inboxRepo.getMessagesForUser(userId)
            .parallelStream()
            .sorted(Comparator.comparingLong(Message::getTimeMessageReceived).reversed())
            .limit(limit)
            .collect(toList());

        LOG.debug("Found {} messages for user [{}] ", messages.size(), userId);

        return new GetInboxResponse(messages);
    }

    private AlchemyAssertion<GetInboxRequest> good()
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
            
        };
    }

}
