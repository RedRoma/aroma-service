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


package tech.aroma.service.operations.encryption;

import com.google.inject.ImplementedBy;
import org.apache.thrift.TException;
import org.jasypt.util.password.PasswordEncryptor;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;


/**
 * Digests and Encrypts a Password in a safe manner.
 * 
 * @author SirWellington
 */
@Internal
@ImplementedBy(AromaPasswordEncryptorImpl.class)
public interface AromaPasswordEncryptor 
{
    /**
     * 
     * @param password The password to encrypt.
     * @return The Digested and securely encrypted password.
     * 
     * @throws TException 
     */
    String encryptPassword(@NonEmpty String password) throws TException;
    
    /**
     * Determines if the raw candidate password matches the digested password.
     * 
     * @param plainPassword The password to check.
     * @param existingDigestedPassword The encrypted digest of the known accurate password.
     * 
     * @return True if they match, false otherwise.
     * 
     * @throws TException If the Operation Failes.
     */
    boolean match(@NonEmpty String plainPassword, @NonEmpty String existingDigestedPassword) throws TException;
    
    public static AromaPasswordEncryptor newInstance(@Required PasswordEncryptor jasyptEncryptor)
    {
        checkThat(jasyptEncryptor).is(notNull());
        
        return new AromaPasswordEncryptorImpl(jasyptEncryptor);
    }
}
