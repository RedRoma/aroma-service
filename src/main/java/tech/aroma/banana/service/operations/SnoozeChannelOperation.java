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
import tech.aroma.thrift.service.SnoozeChannelRequest;
import tech.aroma.thrift.service.SnoozeChannelResponse;
import tech.sirwellington.alchemy.thrift.operations.ThriftOperation;

import static tech.aroma.banana.service.AromaAssertions.checkNotNull;
import static tech.sirwellington.alchemy.generator.ObjectGenerators.pojos;

/**
 *
 * @author SirWellington
 */
final class SnoozeChannelOperation implements ThriftOperation<SnoozeChannelRequest, SnoozeChannelResponse>
{
    private final static Logger LOG = LoggerFactory.getLogger(SnoozeChannelOperation.class);

    @Override
    public SnoozeChannelResponse process(SnoozeChannelRequest request) throws TException
    {
        checkNotNull(request);

        return pojos(SnoozeChannelResponse.class).get();
    }

}
