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


package tech.aroma.service.operations.encryption;

import com.google.inject.ImplementedBy;
import org.apache.thrift.TException;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;


/**
 * This interface is responsible for decrypting passwords and emails sent over the Wire
 * from RedRoma Applications.
 * Red Roma
 * RedRoma
 * 
 * @author SirWellington
 */
@FunctionalInterface
@StrategyPattern(role = INTERFACE)
@ImplementedBy(OverTheWireDecryptorImpl.class)
public interface OverTheWireDecryptor
{

    /**
     * Takes an Encrypted String and decrypts it for consumption.
     * 
     * @param encrypredString The String to Decrypt
     * @return 
     * @throws org.apache.thrift.TException If the String could not be decrypted.
     */
    public String decrypt(String encrypredString) throws TException;
    
    public static OverTheWireDecryptor newInstance(@Required PBEStringEncryptor stringEncryptor)
    {
        checkThat(stringEncryptor).is(notNull());
        
        return new OverTheWireDecryptorImpl(stringEncryptor);
    }
    
}
