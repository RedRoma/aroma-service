/*
 * Copyright 2016 RedRoma.
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

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.service.RenewApplicationTokenRequest;
import tech.aroma.thrift.service.RenewApplicationTokenResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.service.AromaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;

/**
 *
 * @author SirWellington
 */
final class RenewApplicationTokenOperation implements ThriftOperation<RenewApplicationTokenRequest, RenewApplicationTokenResponse>
{

    private final static Logger LOG = LoggerFactory.getLogger(RenewApplicationTokenOperation.class);

    @Override
    public RenewApplicationTokenResponse process(RenewApplicationTokenRequest request) throws TException
    {
        checkNotNull(request);
        //Check user is authorized for this action
        //Check that we have been paid
        //Delete the old token
        //Create a new one
        //Issue and return the new token

        return pojos(RenewApplicationTokenResponse.class).get();
    }

}
