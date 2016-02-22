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
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.exceptions.InvalidCredentialsException;
import tech.aroma.thrift.exceptions.OperationFailedException;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@Internal
final class OverTheWireDecryptorImpl implements OverTheWireDecryptor
{

    private final static Logger LOG = LoggerFactory.getLogger(OverTheWireDecryptorImpl.class);

    private final PBEStringEncryptor decryptor;

    @Inject
    OverTheWireDecryptorImpl(PBEStringEncryptor decryptor)
    {
        checkThat(decryptor)
            .is(notNull());

        this.decryptor = decryptor;
    }

    @Override
    public String decrypt(String encrypredString) throws TException
    {
        checkThat(encrypredString)
            .throwing(InvalidCredentialsException.class)
            .usingMessage("encrypted string is missing")
            .is(nonEmptyString());
        
        try
        {
            return decryptor.decrypt(encrypredString);
        }
        catch (Exception ex)
        {
            LOG.warn("Could not decrypt String {}", encrypredString, ex);
            throw new OperationFailedException("Could not decrypt message: " + ex.getMessage());
        }
    }

}
