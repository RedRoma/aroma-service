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


import java.util.List;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ReactionRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.reactions.Reaction;
import tech.aroma.thrift.service.GetReactionsRequest;
import tech.aroma.thrift.service.GetReactionsResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.validApplicationId;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.trueStatement;

/**
 *
 * @author SirWellington
 */
final class GetReactionsOperation implements ThriftOperation<GetReactionsRequest, GetReactionsResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetReactionsOperation.class);

    private final ReactionRepository reactionsRepo;
    private final UserRepository userRepo;

    @Inject
    GetReactionsOperation(ReactionRepository reactionsRepo, UserRepository userRepo)
    {
        checkThat(reactionsRepo, userRepo)
            .are(notNull());
        
        this.reactionsRepo = reactionsRepo;
        this.userRepo = userRepo;
    }
    
    @Override
    public GetReactionsResponse process(GetReactionsRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        String userId = request.token.userId;
        
        checkThat(userRepo.containsUser(userId))
            .throwing(UserDoesNotExistException.class)
            .is(trueStatement());
        
        List<Reaction> reactions;
        
        if (request.isSetForAppId())
        {
            String appId = request.forAppId;
            
            reactions = reactionsRepo.getReactionsForApplication(appId)
                .stream()
                .collect(toList());
            
            LOG.debug("Found {} reactions stored for app {}", reactions.size(), appId);
        }
        else        
        {
            reactions = reactionsRepo.getReactionsForUser(userId)
                .stream()
                .collect(toList());
            
            LOG.debug("Found {} reactions stored for user {}", reactions.size(), userId);
        }
        
        
        return new GetReactionsResponse().setReactions(reactions);
    }
    
    private AlchemyAssertion<GetReactionsRequest> good()
    {
        return request ->
        {
            checkThat(request).is(notNull());
            
            checkThat(request.token)
                .is(notNull());
            
            checkThat(request.token.userId)
                .is(validUserId());
            
            if (request.isSetForAppId())
            {
                checkThat(request.forAppId)
                    .is(validApplicationId());
            }
        };
    }
    
    private AlchemyAssertion<String> userIdInRepo(UserRepository userRepo)
    {
        return userId ->
        {
            try
            {
                if (!userRepo.containsUser(userId))
                {
                    throw new FailedAssertionException("User with ID does not exist: " + userId);
                }
            }
            catch (TException ex)
            {
                LOG.error("Failed to check if userID [{}] exists", userId, ex);
                throw new FailedAssertionException("Could not check if userId exists: " + ex.getMessage());
            }
        };
    }
    
}
