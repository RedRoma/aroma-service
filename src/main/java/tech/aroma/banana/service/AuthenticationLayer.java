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

 
package tech.aroma.banana.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.banana.thrift.service.BananaService;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.DecoratorPattern.Role.CONCRETE_DECORATOR;

/**
 * This Layer decorates an existing {@link BananaService.Iface} and authenticates
 * calls again an {@linkplain AuthenticationService.Iface Authentication Service}.
 * @author SirWellington
 */
@Internal
@DecoratorPattern(role = CONCRETE_DECORATOR)
final class AuthenticationLayer 
{
    private final static Logger LOG = LoggerFactory.getLogger(AuthenticationLayer.class);

}
