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
import org.gradle.api.problems.ProblemBuilderDefiningLabel;

import javax.annotation.Nullable;

import static java.lang.Boolean.TRUE;

@NonNullApi
public class DefaultTypeAwareProblemBuilder extends DelegatingProblemBuilder implements TypeAwareProblemBuilder {

    public static final String TYPE_NAME = "typeName";
    public static final String PLUGIN_ID = "pluginId";
    public static final String PARENT_PROPERTY_NAME = "parentPropertyName";
    public static final String PROPERTY_NAME = "propertyName";
    public static final String TYPE_IS_IRRELEVANT_IN_ERROR_MESSAGE = "typeIsIrrelevantInErrorMessage";

    public DefaultTypeAwareProblemBuilder(ProblemBuilderDefiningLabel problemBuilder) {
        super(problemBuilder);
    }

    @Override
    public TypeAwareProblemBuilder withAnnotationType(@Nullable Class<?> classWithAnnotationAttached) {
        if (classWithAnnotationAttached != null) {
            additionalData(TYPE_NAME, classWithAnnotationAttached.getName().replaceAll("\\$", "."));
        }
        return this;
    }

    @Override
    public TypeAwareProblemBuilder typeIsIrrelevantInErrorMessage() {
        additionalData(TYPE_IS_IRRELEVANT_IN_ERROR_MESSAGE, TRUE.toString());
        return this;
    }

    @Override
    public TypeAwareProblemBuilder forProperty(String propertyName) {
        additionalData(PROPERTY_NAME, propertyName);
        return this;
    }

    @Override
    public TypeAwareProblemBuilder parentProperty(@Nullable String parentProperty) {
        if (parentProperty == null) {
            return this;
        }
        String pp = getParentProperty(parentProperty);
        additionalData(PARENT_PROPERTY_NAME, pp);
        parentPropertyAdditionalData = pp;
        return this;
    }

    private String parentPropertyAdditionalData = null;

    private String getParentProperty(String parentProperty) {
        String existingParentProperty = parentPropertyAdditionalData;
        if (existingParentProperty == null) {
            return parentProperty;
        }
        return existingParentProperty + "." + parentProperty;
    }
}
