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


import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.thrift.service.SaveChannelRequest;
import tech.aroma.thrift.service.SaveChannelResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.service.AromaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;

/**
 *
 * @author SirWellington
 */
final class SaveChannelOperation implements ThriftOperation<SaveChannelRequest, SaveChannelResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(SaveChannelOperation.class);

    @Override
    public SaveChannelResponse process(SaveChannelRequest request) throws TException
    {
        checkNotNull(request);

        return pojos(SaveChannelResponse.class).get();
    }

}
