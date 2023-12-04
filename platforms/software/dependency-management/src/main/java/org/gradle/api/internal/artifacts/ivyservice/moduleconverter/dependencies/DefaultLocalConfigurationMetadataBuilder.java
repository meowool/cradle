/*
 * Copyright 2007-2009 the original author or authors.
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
package org.gradle.api.internal.artifacts.ivyservice.moduleconverter.dependencies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyConstraint;
import org.gradle.api.artifacts.ExcludeRule;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.attributes.Category;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;
import org.gradle.api.internal.artifacts.configurations.Configurations;
import org.gradle.api.internal.artifacts.configurations.ConfigurationsProvider;
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal;
import org.gradle.api.internal.attributes.AttributeValue;
import org.gradle.api.internal.attributes.ImmutableAttributes;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.internal.Describables;
import org.gradle.internal.DisplayName;
import org.gradle.internal.component.external.model.ImmutableCapabilities;
import org.gradle.internal.component.local.model.DefaultLocalConfigurationMetadata;
import org.gradle.internal.component.local.model.LocalComponentArtifactMetadata;
import org.gradle.internal.component.local.model.LocalComponentMetadata;
import org.gradle.internal.component.local.model.LocalConfigurationMetadata;
import org.gradle.internal.component.local.model.LocalFileDependencyMetadata;
import org.gradle.internal.component.local.model.LocalVariantMetadata;
import org.gradle.internal.component.local.model.PublishArtifactLocalArtifactMetadata;
import org.gradle.internal.component.model.ComponentConfigurationIdentifier;
import org.gradle.internal.component.model.ExcludeMetadata;
import org.gradle.internal.component.model.LocalOriginDependencyMetadata;
import org.gradle.internal.component.model.VariantResolveMetadata;
import org.gradle.internal.model.CalculatedValue;
import org.gradle.internal.model.CalculatedValueContainerFactory;
import org.gradle.internal.model.ModelContainer;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/**
 * Encapsulates all logic required to build a {@link LocalConfigurationMetadata} from a
 * {@link ConfigurationInternal}. Utilizes caching to prevent unnecessary duplicate conversions
 * between DSL and internal metadata types.
 */
public class DefaultLocalConfigurationMetadataBuilder implements LocalConfigurationMetadataBuilder {
    private final DependencyMetadataFactory dependencyMetadataFactory;
    private final ExcludeRuleConverter excludeRuleConverter;

    public DefaultLocalConfigurationMetadataBuilder(
        DependencyMetadataFactory dependencyMetadataFactory,
        ExcludeRuleConverter excludeRuleConverter
    ) {
        this.dependencyMetadataFactory = dependencyMetadataFactory;
        this.excludeRuleConverter = excludeRuleConverter;
    }

