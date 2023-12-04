/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.internal.reflect;

import org.gradle.api.Action;
import org.gradle.api.problems.ReportableProblem;
import org.gradle.api.problems.internal.InternalProblems;
import org.gradle.api.problems.internal.ProblemsProgressEventEmitterHolder;
import org.gradle.internal.reflect.validation.DefaultTypeAwareProblemBuilder;
import org.gradle.internal.reflect.validation.TypeAwareProblemBuilder;
import org.gradle.internal.reflect.validation.TypeValidationContext;
import org.gradle.plugin.use.PluginId;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

import static org.gradle.internal.reflect.validation.DefaultTypeAwareProblemBuilder.PLUGIN_ID;

abstract public class ProblemRecordingTypeValidationContext implements TypeValidationContext {
    private final Class<?> rootType;
    private final Supplier<Optional<PluginId>> pluginId;

    public ProblemRecordingTypeValidationContext(
        @Nullable Class<?> rootType,
        Supplier<Optional<PluginId>> pluginId
    ) {
        this.rootType = rootType;
        this.pluginId = pluginId;
    }

    @Override
    public void visitTypeProblem(Action<? super TypeAwareProblemBuilder> problemSpec) {
        InternalProblems problems = (InternalProblems) ProblemsProgressEventEmitterHolder.get();
        DefaultTypeAwareProblemBuilder problemBuilder = new DefaultTypeAwareProblemBuilder(problems.createProblemBuilder());
        problemSpec.execute(problemBuilder);
        recordProblem(problemBuilder.build());
    }

    private Optional<PluginId> pluginId() {
        return pluginId.get();
    }


    @Override
    public void visitPropertyProblem(Action<? super TypeAwareProblemBuilder> problemSpec) {
        InternalProblems problems = (InternalProblems) ProblemsProgressEventEmitterHolder.get();
        DefaultTypeAwareProblemBuilder problemBuilder = new DefaultTypeAwareProblemBuilder(problems.createProblemBuilder());
        problemSpec.execute(problemBuilder);
        problemBuilder.withAnnotationType(rootType);
        pluginId()
            .map(PluginId::getId)
            .ifPresent(id -> problemBuilder.additionalData(PLUGIN_ID, id));
        recordProblem(problemBuilder.build());
    }

    abstract protected void recordProblem(ReportableProblem problem);
}
