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

import java.util.StringTokenizer;
import java.util.UUID;
import java.util.function.Function;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.data.CredentialRepository;
import tech.aroma.data.MediaRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.service.operations.encryption.AromaPasswordEncryptor;
import tech.aroma.service.operations.encryption.OverTheWireDecryptor;
import tech.aroma.thrift.Image;
import tech.aroma.thrift.User;
import tech.aroma.thrift.authentication.AuthenticationToken;
import tech.aroma.thrift.authentication.TokenType;
import tech.aroma.thrift.authentication.UserToken;
import tech.aroma.thrift.authentication.service.AuthenticationService;
import tech.aroma.thrift.authentication.service.CreateTokenRequest;
import tech.aroma.thrift.authentication.service.CreateTokenResponse;
import tech.aroma.thrift.exceptions.AccountAlreadyExistsException;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.aroma.thrift.exceptions.UserDoesNotExistException;
import tech.aroma.thrift.service.SignUpRequest;
import tech.aroma.thrift.service.SignUpResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.data.assertions.AuthenticationAssertions.completeToken;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.trueStatement;
import static tech.sirwellington.alchemy.arguments.assertions.PeopleAssertions.validEmailAddress;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthGreaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthLessThan;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
final class SignUpOperation implements ThriftOperation<SignUpRequest, SignUpResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(SignUpOperation.class);

    private final AuthenticationService.Iface authenticationService;

    private final CredentialRepository credentialsRepo;
    private final MediaRepository mediaRepo;
    private final UserRepository userRepo;
    
    private final Function<AuthenticationToken, UserToken> tokenMapper;

    private final OverTheWireDecryptor decryptor;
    private final AromaPasswordEncryptor passwordEncryptor;

    @Inject
    SignUpOperation(AuthenticationService.Iface authenticationService,
                    CredentialRepository credentialsRepo,
                    MediaRepository mediaRepo,
                    UserRepository userRepo,
                    Function<AuthenticationToken, UserToken> tokenMapper,
                    OverTheWireDecryptor decryptor,
                    AromaPasswordEncryptor passwordEncryptor)
    {
        checkThat(authenticationService, credentialsRepo, mediaRepo, userRepo, tokenMapper, decryptor, passwordEncryptor)
            .are(notNull());
        
        this.authenticationService = authenticationService;
        this.credentialsRepo = credentialsRepo;
        this.mediaRepo = mediaRepo;
        this.userRepo = userRepo;
        this.tokenMapper = tokenMapper;
        this.decryptor = decryptor;
        this.passwordEncryptor = passwordEncryptor;
    }
    
    @Override
    public SignUpResponse process(SignUpRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());

        checkThat(request.email)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(validEmailAddress())
            .throwing(AccountAlreadyExistsException.class)
            .usingMessage("Email is already in use")
            .is(notAlreadyInUse());

        //User IDs are always UUIDs
        String userId = UUID.randomUUID().toString();
        
        tryToSaveCredentialsFor(userId, request);
        
        
        User user = createUserFrom(request);
        user.userId = userId;
        
        if (request.isSetProfileImage())
        {
            tryToSaveProfileImage(request, user);
        }
        
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
            
            checkThat(request.name)
                .usingMessage("User's Name is required")
                .is(nonEmptyString())
                .usingMessage("User's Name too short")
                .is(stringWithLengthGreaterThanOrEqualTo(2))
                .usingMessage("User's name is too long")
                .is(stringWithLengthLessThan(100));
            
            checkThat(request.mainRole)
                .usingMessage("Your main role is required")
                .is(notNull());
            
            if (request.isSetOrganizationId())
            {
                checkThat(request.organizationId)
                    .usingMessage("organization ID must be a valid UUID type")
                    .is(validUUID());
            }
            
            checkThat(request.credentials)
                .usingMessage("request missing credentials")
                .is(notNull());
            
            checkThat(request.credentials.isSet())
                .usingMessage("request missing credentials")
                .is(trueStatement());
            
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

    private User createUserFrom(SignUpRequest request) throws InvalidArgumentException
    {
        
        if (isMissingNames(request))
        {
            tryToInferNames(request);
        }

        return new User()
            .setBirthdate(request.birthDate)
            .setEmail(request.email)
            .setFirstName(request.firstName)
            .setMiddleName(request.middleName)
            .setLastName(request.lastName)
            .setGithubProfile(request.githubProfile)
            .setName(request.name)
            .setProfileImage(request.profileImage)
            .setRoles(Sets.createFrom(request.mainRole));
    }
    
    private AlchemyAssertion<String> notAlreadyInUse()
    {
        return email ->
        {
            checkThat(email).is(nonEmptyString());
            
            try
            {
                User user = userRepo.getUserByEmail(email);
                throw new FailedAssertionException();
            }
            catch (UserDoesNotExistException ex)
            {
                //Good
            }
            catch (TException ex)
            {
                throw new FailedAssertionException("could not check for existence of email: " + email);
            }
        };
    }

    private boolean isMissingNames(SignUpRequest request)
    {

        if (request.isSetFirstName() && request.isSetLastName())
        {
            return false;
        }

        if (request.isSetFirstName())
        {
            return false;
        }

        return true;
    }

    private void tryToInferNames(SignUpRequest request) throws InvalidArgumentException
    {
        String fullName = request.name;

        StringTokenizer tokenizer = new StringTokenizer(fullName);
        int numberOfTokens = tokenizer.countTokens();

        switch (numberOfTokens)
        {
            case 0:
                throw new InvalidArgumentException("Full Name is missing from request");
            case 1:
                request.firstName = tokenizer.nextToken();
                break;
            case 2:
                request.firstName = tokenizer.nextToken();
                request.lastName = tokenizer.nextToken();
                break;
            case 3:
                request.firstName = tokenizer.nextToken();
                request.middleName = tokenizer.nextToken();
                request.lastName = tokenizer.nextToken();
                break;
            default:
                LOG.warn("Name has more than 4 tokens. Skipping some. [{}]", fullName);
                request.firstName = tokenizer.nextToken();
                request.middleName = tokenizer.nextToken();
                request.lastName = determineLastTokenFrom(tokenizer);
                break;
        }
    }

    private String determineLastTokenFrom(StringTokenizer tokenizer)
    {
        String lastToken = tokenizer.nextToken();

        while (tokenizer.hasMoreTokens())
        {
            lastToken = tokenizer.nextToken();
        }

        return lastToken;
    }

    private void tryToSaveCredentialsFor(String userId, SignUpRequest request) throws InvalidCredentialsException, TException
    {
        //Try to decrypt the over-the-wire password
        //Run it through the password hashing algorithm
        //Store the credentials
        String encryptedPassword = request.credentials.getAromaPassword().getEncryptedPassword();
        
        checkThat(encryptedPassword)
            .throwing(InvalidCredentialsException.class)
            .usingMessage("request credentials encrypted password")
            .is(nonEmptyString());
        
        String password = decryptor.decrypt(encryptedPassword);
        LOG.debug("Password successfully decrypted over the wire");
        
        String digestedPassword = passwordEncryptor.encryptPassword(password);
        LOG.debug("Password successfully encrypted and digested");
        
        credentialsRepo.saveEncryptedPassword(userId, digestedPassword);
        LOG.debug("Password successfully stored");
    }

    private void tryToSaveProfileImage(SignUpRequest request, User user)
    {
        Image profileImage = request.profileImage;
        
        try
        {
            mediaRepo.saveMedia(user.userId, profileImage);
            user.profileImageLink = user.userId;
        }
        catch (Exception ex)
        {
            LOG.error("Failed to save User Profile Image: {}", user, ex);
        }
    }
}
