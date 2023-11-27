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

package org.gradle.problems.internal.emitters;

import org.gradle.api.Incubating;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.problems.Problem;
import org.gradle.api.problems.ProblemEmitter;
import org.gradle.api.problems.internal.DefaultProblem;
import org.gradle.api.problems.internal.DefaultProblemProgressDetails;
import org.gradle.internal.operations.BuildOperationProgressEventEmitter;

/**
 * Emits problems as build operation progress events.
 *
 * @since 8.6
 */
@Incubating
public class BuildOperationBasedProblemEmitter implements ProblemEmitter {

    private static final Logger LOGGER = Logging.getLogger(BuildOperationBasedProblemEmitter.class);

    private final BuildOperationProgressEventEmitter eventEmitter;

    public BuildOperationBasedProblemEmitter(BuildOperationProgressEventEmitter eventEmitter) {
        this.eventEmitter = eventEmitter;
    }

    @Override
    public void emit(Problem problem) {
        if (problem instanceof DefaultProblem) {
            DefaultProblem defaultProblem = (DefaultProblem) problem;

            if (defaultProblem.getBuildOperationId() != null) {
                eventEmitter.emitNow(
                    defaultProblem.getBuildOperationId(),
                    new DefaultProblemProgressDetails(problem)
                );
            }
            // else {
                // TODO (#27170): Turn this back on after deprecation reporting is fixed.
                // If the problem is not associated with a build operation, we cannot emit it as a build operation progress event.
                // LOGGER.error("Problem '{}' is not associated with a build operation, it will not be reported", problem.getLabel());
            // }
        }
    }
}
