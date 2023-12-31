/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.internal.enterprise.impl.legacy;

import org.gradle.internal.scan.scopeids.BuildScanScopeIds;
import org.gradle.internal.scopeids.id.BuildInvocationScopeId;
import org.gradle.internal.scopeids.id.UserScopeId;
import org.gradle.internal.scopeids.id.WorkspaceScopeId;
import org.gradle.internal.service.scopes.Scopes;
import org.gradle.internal.service.scopes.ServiceScope;

@ServiceScope(Scopes.Gradle.class)
public class DefaultBuildScanScopeIds implements BuildScanScopeIds {

    private final BuildInvocationScopeId buildInvocationId;
    private final WorkspaceScopeId workspaceId;
    private final UserScopeId userId;

    public DefaultBuildScanScopeIds(BuildInvocationScopeId buildInvocationId, WorkspaceScopeId workspaceId, UserScopeId userId) {
        this.buildInvocationId = buildInvocationId;
        this.workspaceId = workspaceId;
        this.userId = userId;
    }

    @Override
    public String getBuildInvocationId() {
        return buildInvocationId.getId().asString();
    }

    @Override
    public String getWorkspaceId() {
        return workspaceId.getId().asString();
    }

    @Override
    public String getUserId() {
        return userId.getId().asString();
    }
}
