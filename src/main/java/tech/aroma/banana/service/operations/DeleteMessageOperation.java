/*
 * Copyright 2016 Aroma Tech.
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
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.MessageRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.UnauthorizedException;
import tech.aroma.banana.thrift.service.DeleteMessageRequest;
import tech.aroma.banana.thrift.service.DeleteMessageResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.data.assertions.RequestAssertions.validAppId;
import static tech.aroma.banana.data.assertions.RequestAssertions.validMessageId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.elementInCollection;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
final class DeleteMessageOperation implements ThriftOperation<DeleteMessageRequest, DeleteMessageResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(DeleteMessageOperation.class);

    private final ApplicationRepository appRepo;
    private final MessageRepository messageRepo;

    @Inject
    DeleteMessageOperation(ApplicationRepository appRepo, MessageRepository messageRepo)
    {
        checkThat(appRepo, messageRepo)
            .are(notNull());
        
        this.appRepo = appRepo;
        this.messageRepo = messageRepo;
    }
    
    @Override
    public DeleteMessageResponse process(DeleteMessageRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
            
        List<String> messagesToDelete = Lists.create();
        String appId = request.applicationId;
        String userId = request.token.userId;
        Application app = appRepo.getById(appId);

        checkThat(userId)
            .usingMessage("Not Authorized to delete messages for App")
            .throwing(UnauthorizedException.class)
            .is(elementInCollection(app.owners));
        
        if(request.isSetMessageId())
        {
            messagesToDelete.add(request.messageId);
        }
        
        if(request.isSetMessageIds())
        {
            messagesToDelete.addAll(request.messageIds);
        }
        
        for(String messageId : messagesToDelete)
        {
            messageRepo.deleteMessage(appId, messageId);
        }
        
        LOG.debug("Deleted {} messages for App [{]]", messagesToDelete.size(), appId);
        
        return new DeleteMessageResponse()
            .setMessagesDeleted(messagesToDelete.size());
    }

    private AlchemyAssertion<DeleteMessageRequest> good()
    {
        return request ->
        {
            checkThat(request)
                .usingMessage("request is null")
                .is(notNull());
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
            
            checkThat(request.token.userId)
                .usingMessage("request missing userId in Token")
                .is(nonEmptyString());
            
            checkThat(request.applicationId)
                .is(validAppId());
            
            if(request.isSetMessageId())
            {
                checkThat(request.messageId)
                    .is(validMessageId());
            }
            
            if(request.isSetMessageIds())
            {
                for (String messageId : request.messageIds)
                {
                    checkThat(messageId)
                        .is(validMessageId());
                }
            }
        };
    }

}
