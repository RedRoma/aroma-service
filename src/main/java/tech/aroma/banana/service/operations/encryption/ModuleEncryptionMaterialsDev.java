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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.jasypt.digest.StandardStringDigester;
import org.jasypt.digest.StringDigester;
import org.jasypt.digest.config.DigesterConfig;
import org.jasypt.digest.config.SimpleStringDigesterConfig;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.salt.RandomSaltGenerator;
import org.jasypt.salt.SaltGenerator;
import org.jasypt.util.password.ConfigurablePasswordEncryptor;
import org.jasypt.util.password.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.authentication.AuthenticationConstants;

/**
 *
 * @author SirWellington
 */
public class ModuleEncryptionMaterialsDev extends AbstractModule
{

    private final static Logger LOG = LoggerFactory.getLogger(ModuleEncryptionMaterialsDev.class);
    private final static String FIXED_SALT = "fu3opkvp2l1`2890)W480129LWH*@#&$@(*hvd;lkf;l2i9ivod snfkjlq3hfu090fu309jrlkdafm l;Afj;KDL:-39UFDN";

    @Override
    protected void configure()
    {
    }

    //=================================Password Functions===================================
    @Provides
    DigesterConfig providePasswordConfig()
    {
        SimpleStringDigesterConfig config = new SimpleStringDigesterConfig();
        config.setAlgorithm("SHA-256");
        config.setIterations(100_101);
        config.setSaltSizeBytes(20);
        config.setPoolSize(10);
        return config;
    }

    @Provides
    PasswordEncryptor providePasswordEncryptor(DigesterConfig config)
    {
        ConfigurablePasswordEncryptor encryptor = new ConfigurablePasswordEncryptor();
        encryptor.setConfig(config);
        return encryptor;
    }

    @Provides
    SaltGenerator provideSaltGenerator()
    {
        return new RandomSaltGenerator();
//        return new StringFixedSaltGenerator(FIXED_SALT);
    }
    
    @Provides
    SimpleStringDigesterConfig provideStringDigesterConfig(SaltGenerator salt)
    {
        SimpleStringDigesterConfig config = new SimpleStringDigesterConfig();
        config.setAlgorithm("SHA-1");
        config.setIterations(1_000);
        config.setPoolSize(10);
        config.setSaltGenerator(salt);
        return config;
    }

    @Provides
    StringDigester provideIdentityHashingFunction(SimpleStringDigesterConfig config)
    {
        StandardStringDigester digester = new StandardStringDigester();
        digester.setConfig(config);
        return digester;
    }

    @Provides
    PBEStringEncryptor provideOverTheWireDecryptor()
    {
        StandardPBEStringEncryptor decryptor = new StandardPBEStringEncryptor();
        decryptor.setPassword(AuthenticationConstants.OVER_THE_WIRE_PASSWORD_ENCRYPTION_KEY);
        decryptor.setAlgorithm("PBEWithMD5AndDES");
        return decryptor;
    }

}
