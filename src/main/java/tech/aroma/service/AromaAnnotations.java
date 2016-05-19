/*
 * Copyright 2016 RedRoma, Inc.
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

 
package tech.aroma.service;


import com.google.inject.BindingAnnotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;
import tech.aroma.thrift.User;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Special annotations that are used to mark special Guice dependencies
 * are defined here.
 * <p>
 * These Binding annotations allow the system to qualify specific dependencies.
 * For example, 
 * 
 * <pre>
 * {@code 
 * 
 *  SomeObject(@SuperUsers List<String> powerUsers, @BlackListedUsers List<String> blacklisted)
 }
 * </pre>
 * 
 * @author SirWellington
 */
@Internal
@NonInstantiable
public interface AromaAnnotations 
{
    /**
     * Defines a set of users that have been black-listed within Aroma.
     * These are typically defined as a {@linkplain Set set} of {@linkplain User#userId User IDs}.
     * 
     * <p>
     * Blacklisted users typically cannot perform any Update/Delete operations.
     * 
     */
    @BindingAnnotation
    @Target({ PARAMETER, FIELD, METHOD })
    @Retention(RUNTIME)
    public @interface BlacklistedUsers
    {
        
    }
    
    /**
     * Defines a set of users that have Super-Power abilities within Aroma.
     * These are typically defined as a {@linkplain Set set} of {@linkplain User#userId User IDs}.
     */
    @BindingAnnotation
    @Target({ PARAMETER, FIELD, METHOD })
    @Retention(RUNTIME)
    public @interface SuperUsers
    {
        
    }
}
