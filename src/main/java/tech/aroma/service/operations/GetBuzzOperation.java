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
import tech.aroma.thrift.Application;
import tech.aroma.thrift.User;
import tech.aroma.thrift.service.GetBuzzRequest;
import tech.aroma.thrift.service.GetBuzzResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.service.AromaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

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
        List<User> recentUsers = userRepo.getRecentlyCreatedUsers();
        
        //Get recently created apps
        //Get the requester's orgs
        //Get recent users in those orgs
        //Get recent apps in those orgs
        //Return all that
        GetBuzzResponse response = new GetBuzzResponse()
            .setFreshApplications(recentApps)
            .setFreshUsers(recentUsers);

        LOG.debug("Returning Buzz: {}", response);
        
        return response;
    }
    
}
