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

package org.gradle.api.internal.artifacts;

import org.gradle.BuildAdapter;
import org.gradle.BuildResult;
import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.artifacts.ivyservice.ArtifactCachesProvider;
import org.gradle.api.internal.artifacts.ivyservice.DefaultArtifactCaches;
import org.gradle.api.internal.artifacts.transform.ImmutableTransformWorkspaceServices;
import org.gradle.api.internal.artifacts.transform.ToPlannedTransformStepConverter;
import org.gradle.api.internal.cache.CacheConfigurationsInternal;
import org.gradle.api.internal.cache.StringInterner;
import org.gradle.api.internal.changedetection.state.DefaultExecutionHistoryCacheAccess;
import org.gradle.cache.CacheBuilder;
import org.gradle.cache.UnscopedCacheBuilderFactory;
import org.gradle.cache.internal.CrossBuildInMemoryCacheFactory;
import org.gradle.cache.internal.InMemoryCacheDecoratorFactory;
import org.gradle.cache.internal.UsedGradleVersions;
import org.gradle.cache.scopes.GlobalScopedCacheBuilderFactory;
import org.gradle.execution.plan.ToPlannedNodeConverter;
import org.gradle.internal.Try;
import org.gradle.internal.event.ListenerManager;
import org.gradle.internal.execution.history.ExecutionHistoryCacheAccess;
import org.gradle.internal.execution.history.ExecutionHistoryStore;
import org.gradle.internal.execution.history.impl.DefaultExecutionHistoryStore;
import org.gradle.internal.file.FileAccessTimeJournal;
import org.gradle.internal.hash.ClassLoaderHierarchyHasher;

public class DependencyManagementGradleUserHomeScopeServices {

    ToPlannedNodeConverter createToPlannedTransformStepConverter() {
        return new ToPlannedTransformStepConverter();
    }

    DefaultArtifactCaches.WritableArtifactCacheLockingParameters createWritableArtifactCacheLockingParameters(FileAccessTimeJournal fileAccessTimeJournal, UsedGradleVersions usedGradleVersions) {
        return new DefaultArtifactCaches.WritableArtifactCacheLockingParameters() {
            @Override
            public FileAccessTimeJournal getFileAccessTimeJournal() {
                return fileAccessTimeJournal;
            }

            @Override
            public UsedGradleVersions getUsedGradleVersions() {
                return usedGradleVersions;
            }
        };
    }

    ArtifactCachesProvider createArtifactCaches(
        GlobalScopedCacheBuilderFactory cacheBuilderFactory,
        UnscopedCacheBuilderFactory unscopedCacheBuilderFactory,
        DefaultArtifactCaches.WritableArtifactCacheLockingParameters parameters,
        ListenerManager listenerManager,
        DocumentationRegistry documentationRegistry,
        CacheConfigurationsInternal cacheConfigurations
    ) {
        DefaultArtifactCaches artifactCachesProvider = new DefaultArtifactCaches(cacheBuilderFactory, unscopedCacheBuilderFactory, parameters, documentationRegistry, cacheConfigurations);
        listenerManager.addListener(new BuildAdapter() {
            @SuppressWarnings("deprecation")
            @Override
            public void buildFinished(BuildResult result) {
                artifactCachesProvider.getWritableCacheAccessCoordinator().useCache(() -> {
                    // forces cleanup even if cache wasn't used
                });
            }
        });
        return artifactCachesProvider;
    }

    ExecutionHistoryCacheAccess createExecutionHistoryCacheAccess(GlobalScopedCacheBuilderFactory cacheBuilderFactory) {
        return new DefaultExecutionHistoryCacheAccess(cacheBuilderFactory);
    }

    ExecutionHistoryStore createExecutionHistoryStore(
        ExecutionHistoryCacheAccess executionHistoryCacheAccess,
        InMemoryCacheDecoratorFactory inMemoryCacheDecoratorFactory,
        StringInterner stringInterner,
        ClassLoaderHierarchyHasher classLoaderHasher
    ) {
        return new DefaultExecutionHistoryStore(
            executionHistoryCacheAccess,
            inMemoryCacheDecoratorFactory,
            stringInterner,
            classLoaderHasher
        );
    }

    ImmutableTransformWorkspaceServices createTransformWorkspaceServices(
        ArtifactCachesProvider artifactCaches,
        UnscopedCacheBuilderFactory unscopedCacheBuilderFactory,
        CrossBuildInMemoryCacheFactory crossBuildInMemoryCacheFactory,
        FileAccessTimeJournal fileAccessTimeJournal,
        ExecutionHistoryStore executionHistoryStore,
        CacheConfigurationsInternal cacheConfigurations
    ) {
        return new ImmutableTransformWorkspaceServices(
            unscopedCacheBuilderFactory
                .cache(artifactCaches.getWritableCacheMetadata().getTransformsStoreDirectory())
                .withCrossVersionCache(CacheBuilder.LockTarget.DefaultTarget)
                .withDisplayName("Artifact transforms cache"),
            fileAccessTimeJournal,
            executionHistoryStore,
            crossBuildInMemoryCacheFactory.newCacheRetainingDataFromPreviousBuild(Try::isSuccessful),
            cacheConfigurations
        );
    }
}
