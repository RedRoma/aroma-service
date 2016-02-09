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

package tech.aroma.banana.service.operations.encryption;

import javax.inject.Inject;
import org.apache.thrift.TException;
import org.jasypt.util.password.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.banana.thrift.exceptions.OperationFailedException;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

@Internal
final class AromaPasswordEncryptorImpl implements AromaPasswordEncryptor
{

    private final static Logger LOG = LoggerFactory.getLogger(AromaPasswordEncryptorImpl.class);
    
    private PasswordEncryptor encryptor;
    @Inject
    AromaPasswordEncryptorImpl(PasswordEncryptor encryptor)
    {
        checkThat(encryptor).is(notNull());
        
        this.encryptor = encryptor;
    }
    
    @Override
    public String encryptPassword(String password) throws TException
    {
        checkThat(password)
            .throwing(InvalidCredentialsException.class)
            .is(nonEmptyString());
        
        try
        {
            return encryptor.encryptPassword(password);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to digest and encrypt password", ex);
            throw new InvalidCredentialsException("Could not digest password: " + ex.getMessage());
        }
    }
    
    @Override
    public boolean match(String plainPassword, String existingDigestedPassword) throws TException
    {
        checkThat(plainPassword, existingDigestedPassword)
            .throwing(InvalidCredentialsException.class)
            .usingMessage("credentials cannot be empty")
            .are(nonEmptyString());
        
        try
        {
            return encryptor.checkPassword(plainPassword, existingDigestedPassword);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to check if candidate matches existing digest", ex);
            throw new OperationFailedException("Could not check credentials: " + ex.getMessage());
        }
    }
    
}
