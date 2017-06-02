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

import java.util.Set;
import javax.inject.Inject;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.InboxRepository;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.DismissMessageRequest;
import tech.aroma.thrift.service.DismissMessageResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.RequestAssertions.validMessageId;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.*;

/**
 * @author SirWellington
 */
final class DismissMessageOperation implements ThriftOperation<DismissMessageRequest, DismissMessageResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(DismissMessageOperation.class);
    private final InboxRepository inboxRepo;

    @Inject
    DismissMessageOperation(InboxRepository inboxRepo)
    {
        checkThat(inboxRepo).is(notNull());

        this.inboxRepo = inboxRepo;
    }

    @Override
    public DismissMessageResponse process(DismissMessageRequest request) throws TException
    {
        checkThat(request)
                .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
                .is(good());

        String userId = request.token.userId;

        long count;
        if (request.dismissAll)
        {
            count = clearInboxFor(userId);
        }
        else
        {
            Set<String> messageIds = getAllMessageIdsFrom(request);

            deleteMessages(userId, messageIds);
            return new DismissMessageResponse().setMessagesDismissed(messageIds.size());
        }

        return new DismissMessageResponse().setMessagesDismissed((int) count);
    }

    private AlchemyAssertion<DismissMessageRequest> good()
    {
        return request ->
        {
            checkThat(request)
                    .is(notNull());

            checkThat(request.token)
                    .is(notNull());

            checkThat(request.token.userId)
                    .usingMessage("token is missing userId")
                    .is(nonEmptyString())
                    .usingMessage("token userId must be a UUID")
                    .is(validUUID());

            if (request.isSetMessageId())
            {
                checkThat(request.messageId)
                        .is(validMessageId());
            }

            if (request.isSetMessageIds())
            {
                request.messageIds
                        .parallelStream()
                        .forEach(id -> checkThat(id).is(validMessageId()));
            }
        };
    }

    private long clearInboxFor(String userId) throws TException
    {
        long count = inboxRepo.countInboxForUser(userId);
        inboxRepo.deleteAllMessagesForUser(userId);

        LOG.debug("Deleted {} messages from Inbox of User [{}]", count, userId);
        return count;
    }

    private Set<String> getAllMessageIdsFrom(DismissMessageRequest request)
    {
        Set<String> result = Sets.copyOf(request.messageIds);
        result.add(request.messageId);

        return result;
    }

    private void deleteMessages(String userId, Set<String> messageIds)
    {
        messageIds.parallelStream()
                  .forEach(msgId -> this.deleteMessage(userId, msgId));
    }

    private void deleteMessage(String userId, String msgId)
    {
        try
        {
            inboxRepo.deleteMessageForUser(userId, msgId);
        }
        catch (TException ex)
        {
            LOG.warn("Failed to delete message {} for user {}", msgId, userId, ex);
        }
    }
}
