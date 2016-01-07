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

import java.util.List;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.service.GetMyApplicationsRequest;
import tech.aroma.banana.thrift.service.GetMyApplicationsResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.service.BananaAssertions.checkNotNull;
import static tech.aroma.banana.service.BananaGenerators.applications;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;

/**
 *
 * @author SirWellington
 */
final class GetMyApplicationsOperation implements ThriftOperation<GetMyApplicationsRequest, GetMyApplicationsResponse>
{
    
    private final static Logger LOG = LoggerFactory.getLogger(GetMyApplicationsOperation.class);
    
    @Override
    public GetMyApplicationsResponse process(GetMyApplicationsRequest request) throws TException
    {
        checkNotNull(request);
        
        LOG.debug("Received request to GetMyApplications {}", request);
        
        GetMyApplicationsResponse response = new GetMyApplicationsResponse();
        
        int count = one(integers(0, 40));
        List<Application> fakeApplications = listOf(applications(), count);
        response.setApplications(fakeApplications);
        
        LOG.info("Returning {} Applications for {}", fakeApplications.size(), request);
        
        return response;
        
    }
    
    
    
}
