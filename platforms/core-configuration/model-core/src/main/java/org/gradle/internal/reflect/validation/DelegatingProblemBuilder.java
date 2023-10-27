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

package org.gradle.internal.reflect.validation;

import org.gradle.api.NonNullApi;
import org.gradle.api.problems.BuildableProblemBuilder;
import org.gradle.api.problems.DocLink;
import org.gradle.api.problems.ProblemBuilder;
import org.gradle.api.problems.ProblemBuilderDefiningCategory;
import org.gradle.api.problems.ProblemBuilderDefiningDocumentation;
import org.gradle.api.problems.ProblemBuilderDefiningLabel;
import org.gradle.api.problems.ProblemBuilderDefiningLocation;
import org.gradle.api.problems.ReportableProblem;
import org.gradle.api.problems.Severity;

import javax.annotation.Nullable;

@NonNullApi
class DelegatingProblemBuilder implements
    ProblemBuilderDefiningLabel,
    ProblemBuilderDefiningDocumentation,
    ProblemBuilderDefiningLocation,
    ProblemBuilderDefiningCategory,
    BuildableProblemBuilder {

    private final Object delegate;

    DelegatingProblemBuilder(ProblemBuilderDefiningLabel delegate) {
        this.delegate = delegate;
    }

    @Override
    public ProblemBuilderDefiningDocumentation label(String label, Object... args) {
        ProblemBuilderDefiningDocumentation newDelegate = ((ProblemBuilderDefiningLabel) delegate).label(label, args);
        return validateDelegate(newDelegate);
    }

    @Override
    public ProblemBuilderDefiningLocation documentedAt(DocLink doc) {
        ProblemBuilderDefiningLocation newDelegate = ((ProblemBuilderDefiningDocumentation) delegate).documentedAt(doc);
        return validateDelegate(newDelegate);
    }

    @Override
    public ProblemBuilderDefiningLocation undocumented() {
        ProblemBuilderDefiningLocation newDelegate = ((ProblemBuilderDefiningDocumentation) delegate).undocumented();
        return validateDelegate(newDelegate);
    }

    @Override
    public ProblemBuilderDefiningCategory location(String path, Integer line) {
        ProblemBuilderDefiningCategory newDelegate = ((ProblemBuilderDefiningLocation) delegate).location(path, line);
        return validateDelegate(newDelegate);
    }

    @Override
    public ProblemBuilderDefiningCategory location(String path, Integer line, Integer column) {
        ProblemBuilderDefiningCategory newDelegate = ((ProblemBuilderDefiningLocation) delegate).location(path, line, column);
        return validateDelegate(newDelegate);
    }

    @Override
    public ProblemBuilderDefiningCategory pluginLocation(String pluginId) {
        ProblemBuilderDefiningCategory newDelegate = ((ProblemBuilderDefiningLocation) delegate).pluginLocation(pluginId);
        return validateDelegate(newDelegate);
    }

    private <T> T validateDelegate(T newDelegate) {
        if (delegate != newDelegate) {
            throw new IllegalStateException("Builder pattern expected to return 'this'");
        }
        return newDelegate;
    }

    @Override
    public ProblemBuilderDefiningCategory collectLocation() {
        ProblemBuilderDefiningCategory newDelegate = ((ProblemBuilderDefiningLocation) delegate).collectLocation();
        return validateDelegate(newDelegate);
    }

    @Override
    public ProblemBuilderDefiningCategory noLocation() {
        ProblemBuilderDefiningCategory newDelegate = ((ProblemBuilderDefiningLocation) delegate).noLocation();
        return validateDelegate(newDelegate);
    }

    @Override
    public ProblemBuilder category(String category, String... details){
        ProblemBuilder newDelegate = ((ProblemBuilderDefiningCategory) delegate).category(category, details);
        return validateDelegate(newDelegate);
    }

    @Override
    public BuildableProblemBuilder details(String details) {
        ProblemBuilder newDelegate = ((BuildableProblemBuilder) delegate).details(details);
        return (BuildableProblemBuilder) validateDelegate(newDelegate);
    }

    @Override
    public BuildableProblemBuilder solution(@Nullable String solution) {
        ProblemBuilder newDelegate = ((BuildableProblemBuilder) delegate).solution(solution);
        return (BuildableProblemBuilder) validateDelegate(newDelegate);
    }

    @Override
    public BuildableProblemBuilder additionalData(String key, String value) {
        ProblemBuilder newDelegate = ((BuildableProblemBuilder) delegate).additionalData(key, value);
        return (BuildableProblemBuilder) validateDelegate(newDelegate);
    }

    @Override
    public BuildableProblemBuilder withException(RuntimeException e) {
        ProblemBuilder newDelegate = ((BuildableProblemBuilder) delegate).withException(e);
        return (BuildableProblemBuilder) validateDelegate(newDelegate);
    }

    @Override
    public BuildableProblemBuilder severity(@Nullable Severity severity) {
        ProblemBuilder newDelegate = ((BuildableProblemBuilder) delegate).severity(severity);
        return (BuildableProblemBuilder) validateDelegate(newDelegate);
    }

    @Override
    public ReportableProblem build() {
        return ((BuildableProblemBuilder) delegate).build();
    }
}
