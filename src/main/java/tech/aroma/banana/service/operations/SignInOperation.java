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

import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.LengthOfTime;
import tech.aroma.banana.thrift.TimeUnit;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.TokenType;
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.banana.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.service.SignInRequest;
import tech.aroma.banana.thrift.service.SignInResponse;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.data.assertions.AuthenticationAssertions.completeToken;
import static tech.aroma.banana.service.BananaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
final class SignInOperation implements ThriftOperation<SignInRequest, SignInResponse>
{
    
    private final static Logger LOG = LoggerFactory.getLogger(SignInOperation.class);
    
    private final AuthenticationService.Iface authenticationService;
    private final Function<AuthenticationToken, UserToken> tokenMapper;
    private final UserRepository userRepo;
    
    @Inject
    SignInOperation(AuthenticationService.Iface authenticationService,
                    Function<AuthenticationToken, UserToken> tokenMapper, UserRepository userRepo)
    {
        checkThat(authenticationService, tokenMapper, userRepo)
            .are(notNull());
        
        this.authenticationService = authenticationService;
        this.tokenMapper = tokenMapper;
        this.userRepo = userRepo;
    }
    
    @Override
    public SignInResponse process(SignInRequest request) throws TException
    {
        checkNotNull(request);

        //Get User ID By email
        //Retrieve stored credentials 
        //Check credentials
        //If good create token
        //Return token
        LOG.info("Received request to sign in: {}", request);
        
        User user = userRepo.getUserByEmail(request.emailAddress);
        
        AuthenticationToken authToken = getTokenFor(user);
        
        UserToken userToken = tokenMapper.apply(authToken);
        
        return new SignInResponse()
            .setUserToken(userToken);
    }
    
    private AuthenticationToken getTokenFor(User user) throws OperationFailedException
    {
        LengthOfTime tokenLifetime = new LengthOfTime()
            .setUnit(TimeUnit.DAYS)
            .setValue(60);
        
        CreateTokenRequest request = new CreateTokenRequest()
            .setOwnerId(user.userId)
            .setOwnerName(user.name)
            .setDesiredTokenType(TokenType.USER)
            .setLifetime(tokenLifetime);
        
        CreateTokenResponse response;
        try
        {
            response = authenticationService.createToken(request);
        }
        catch (Exception ex)
        {
            LOG.error("Authentication Service request failed: {}", request, ex);
            throw new OperationFailedException("Could not create token: " + ex.getMessage());
        }
        
        checkThat(response)
            .usingMessage("Authentication Service returned null")
            .throwing(OperationFailedException.class)
            .is(notNull());
        
        checkThat(response.token)
            .throwing(OperationFailedException.class)
            .usingMessage("Authentication Service returned incomplete token")
            .is(completeToken());
        
        return response.getToken();
    }
    
}
