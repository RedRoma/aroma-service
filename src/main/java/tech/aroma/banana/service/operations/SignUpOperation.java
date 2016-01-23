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

import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.User;
import tech.aroma.banana.thrift.authentication.AuthenticationToken;
import tech.aroma.banana.thrift.authentication.TokenType;
import tech.aroma.banana.thrift.authentication.UserToken;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.banana.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.aroma.banana.thrift.service.SignUpRequest;
import tech.aroma.banana.thrift.service.SignUpResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.data.assertions.AuthenticationAssertions.completeToken;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
final class SignUpOperation implements ThriftOperation<SignUpRequest, SignUpResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(SignUpOperation.class);

    private final UserRepository userRepo;
    private final AuthenticationService.Iface authenticationService;
    private final Function<AuthenticationToken, UserToken> tokenMapper;

    @Inject
    SignUpOperation(UserRepository userRepo,
                    AuthenticationService.Iface authenticationService,
                    Function<AuthenticationToken, UserToken> tokenMapper)
    {
        checkThat(userRepo, authenticationService, tokenMapper)
            .are(notNull());

        this.userRepo = userRepo;
        this.authenticationService = authenticationService;
        this.tokenMapper = tokenMapper;
    }

    @Override
    public SignUpResponse process(SignUpRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        //Create User object
        String userId = UUID.randomUUID().toString();

        User user = new User()
            .setBirthdate(request.birthDate)
            .setEmail(request.email)
            .setFirstName(request.firstName)
            .setMiddleName(request.middleName)
            .setLastName(request.lastName)
            .setGithubProfile(request.githubProfile)
            .setName(request.name)
            .setProfileImage(request.profileImage)
            .setRoles(Sets.createFrom(request.mainRole))
            .setUserId(userId);

        //Store in Repository
        userRepo.saveUser(user);

        //Create and Acquire token from authentication service
        CreateTokenRequest authRequest = makeAuthenticationRequestToCreateToken(user);
        AuthenticationToken token = tryToGetTokenFromAuthenticationService(authRequest);

        UserToken userToken = convertToUserToken(token);

        return new SignUpResponse()
            .setUserId(userId)
            .setUserToken(userToken);
    }

    private AlchemyAssertion<SignUpRequest> good()
    {
        return request ->
            {
                checkThat(request)
                    .usingMessage("request is null")
                    .is(notNull());

                checkThat(request.firstName, request.lastName)
                    .usingMessage("first and last name are required")
                    .are(nonEmptyString());

                checkThat(request.mainRole)
                    .usingMessage("Your main role is required")
                    .is(notNull());

                if (request.isSetOrganizationId())
                {
                    checkThat(request.organizationId)
                        .usingMessage("organization ID must be a valid UUID type")
                        .is(validUUID());
                }

                //TODO: Add check on the email
            };
    }

    private CreateTokenRequest makeAuthenticationRequestToCreateToken(User user)
    {
        return new CreateTokenRequest()
            .setDesiredTokenType(TokenType.USER)
            .setOwnerId(user.userId)
            .setOwnerName(user.name);
    }

    private AuthenticationToken tryToGetTokenFromAuthenticationService(CreateTokenRequest authRequest) throws
        OperationFailedException
    {
        CreateTokenResponse response;
        try
        {
            response = authenticationService.createToken(authRequest);
        }
        catch (TException ex)
        {
            throw new OperationFailedException("Could not get token from the Authentication Service: " + ex.getMessage());
        }

        checkThat(response.token)
            .throwing(OperationFailedException.class)
            .usingMessage("Auth Service returned invalid token")
            .is(completeToken());

        return response.token;
    }

    private UserToken convertToUserToken(AuthenticationToken token)
    {
        return tokenMapper.apply(token);
    }

}
