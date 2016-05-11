/*
 * Copyright 2016 RedRoma, Inc.
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


import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jasypt.encryption.pbe.PBEStringEncryptor;
import org.jasypt.util.password.PasswordEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SirWellington
 */
class TestMaterials 
{
    private final static Logger LOG = LoggerFactory.getLogger(TestMaterials.class);
    
    private static final Injector GUICE = Guice.createInjector(new ModuleEncryptionMaterialsDev());
    
    static PasswordEncryptor newPasswordEncryptor()
    {
        return GUICE.getInstance(PasswordEncryptor.class);
    }
    
    static PBEStringEncryptor newStringEncryptor()
    {
        return GUICE.getInstance(PBEStringEncryptor.class);
    }

}
