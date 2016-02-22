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
import javax.inject.Inject;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.data.ApplicationRepository;
import tech.aroma.banana.data.OrganizationRepository;
import tech.aroma.banana.thrift.Application;
import tech.aroma.banana.thrift.exceptions.InvalidArgumentException;
import tech.aroma.banana.thrift.exceptions.OrganizationDoesNotExistException;
import tech.aroma.banana.thrift.exceptions.UnauthorizedException;
import tech.aroma.banana.thrift.service.SearchForApplicationsRequest;
import tech.aroma.banana.thrift.service.SearchForApplicationsResponse;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthGreaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;

/**
 *
 * @author SirWellington
 */
final class SearchForApplicationsOperation implements ThriftOperation<SearchForApplicationsRequest, SearchForApplicationsResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(SearchForApplicationsOperation.class);

    private final ApplicationRepository appRepo;
    private final OrganizationRepository orgRepo;

    @Inject
    SearchForApplicationsOperation(ApplicationRepository appRepo, OrganizationRepository orgRepo)
    {
        checkThat(appRepo, orgRepo)
            .are(notNull());
        
        this.appRepo = appRepo;
        this.orgRepo = orgRepo;
    }
    
    @Override
    public SearchForApplicationsResponse process(SearchForApplicationsRequest request) throws TException
    {
        checkThat(request)
            .throwing(ex -> new InvalidArgumentException(ex.getMessage()))
            .is(good());
        
        String searchTerm = request.getApplicationName();
        String userId = request.token.userId;
        
        if(request.isSetOrganizationId())
        {
            String orgId = request.organizationId;
            checkThat(orgId)
                .throwing(OrganizationDoesNotExistException.class)
                .is(real());
            
            checkThat(userId)
                .throwing(UnauthorizedException.class)
                .is(memberInOrg(orgId));
            
            List<Application> apps = searchForApplicationInOrgThatMatch(orgId, searchTerm);
            
            return new SearchForApplicationsResponse(apps);
        }
        
        return pojos(SearchForApplicationsResponse.class).get();
    }

    private AlchemyAssertion<SearchForApplicationsRequest> good()
    {
        return request ->
        {
            checkThat(request).is(notNull());
            
            checkThat(request.applicationName)
                .usingMessage("Search term is empty")
                .is(nonEmptyString())
                .usingMessage("Search term must have at least 2 characters")
                .is(stringWithLengthGreaterThan(2));
            
            checkThat(request.token)
                .usingMessage("request missing token")
                .is(notNull());
            
            checkThat(request.token.userId)
                .usingMessage("token missing userId")
                .is(nonEmptyString())
                .usingMessage("userId must be a UUID")
                .is(validUUID());
            
            if(request.isSetOrganizationId())
            {
                checkThat(request.organizationId)
                    .usingMessage("orgId must be a UUID")
                    .is(validUUID());
            }
        };
        
    }

    private AlchemyAssertion<String> memberInOrg(String orgId) throws TException
    {
        return userId ->
        {
            boolean isMember = false;
            
            try
            {
                isMember = orgRepo.isMemberInOrganization(orgId, userId);
            }
            catch(TException ex)
            {
                throw new FailedAssertionException("Could not check for membership of user in org: " + orgId, ex);
            }
            
            if(!isMember)
            {
                throw new FailedAssertionException(format("User %s is not a member in Org %s", userId, orgId));
            }
        };
    }


    private List<Application> searchForApplicationInOrgThatMatch(String orgId, String searchTerm) throws TException
    {
        return appRepo.getApplicationsByOrg(orgId)
            .parallelStream()
            .filter(app -> app.name.contains(searchTerm))
            .collect(toList());
    }

    private AlchemyAssertion<String> real()
    {
        return orgId -> 
        {
            boolean exists = false;
            try
            {
                exists = orgRepo.containsOrganization(orgId);
            }
            catch (TException ex)
            {
                throw new FailedAssertionException(format("Could not check if org exists: %s ", orgId), ex);
            }
            
            if(!exists)
            {
                throw new FailedAssertionException("Organization does not exist: " + orgId);
            }
        };         
    }


}
