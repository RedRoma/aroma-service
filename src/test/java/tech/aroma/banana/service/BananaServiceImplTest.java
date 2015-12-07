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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.service.ProvisionServiceRequest;
import tech.aroma.banana.thrift.service.ProvisionServiceResponse;
import tech.aroma.banana.thrift.service.SignInRequest;
import tech.aroma.banana.thrift.service.SignInResponse;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class BananaServiceImplTest 
{   
    
    @Mock
    private ThriftOperation<SignInRequest, SignInResponse> signIn;
    
    @Mock
    private ThriftOperation<ProvisionServiceRequest, ProvisionServiceResponse> provisionService;

    private BananaServiceImpl instance;
    
    @Before
    public void setUp()
    {
        
        instance = new BananaServiceImpl(signIn, provisionService);
        verifyZeroInteractions(signIn, provisionService);
    }

    @Test
    public void testSignIn() throws Exception
    {
        SignInRequest request = pojos(SignInRequest.class).get();
        SignInResponse expectedResponse = pojos(SignInResponse.class).get();
        when(signIn.process(request)).thenReturn(expectedResponse);
        
        SignInResponse response = instance.signIn(request);
        assertThat(response, is(expectedResponse));
        
        //Edge cases
        assertThrows(() -> instance.signIn(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testProvisionService() throws Exception
    {
        ProvisionServiceRequest request = one(pojos(ProvisionServiceRequest.class));
        ProvisionServiceResponse expectedResponse = one(pojos(ProvisionServiceResponse.class));
        when(provisionService.process(request)).thenReturn(expectedResponse);
        
        ProvisionServiceResponse response = instance.provisionService(request);
        assertThat(response, is(expectedResponse));
        
        //Edge cases
        assertThrows(() -> instance.provisionService(null))
            .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    public void testSubscribeToService() throws Exception
    {
    }

    @Test
    public void testRegisterHealthCheck() throws Exception
    {
    }

    @Test
    public void testRenewServiceToken() throws Exception
    {
    }

    @Test
    public void testRegenerateToken() throws Exception
    {
    }

    @Test
    public void testGetServiceInfo() throws Exception
    {
    }

    @Test
    public void testSearchForServices() throws Exception
    {
    }

    @Test
    public void testGetServiceSubscribers() throws Exception
    {
    }

    @Test
    public void testSendMessage() throws Exception
    {
    }

    @Test
    public void testSendMessageAsync() throws Exception
    {
    }

}