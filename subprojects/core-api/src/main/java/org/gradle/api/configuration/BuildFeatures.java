/*
 * Copyright 2023 the original author or authors.
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

package org.gradle.api.configuration;

import org.gradle.api.Incubating;
import org.gradle.internal.service.scopes.Scopes;
import org.gradle.internal.service.scopes.ServiceScope;

/**
 * Provides information about various build features supported by Gradle,
 * and their state in the current build.
 *
 * @see BuildFeature
 * @since 8.5
 */
@Incubating
@ServiceScope(Scopes.BuildTree.class)
public interface BuildFeatures {

    /**
     * State of the <a href="https://docs.gradle.org/current/userguide/configuration_cache.html">Configuration Cache</a> feature in the build.
     *
     * @since 8.5
     */
    BuildFeature getConfigurationCache();

    /**
     * State of the Isolated Projects feature in the build.
     *
     * @since 8.5
     */
    BuildFeature getIsolatedProjects();

}
