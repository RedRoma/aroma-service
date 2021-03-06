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

import java.util.function.Function;
import javax.inject.Inject;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.CredentialRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.service.operations.encryption.AromaPasswordEncryptor;
import tech.aroma.service.operations.encryption.OverTheWireDecryptor;
import tech.aroma.thrift.*;
import tech.aroma.thrift.authentication.*;
import tech.aroma.thrift.authentication.service.*;
import tech.aroma.thrift.exceptions.*;
import tech.aroma.thrift.service.SignInRequest;
import tech.aroma.thrift.service.SignInResponse;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.lang.String.format;
import static tech.aroma.data.assertions.AuthenticationAssertions.completeToken;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.trueStatement;
import static tech.sirwellington.alchemy.arguments.assertions.PeopleAssertions.validEmailAddress;

/**
 * @author SirWellington
 */
@Internal
final class SignInOperation implements ThriftOperation<SignInRequest, SignInResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(SignInOperation.class);

    private final AuthenticationService.Iface authenticationService;
    private final Function<AuthenticationToken, UserToken> tokenMapper;

    private final CredentialRepository credentialsRepo;
    private final OverTheWireDecryptor decryptor;
    private final AromaPasswordEncryptor encryptor;
    private final UserRepository userRepo;

    @Inject
    SignInOperation(AuthenticationService.Iface authenticationService,
                    Function<AuthenticationToken, UserToken> tokenMapper,
                    CredentialRepository credentialsRepo,
                    OverTheWireDecryptor decryptor,
                    AromaPasswordEncryptor encryptor,
                    UserRepository userRepo)
    {
        checkThat(authenticationService, tokenMapper, credentialsRepo, decryptor, encryptor, userRepo)
                .are(notNull());

        this.authenticationService = authenticationService;
        this.tokenMapper = tokenMapper;
        this.credentialsRepo = credentialsRepo;
        this.decryptor = decryptor;
        this.encryptor = encryptor;
        this.userRepo = userRepo;
    }

    @Override
    public SignInResponse process(SignInRequest request) throws TException
    {
        LOG.debug("Received request to sign in: {}", request);

        checkThat(request)
                .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
                .is(good());

        User user = userRepo.getUserByEmail(request.emailAddress);
        checkThatUserHasCredentials(user);


        String password = decryptPasswordFrom(request);
        String digestedPassword = credentialsRepo.getEncryptedPassword(user.userId);

        checkPasswordsMatch(password, digestedPassword);

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
            throw new OperationFailedException("Could not create token. Authentication Service: " + ex.getMessage());
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

    private AlchemyAssertion<SignInRequest> good()
    {
        return request ->
        {
            checkThat(request)
                    .usingMessage("request missing")
                    .is(notNull());

            checkThat(request.credentials)
                    .usingMessage("request missing credentials")
                    .is(notNull());

            checkThat(request.credentials.isSet())
                    .usingMessage("Credentials missing")
                    .is(trueStatement());

            checkThat(request.emailAddress)
                    .usingMessage("Email address is invalid")
                    .is(validEmailAddress());

            checkThat(request.credentials.isSetAromaPassword())
                    .usingMessage("Only Passwords are currently accepted")
                    .is(trueStatement());
        };
    }

    private String decryptPasswordFrom(SignInRequest request) throws TException
    {
        String encryptedPassword = request.credentials.getAromaPassword().getEncryptedPassword();

        String decryptedPassword = decryptor.decrypt(encryptedPassword);

        return decryptedPassword;
    }

    private void checkPasswordsMatch(String password, String digestedPassword) throws TException
    {
        checkThat(encryptor.match(password, digestedPassword))
                .throwing(InvalidCredentialsException.class)
                .usingMessage("Email and Password do not match")
                .is(trueStatement());
    }

    private void checkThatUserHasCredentials(User user) throws UserDoesNotExistException
    {
        checkThat(user)
                .throwing(UserDoesNotExistException.class)
                .usingMessage("User has no password stored")
                .is(userWithCredentials());
    }

    private AlchemyAssertion<User> userWithCredentials()
    {
        return user ->
        {
            try
            {
                if (!credentialsRepo.containsEncryptedPassword(user.userId))
                {
                    throw new FailedAssertionException(format("User with ID %s has no saved Credentials", user.userId));
                }
            }
            catch (TException ex)
            {
                LOG.error("Failed to check for credentials of user [{}]", user.userId, ex);
                throw new FailedAssertionException(format("Could not determine is credentials exist for %s. : %s",
                                                          user.userId,
                                                          ex.getMessage()));
            }
        };
    }

}
