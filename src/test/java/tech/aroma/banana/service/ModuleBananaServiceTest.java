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

package tech.aroma.banana.service;

import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.banana.data.memory.ModuleMemoryDataRepositories;
import tech.aroma.banana.service.operations.ModuleBananaServiceOperations;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * This Test Class can be considered an Integration level test, because it tests the validity of
 * the Dependency Injection Framework and Object Graph.
 * 
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class ModuleBananaServiceTest 
{
    
    private ModuleBananaServiceOperations operationsModule;
    private ModuleMemoryDataRepositories dataModule;
    private ModuleBananaService instance;
    
    @Before
    public void setUp()
    {
        dataModule = new ModuleMemoryDataRepositories();
        instance = new ModuleBananaService();
    }

    @Test
    public void testConfigure()
    {
        ModuleBananaServiceOperations operations = new ModuleBananaServiceOperations();
        Guice.createInjector(operations, dataModule, instance);
    }

    @Test
    public void testProvideAlchemyHttpClient()
    {
        AlchemyHttp result = instance.provideAlchemyHttpClient();
        assertThat(result, notNullValue());
    }

}