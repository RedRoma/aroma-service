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

 
package tech.aroma.banana.service.operations;


import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.service.GetUserInfoRequest;
import tech.aroma.banana.thrift.service.GetUserInfoResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.service.BananaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;

/**
 *
 * @author SirWellington
 */
final class GetUserInfoOperation implements ThriftOperation<GetUserInfoRequest, GetUserInfoResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetUserInfoOperation.class);

    @Override
    public GetUserInfoResponse process(GetUserInfoRequest request) throws TException
    {
        checkNotNull(request);
        
        GetUserInfoResponse response = pojos(GetUserInfoResponse.class).get();
        return response;
    }

}