    @Override
    public LocalConfigurationMetadata create(
        ConfigurationInternal configuration,
        ConfigurationsProvider configurationsProvider,
        LocalComponentMetadata parent,
        DependencyCache dependencyCache,
        ModelContainer<?> model,
        CalculatedValueContainerFactory calculatedValueContainerFactory
    ) {
        String configurationName = configuration.getName();
        String description = configuration.getDescription();
        ComponentIdentifier componentId = parent.getId();
        ComponentConfigurationIdentifier configurationIdentifier = new ComponentConfigurationIdentifier(componentId, configuration.getName());

        // Collect all artifacts and sub-variants.
        ImmutableList.Builder<PublishArtifact> artifactBuilder = ImmutableList.builder();
        ImmutableSet.Builder<LocalVariantMetadata> variantsBuilder = ImmutableSet.builder();
        configuration.collectVariants(new ConfigurationInternal.VariantVisitor() {
            @Override
            public void visitArtifacts(Collection<? extends PublishArtifact> artifacts) {
                artifactBuilder.addAll(artifacts);
            }

            @Override
            public void visitOwnVariant(DisplayName displayName, ImmutableAttributes attributes, Collection<? extends Capability> capabilities, Collection<? extends PublishArtifact> artifacts) {
                CalculatedValue<ImmutableList<LocalComponentArtifactMetadata>> variantArtifacts = getVariantArtifacts(componentId, displayName, artifacts, model, calculatedValueContainerFactory);
                variantsBuilder.add(new LocalVariantMetadata(configurationName, configurationIdentifier, displayName, attributes, ImmutableCapabilities.of(capabilities), variantArtifacts));
            }

            @Override
            public void visitChildVariant(String name, DisplayName displayName, ImmutableAttributes attributes, Collection<? extends Capability> capabilities, Collection<? extends PublishArtifact> artifacts) {
                CalculatedValue<ImmutableList<LocalComponentArtifactMetadata>> variantArtifacts = getVariantArtifacts(componentId, displayName, artifacts, model, calculatedValueContainerFactory);
                variantsBuilder.add(new LocalVariantMetadata(configurationName + "-" + name, new NestedVariantIdentifier(configurationIdentifier, name), displayName, attributes, ImmutableCapabilities.of(capabilities), variantArtifacts));
            }
        });

        // We must call this before collecting dependency state, since dependency actions may modify the hierarchy.
        runDependencyActionsInHierarchy(configuration);

        // Collect all dependencies and excludes in hierarchy.
        ImmutableAttributes attributes = configuration.getAttributes().asImmutable();
        ImmutableSet<String> hierarchy = Configurations.getNames(configuration.getHierarchy());

        CalculatedValue<DefaultLocalConfigurationMetadata.ConfigurationDependencyMetadata> dependencies =
            calculatedValueContainerFactory.create(Describables.of("Dependency state for", description), context -> {
                // TODO: Do we need to acquire project lock from `model`? getState calls user code.
                DependencyState state = getState(configurationsProvider, hierarchy, dependencyCache);
                return new DefaultLocalConfigurationMetadata.ConfigurationDependencyMetadata(
                    maybeForceDependencies(state.dependencies, attributes), state.files, state.excludes
                );
            });

        List<PublishArtifact> sourceArtifacts = artifactBuilder.build();
        CalculatedValue<ImmutableList<LocalComponentArtifactMetadata>> artifacts =
            getConfigurationArtifacts(parent, model, calculatedValueContainerFactory, configurationName, description, hierarchy, sourceArtifacts);

        return new DefaultLocalConfigurationMetadata(
            configurationName,
            description,
            componentId,
            configuration.isVisible(),
            configuration.isTransitive(),
            hierarchy,
            attributes,
            ImmutableCapabilities.of(Configurations.collectCapabilities(configuration, Sets.newHashSet(), Sets.newHashSet())),
            configuration.isCanBeConsumed(),
            configuration.isDeprecatedForConsumption(),
            configuration.isCanBeResolved(),
            dependencies,
            variantsBuilder.build(),
            calculatedValueContainerFactory,
            artifacts
        );
    }

    private static CalculatedValue<ImmutableList<LocalComponentArtifactMetadata>> getConfigurationArtifacts(
        LocalComponentMetadata parent, ModelContainer<?> model, CalculatedValueContainerFactory calculatedValueContainerFactory,
        String name, String description, ImmutableSet<String> hierarchy, List<PublishArtifact> sourceArtifacts
    ) {
        CalculatedValue<ImmutableList<LocalComponentArtifactMetadata>> artifacts =
            calculatedValueContainerFactory.create(Describables.of(description, "artifacts"), context -> {
                if (sourceArtifacts.isEmpty() && hierarchy.isEmpty()) {
                    return ImmutableList.of();
                } else {
                    return model.fromMutableState(m -> {
                        Set<LocalComponentArtifactMetadata> result = new LinkedHashSet<>(sourceArtifacts.size());
                        for (PublishArtifact sourceArtifact : sourceArtifacts) {
                            // The following line may realize tasks, so project lock must be held.
                            result.add(new PublishArtifactLocalArtifactMetadata(parent.getId(), sourceArtifact));
                        }
                        for (String config : hierarchy) {
                            if (config.equals(name)) {
                                continue;
                            }
                            // TODO: Deprecate the behavior of inheriting artifacts from parent configurations.
                            LocalConfigurationMetadata conf = parent.getConfiguration(config);
                            result.addAll(conf.prepareToResolveArtifacts().getArtifacts());
                        }
                        return ImmutableList.copyOf(result);
                    });
                }
            });
        return artifacts;
    }

