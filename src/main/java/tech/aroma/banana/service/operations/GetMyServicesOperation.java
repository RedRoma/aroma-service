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
import tech.aroma.banana.thrift.service.GetMyServicesRequest;
import tech.aroma.banana.thrift.service.GetMyServicesResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

/**
 *
 * @author SirWellington
 */
final class GetMyServicesOperation implements ThriftOperation<GetMyServicesRequest, GetMyServicesResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetMyServicesOperation.class);

    @Override
    public GetMyServicesResponse process(GetMyServicesRequest request) throws TException
    {
        LOG.debug("Received request to GetMyServices {}", request);
        
        return new GetMyServicesResponse();
    }

}
