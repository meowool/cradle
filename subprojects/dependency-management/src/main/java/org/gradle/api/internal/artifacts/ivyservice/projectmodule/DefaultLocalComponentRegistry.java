/*
 * Copyright 2016 the original author or authors.
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

package org.gradle.api.internal.artifacts.ivyservice.projectmodule;

import org.gradle.api.artifacts.component.BuildIdentifier;
import org.gradle.api.artifacts.component.ProjectComponentIdentifier;
import org.gradle.internal.component.local.model.LocalComponentGraphResolveState;

/**
 * Default implementation of {@link LocalComponentRegistry}. This is a simple build-scoped wrapper
 * around {@link BuildTreeLocalComponentProvider} that contextualizes it to the current build, so that
 * users of this class do not need to keep track of their own build ID.
 *
 * When {@link BuildIdentifier#isCurrentBuild()} is removed, this class can be made build-tree scoped.
 */
public class DefaultLocalComponentRegistry implements LocalComponentRegistry {
    private final BuildIdentifier thisBuild;
    private final BuildTreeLocalComponentProvider componentProvider;

    public DefaultLocalComponentRegistry(
        BuildIdentifier thisBuild,
        BuildTreeLocalComponentProvider componentProvider
    ) {
        this.thisBuild = thisBuild;
        this.componentProvider = componentProvider;
    }

    @Override
    public LocalComponentGraphResolveState getComponent(ProjectComponentIdentifier projectIdentifier) {
        return componentProvider.getComponent(projectIdentifier, thisBuild);
    }
}
