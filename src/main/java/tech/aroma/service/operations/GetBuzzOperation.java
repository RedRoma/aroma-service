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

 
package tech.aroma.service.operations;


import java.util.List;
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.data.ApplicationRepository;
import tech.aroma.data.OrganizationRepository;
import tech.aroma.data.UserRepository;
import tech.aroma.service.AromaGenerators;
import tech.aroma.thrift.Application;
import tech.aroma.thrift.events.GeneralEvent;
import tech.aroma.thrift.events.HealthCheckFailed;
import tech.aroma.thrift.service.GetBuzzRequest;
import tech.aroma.thrift.service.GetBuzzResponse;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.service.AromaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;

/**
 *
 * @author SirWellington
 */
final class GetBuzzOperation implements ThriftOperation<GetBuzzRequest, GetBuzzResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(GetBuzzOperation.class);
    
    private final ApplicationRepository appRepo;
    private final OrganizationRepository orgRepo;
    private final UserRepository userRepo;

    @Inject
    GetBuzzOperation(ApplicationRepository appRepo, OrganizationRepository orgRepo, UserRepository userRepo)
    {
        checkThat(appRepo, orgRepo, userRepo)
            .are(notNull());
        
        this.appRepo = appRepo;
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
    }
    
    @Override
    public GetBuzzResponse process(GetBuzzRequest request) throws TException
    {
        checkNotNull(request);
        
        List<Application> recentApps = appRepo.getRecentlyCreated();
        //Get recently created apps
        //Get the requester's orgs
        //Get recent users in those orgs
        //Get recent apps in those orgs
        //Return all that
        
        
        GetBuzzResponse response = one(buzz());
        response.setFreshApplications(recentApps);
        
        LOG.debug("Returning Buzz: {}", response);
        
        return response;
    }

    
    private AlchemyGenerator<GetBuzzResponse> buzz()
    {
        return () ->
        {
          
            GetBuzzResponse response = new GetBuzzResponse();
            
            AlchemyGenerator<HealthCheckFailed> healthChecks = pojos(HealthCheckFailed.class);
            AlchemyGenerator<GeneralEvent> generalEvents = pojos(GeneralEvent.class);
            
            int numberOfUsers = one(integers(5, 22));
            int numberOfFailedHealthChecks = one(integers(0, 6));
            int numberOfGeneralHappenings = one(integers(5, 25));
            
            response.setFreshUsers(listOf(AromaGenerators.users(), numberOfUsers))
                .setFailedHealthChecks(listOf(healthChecks, numberOfFailedHealthChecks))
                .setGeneralEvents(listOf(generalEvents, numberOfGeneralHappenings));
            
            return response;
            
        };
    
    }
    
}
