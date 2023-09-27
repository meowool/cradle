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

package org.gradle.api.problems.internal;

import org.gradle.api.problems.Problems;
import org.gradle.internal.operations.NoOpBuildOperationProgressEventEmitter;

public class ProblemsProgressEventEmitterHolder {
    private static Problems problemsService = new DefaultProblems(new NoOpBuildOperationProgressEventEmitter());

    public static void init(Problems problemsService) {
        ProblemsProgressEventEmitterHolder.problemsService = problemsService;
    }

    public static Problems get() {
        if (problemsService == null) {
            throw new IllegalStateException("Problems service was null. At the same time, the event emitter is: " + new NoOpBuildOperationProgressEventEmitter());
        }
        return problemsService;
    }
}
