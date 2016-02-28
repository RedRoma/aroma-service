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

package tech.aroma.service.operations;

import java.util.List;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.exceptions.InvalidArgumentException;
import tech.aroma.thrift.service.GetApplicationsOwnedByRequest;
import tech.aroma.thrift.service.GetApplicationsOwnedByResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static tech.aroma.data.assertions.RequestAssertions.validUserId;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
final class GetApplicationsOwnedByOperation implements ThriftOperation<GetApplicationsOwnedByRequest, GetApplicationsOwnedByResponse>
{
    
    private final static Logger LOG = LoggerFactory.getLogger(GetApplicationsOwnedByOperation.class);
    
    private final ApplicationRepository appRepo;
    
    @Inject
    GetApplicationsOwnedByOperation(ApplicationRepository appRepo)
    {
        checkThat(appRepo)
            .is(notNull());
        
        this.appRepo = appRepo;
    }
    
    @Override
    public GetApplicationsOwnedByResponse process(GetApplicationsOwnedByRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        LOG.debug("Received request to GetApplicationsOwnedBy {}", request);
        
        String userId = request.token.userId;
        List<Application> apps = appRepo.getApplicationsOwnedBy(userId)
            .parallelStream()
            .sorted(comparing(Application::getName))
            .collect(toList());
        
        LOG.debug("Found {} applications owned by {}", apps.size(), userId);
        
        return new GetApplicationsOwnedByResponse(apps);
    }
    
    private AlchemyAssertion<GetApplicationsOwnedByRequest> good()
    {
        return request ->
        {
            checkThat(request)
                .usingMessage("missing request")
                .is(notNull());
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
            
            checkThat(request.token.userId)
                .usingMessage("token missing userId")
                .is(nonEmptyString());
            
            checkThat(request.token.userId)
                .is(validUserId());
        };
    }
    
}
