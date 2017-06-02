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


import javax.inject.Inject;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.*;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.Message;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.MessageDoesNotExistException;
import tech.aroma.thrift.service.GetFullMessageRequest;
import tech.aroma.thrift.service.GetFullMessageResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validMessageId;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * @author SirWellington
 */
final class GetFullMessageOperation implements ThriftOperation<GetFullMessageRequest, GetFullMessageResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetFullMessageOperation.class);

    private final ApplicationRepository appRepo;
    private final FollowerRepository followerRepo;
    private final MessageRepository messageRepo;

    @Inject
    GetFullMessageOperation(ApplicationRepository appRepo, FollowerRepository followerRepo, MessageRepository messageRepo)
    {
        checkThat(appRepo, followerRepo, messageRepo)
                .are(notNull());

        this.appRepo = appRepo;
        this.followerRepo = followerRepo;
        this.messageRepo = messageRepo;
    }

    @Override
    public GetFullMessageResponse process(GetFullMessageRequest request) throws TException
    {
        checkThat(request)
                .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
                .is(good());

        String appId = request.applicationId;
        String messageId = request.messageId;
        String userId = request.token.userId;

        Application app = appRepo.getById(appId);

        if (!userCanViewAppMessages(userId, app))
        {
            throw new MessageDoesNotExistException(request.messageId);
        }

        Message message = messageRepo.getMessage(appId, messageId);

        return new GetFullMessageResponse(message);
    }

    private AlchemyAssertion<GetFullMessageRequest> good()
    {
        return request ->
        {
            checkThat(request)
                    .usingMessage("missing request")
                    .is(notNull());

            checkThat(request.messageId)
                    .is(validMessageId());

            checkThat(request.applicationId)
                    .is(validApplicationId());
        };

    }

    private boolean userCanViewAppMessages(String userId, Application app) throws TException
    {
        if (app.owners.contains(userId))
        {
            return true;
        }

        String appId = app.applicationId;

        try
        {
            return followerRepo.followingExists(userId, appId);
        }
        catch (TException ex)
        {
            LOG.error("Failed to check if user [{}] is following App [{}]", userId, appId);
            throw ex;
        }

    }

}
