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

package org.gradle.api.problems;

import org.gradle.api.Incubating;

import javax.annotation.Nullable;

/**
 * {@link Problem} instance builder requiring the specification of the problem location.
 *
 * @since 8.4
 */
@Incubating
public interface ProblemBuilderDefiningLocation { // TODO discuss how to compose multiple explicit location information in problem builders
    /**
     * Declares that this problem is in a file with optional position and length.
     *
     * @param path the file location
     * @param line the line number
     * @param column the column number
     * @param length the length of the text
     * @return the builder for the next required property
     * @since 8.5
     */
    ProblemBuilderDefiningCategory fileLocation(String path, @Nullable Integer line, @Nullable Integer column, @Nullable Integer length);

    /**
     * Declares that this problem is emitted while applying a plugin.
     *
     * @param pluginId the ID of the applied plugin
     * @return the builder for the next required property
     * @since 8.5
     */
    ProblemBuilderDefiningCategory pluginLocation(String pluginId);

    /**
     * Declares that this problem should automatically collect the location information based on the current stack trace.
     *
     * @return the builder for the next required property
     * @since 8.5
     */
    ProblemBuilderDefiningCategory stackLocation();


    /**
     * Declares that this problem has no associated location data.
     *
     * @return the builder for the next required property
     * @since 8.4
     */
    ProblemBuilderDefiningCategory noLocation();
}
