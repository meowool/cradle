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

import org.gradle.api.problems.Problem;
import org.gradle.api.problems.ProblemBuilder;
import org.gradle.api.problems.ProblemBuilderSpec;
import org.gradle.api.problems.ProblemGroup;
import org.gradle.api.problems.ReportableProblem;
import org.gradle.internal.operations.BuildOperationProgressEventEmitter;
import org.gradle.internal.service.scopes.Scope;
import org.gradle.internal.service.scopes.ServiceScope;

import java.util.LinkedHashMap;
import java.util.Map;

@ServiceScope(Scope.Global.class)
public class DefaultProblems implements InternalProblems {
    private final BuildOperationProgressEventEmitter buildOperationProgressEventEmitter;

    private final Map<String, ProblemGroup> problemGroups = new LinkedHashMap<>();

    public DefaultProblems(BuildOperationProgressEventEmitter buildOperationProgressEventEmitter) {
        this.buildOperationProgressEventEmitter = buildOperationProgressEventEmitter;
        addPredefinedGroup(ProblemGroup.GENERIC_ID);
        addPredefinedGroup(ProblemGroup.TYPE_VALIDATION_ID);
        addPredefinedGroup(ProblemGroup.DEPRECATION_ID);
        addPredefinedGroup(ProblemGroup.VERSION_CATALOG_ID);
    }

    private void addPredefinedGroup(String genericId) {
        problemGroups.put(genericId, new ProblemGroup(genericId));
    }

    @Override
    public DefaultBuildableProblemBuilder createProblemBuilder() {
        return new DefaultBuildableProblemBuilder(this);
    }

    @Override
    public RuntimeException throwing(ProblemBuilderSpec action) {
        DefaultBuildableProblemBuilder defaultProblemBuilder = createProblemBuilder();
        action.apply(defaultProblemBuilder);
        throw throwError(defaultProblemBuilder.getException(), defaultProblemBuilder.build());
    }

    @Override
    public RuntimeException rethrowing(RuntimeException e, ProblemBuilderSpec action) {
        DefaultBuildableProblemBuilder defaultProblemBuilder = createProblemBuilder();
        ProblemBuilder problemBuilder = action.apply(defaultProblemBuilder);
        problemBuilder.withException(e);
        throw throwError(e, defaultProblemBuilder.build());
    }

    @Override
    public ReportableProblem createProblem(ProblemBuilderSpec action) {
        DefaultBuildableProblemBuilder defaultProblemBuilder = createProblemBuilder();
        action.apply(defaultProblemBuilder);
        return defaultProblemBuilder.build();
    }

    public RuntimeException throwError(RuntimeException exception, Problem problem) {
        reportAsProgressEvent(problem);
        throw exception;
    }

    @Override
    public void reportAsProgressEvent(Problem problem) {
        buildOperationProgressEventEmitter.emitNowIfCurrent(new DefaultProblemProgressDetails(problem));
    }

    @Override
    public ProblemGroup getProblemGroup(String groupId) {
        return problemGroups.get(groupId);
    }
}
