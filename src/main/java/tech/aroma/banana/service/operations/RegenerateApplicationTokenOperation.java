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

package tech.aroma.banana.service.operations;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.TokenRepository;
import tech.aroma.banana.data.UserRepository;
import tech.aroma.banana.thrift.authentication.service.AuthenticationService;
import tech.aroma.banana.thrift.service.RegenerateApplicationTokenRequest;
import tech.aroma.banana.thrift.service.RegenerateApplicationTokenResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.service.BananaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;

/**
 *
 * @author SirWellington
 */
final class RegenerateApplicationTokenOperation implements ThriftOperation<RegenerateApplicationTokenRequest, RegenerateApplicationTokenResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(RegenerateApplicationTokenOperation.class);
    private UserRepository userRepo;
    private AuthenticationService.Iface authenticationService;
    private ApplicationRepository appRepo;
    private TokenRepository tokenRepo;

    @Override
    public RegenerateApplicationTokenResponse process(RegenerateApplicationTokenRequest request) throws TException
    {
        checkNotNull(request);
        
        //Get User ID
        //Get App Info
        //Assert user has authorization to do this
        //Delete existing tokens for App
        //Create new token
        //Return token

        return pojos(RegenerateApplicationTokenResponse.class).get();
    }

}