    private static CalculatedValue<ImmutableList<LocalComponentArtifactMetadata>> getVariantArtifacts(
        ComponentIdentifier componentId, DisplayName displayName, Collection<? extends PublishArtifact> sourceArtifacts, ModelContainer<?> model, CalculatedValueContainerFactory calculatedValueContainerFactory
    ) {
        return calculatedValueContainerFactory.create(Describables.of(displayName, "artifacts"), context -> {
            if (sourceArtifacts.isEmpty()) {
                return ImmutableList.of();
            } else {
                return model.fromMutableState(m -> {
                    ImmutableList.Builder<LocalComponentArtifactMetadata> result = ImmutableList.builderWithExpectedSize(sourceArtifacts.size());
                    for (PublishArtifact sourceArtifact : sourceArtifacts) {
                        result.add(new PublishArtifactLocalArtifactMetadata(componentId, sourceArtifact));
                    }
                    return result.build();
                });
            }
        });
    }

    /**
     * Runs the dependency actions for all configurations in {@code conf}'s hierarchy.
     *
     * <p>Specifically handles the case where {@link Configuration#extendsFrom} is called during the
     * dependency action execution.</p>
     */
    private static void runDependencyActionsInHierarchy(ConfigurationInternal conf) {
        Set<Configuration> seen = new HashSet<>();
        Queue<Configuration> remaining = new ArrayDeque<>();
        remaining.add(conf);
        seen.add(conf);

        while (!remaining.isEmpty()) {
            Configuration current = remaining.remove();
            ((ConfigurationInternal) current).runDependencyActions();

            for (Configuration parent : current.getExtendsFrom()) {
                if (seen.add(parent)) {
                    remaining.add(parent);
                }
            }
        }
    }

    /**
     * Collect all dependencies and excludes of all configurations in the provided {@code hierarchy}.
     */
    private DependencyState getState(
        ConfigurationsProvider configurations,
        ImmutableSet<String> hierarchy,
        DependencyCache cache
    ) {
        ImmutableList.Builder<LocalOriginDependencyMetadata> dependencies = ImmutableList.builder();
        ImmutableSet.Builder<LocalFileDependencyMetadata> files = ImmutableSet.builder();
        ImmutableList.Builder<ExcludeMetadata> excludes = ImmutableList.builder();

        configurations.visitAll(config -> {
            if (hierarchy.contains(config.getName())) {
                DependencyState defined = getDefinedState(config, cache);
                dependencies.addAll(defined.dependencies);
                files.addAll(defined.files);
                excludes.addAll(defined.excludes);
            }
        });

        return new DependencyState(dependencies.build(), files.build(), excludes.build());
    }

    /**
     * Get the defined dependencies and excludes for {@code configuration}, while also caching the result.
     */
    private DependencyState getDefinedState(ConfigurationInternal configuration, DependencyCache cache) {
        return cache.computeIfAbsent(configuration, this::doGetDefinedState);
    }

