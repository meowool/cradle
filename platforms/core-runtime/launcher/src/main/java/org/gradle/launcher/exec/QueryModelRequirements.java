/*
 * Copyright 2021 the original author or authors.
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

package org.gradle.launcher.exec;

import org.gradle.api.internal.StartParameterInternal;
import org.gradle.internal.hash.Hasher;

public class QueryModelRequirements extends AbstractToolingModelRequirements {
    private final String modelName;

    public QueryModelRequirements(StartParameterInternal startParameter,
                                  boolean runsTasks,
                                  String modelName) {
        super(startParameter, runsTasks);
        this.modelName = modelName;
    }

    @Override
    public void appendKeyTo(Hasher hasher) {
        // Identify the type of action
        hasher.putByte((byte) 2);
        hasher.putString(modelName);
    }
}
