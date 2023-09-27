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

/**
 * {@link Problem} instance builder requiring the specification of the problem location.
 *
 * @since 8.4
 */
@Incubating
public interface ProblemBuilderDefiningLocation {
    /**
     * Declares that this problem is in a file at a particular line.
     *
     * @param path the file location
     * @param line the line number
     * @return the builder for the next required property
     */
    ProblemBuilderDefiningType location(String path, Integer line);

    /**
     * Declares that this problem is in a file at a particular line.
     *
     * @param path the file location
     * @param line the line number
     * @param column the column number
     * @return the builder for the next required property
     */
    ProblemBuilderDefiningType location(String path, Integer line, Integer column);

    /**
     * Declares that this problem has no associated location data.
     *
     * @return the builder for the next required property
     */
    ProblemBuilderDefiningType noLocation();
}