    /**
     * Calculate the defined dependencies and excludes for {@code configuration}, while converting the
     * DSL representation to the internal representation.
     */
    @SuppressWarnings("deprecation")
    private DependencyState doGetDefinedState(ConfigurationInternal configuration) {

        ImmutableList.Builder<LocalOriginDependencyMetadata> dependencyBuilder = ImmutableList.builder();
        ImmutableSet.Builder<LocalFileDependencyMetadata> fileBuilder = ImmutableSet.builder();
        ImmutableList.Builder<ExcludeMetadata> excludeBuilder = ImmutableList.builder();

        // Configurations that are not declarable should not have dependencies or constraints present,
        // but we need to allow dependencies to be checked to avoid emitting many warnings when the
        // Kotlin plugin is applied.  This is because applying the Kotlin plugin adds dependencies
        // to the testRuntimeClasspath configuration, which is not declarable.
        // To demonstrate this, add a check for configuration.isCanBeDeclared() && configuration.assertHasNoDeclarations() if not
        // and run tests such as KotlinDslPluginTest, or the building-kotlin-applications samples and you'll configurations which
        // aren't declarable but have declared dependencies present.
        for (Dependency dependency : configuration.getDependencies()) {
            if (dependency instanceof ModuleDependency) {
                ModuleDependency moduleDependency = (ModuleDependency) dependency;
                dependencyBuilder.add(dependencyMetadataFactory.createDependencyMetadata(moduleDependency));
            } else if (dependency instanceof FileCollectionDependency) {
                final FileCollectionDependency fileDependency = (FileCollectionDependency) dependency;
                fileBuilder.add(new DefaultLocalFileDependencyMetadata(fileDependency));
            } else {
                throw new IllegalArgumentException("Cannot convert dependency " + dependency + " to local component dependency metadata.");
            }
        }

        // Configurations that are not declarable should not have dependencies or constraints present,
        // no smoke-tested plugins add constraints, so we should be able to safely throw an exception here
        // if we find any - but we'll avoid doing so for now to avoid breaking any existing builds and to
        // remain consistent with the behavior for dependencies.
        for (DependencyConstraint dependencyConstraint : configuration.getDependencyConstraints()) {
            dependencyBuilder.add(dependencyMetadataFactory.createDependencyConstraintMetadata(dependencyConstraint));
        }

        for (ExcludeRule excludeRule : configuration.getExcludeRules()) {
            excludeBuilder.add(excludeRuleConverter.convertExcludeRule(excludeRule));
        }

        return new DependencyState(dependencyBuilder.build(), fileBuilder.build(), excludeBuilder.build());
    }

    private static ImmutableList<LocalOriginDependencyMetadata> maybeForceDependencies(
        ImmutableList<LocalOriginDependencyMetadata> dependencies,
        ImmutableAttributes attributes
    ) {
        AttributeValue<Category> attributeValue = attributes.findEntry(Category.CATEGORY_ATTRIBUTE);
        if (!attributeValue.isPresent() || !attributeValue.get().getName().equals(Category.ENFORCED_PLATFORM)) {
            return dependencies;
        }

        // Need to wrap all dependencies to force them.
        ImmutableList.Builder<LocalOriginDependencyMetadata> forcedDependencies = ImmutableList.builder();
        for (LocalOriginDependencyMetadata rawDependency : dependencies) {
            forcedDependencies.add(rawDependency.forced());
        }
        return forcedDependencies.build();
    }

    /**
     * {@link VariantResolveMetadata.Identifier} implementation for non-implicit sub-variants of a configuration.
     */
    private static class NestedVariantIdentifier implements VariantResolveMetadata.Identifier {
        private final VariantResolveMetadata.Identifier parent;
        private final String name;

        public NestedVariantIdentifier(VariantResolveMetadata.Identifier parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        @Override
        public int hashCode() {
            return parent.hashCode() ^ name.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            NestedVariantIdentifier other = (NestedVariantIdentifier) obj;
            return parent.equals(other.parent) && name.equals(other.name);
        }
    }

    /**
     * Default implementation of {@link LocalFileDependencyMetadata}.
     */
    private static class DefaultLocalFileDependencyMetadata implements LocalFileDependencyMetadata {
        private final FileCollectionDependency fileDependency;

        DefaultLocalFileDependencyMetadata(FileCollectionDependency fileDependency) {
            this.fileDependency = fileDependency;
        }

        @Override
        public FileCollectionDependency getSource() {
            return fileDependency;
        }

        @Override @Nullable
        public ComponentIdentifier getComponentId() {
            return ((SelfResolvingDependencyInternal) fileDependency).getTargetComponentId();
        }

        @Override
        public FileCollectionInternal getFiles() {
            return (FileCollectionInternal) fileDependency.getFiles();
        }
    }
}
