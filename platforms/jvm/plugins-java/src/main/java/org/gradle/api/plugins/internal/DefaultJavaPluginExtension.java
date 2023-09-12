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

package org.gradle.api.plugins.internal;

import com.google.common.collect.ImmutableList;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.java.archives.Manifest;
import org.gradle.api.java.archives.internal.DefaultManifest;
import org.gradle.api.jvm.ModularitySpec;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.FeatureSpec;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.JavaResolutionConsistency;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.plugins.jvm.internal.JvmFeatureInternal;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.internal.Actions;
import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.internal.jvm.DefaultModularitySpec;
import org.gradle.jvm.toolchain.JavaToolchainSpec;
import org.gradle.jvm.toolchain.internal.DefaultToolchainSpec;
import org.gradle.jvm.toolchain.internal.JavaToolchainSpecInternal;
import org.gradle.testing.base.plugins.TestingBasePlugin;

import javax.inject.Inject;
import java.util.regex.Pattern;

import static org.gradle.api.attributes.DocsType.JAVADOC;
import static org.gradle.api.attributes.DocsType.SOURCES;
import static org.gradle.util.internal.ConfigureUtil.configure;

public class DefaultJavaPluginExtension implements JavaPluginExtension {
    private static final Pattern VALID_FEATURE_NAME = Pattern.compile("[a-zA-Z0-9]+");
    private final SourceSetContainer sourceSets;

    private final JavaToolchainSpecInternal toolchainSpec;
    private final ObjectFactory objectFactory;
    private final ModularitySpec modularity;
    private final JavaToolchainSpec toolchain;
    private final ProjectInternal project;

    private final DirectoryProperty docsDir;
    private final DirectoryProperty testResultsDir;
    private final DirectoryProperty testReportDir;
    private JavaVersion srcCompat;
    private JavaVersion targetCompat;
    private boolean autoTargetJvm = true;

    @Inject
    public DefaultJavaPluginExtension(ProjectInternal project, SourceSetContainer sourceSets, DefaultToolchainSpec toolchainSpec) {
        this.docsDir = project.getObjects().directoryProperty();
        this.testResultsDir = project.getObjects().directoryProperty();
        this.testReportDir = project.getObjects().directoryProperty(); //TestingBasePlugin.TESTS_DIR_NAME;
        this.project = project;
        this.sourceSets = sourceSets;
        this.toolchainSpec = toolchainSpec;
        this.objectFactory = project.getObjects();
        this.modularity = objectFactory.newInstance(DefaultModularitySpec.class);
        this.toolchain = toolchainSpec;
        configureDefaults();
    }

    private void configureDefaults() {
        docsDir.convention(project.getLayout().getBuildDirectory().dir("docs"));
        testResultsDir.convention(project.getLayout().getBuildDirectory().dir(TestingBasePlugin.TEST_RESULTS_DIR_NAME));
        testReportDir.convention(project.getExtensions().getByType(ReportingExtension.class).getBaseDirectory().dir(TestingBasePlugin.TESTS_DIR_NAME));
    }

    @Override
    public Object sourceSets(@SuppressWarnings("rawtypes") Closure closure) {
        return sourceSets.configure(closure);
    }

    @Override
    public DirectoryProperty getDocsDir() {
        return docsDir;
    }

    @Override
    public DirectoryProperty getTestResultsDir() {
        return testResultsDir;
    }

    @Override
    public DirectoryProperty getTestReportDir() {
        return testReportDir;
    }

    @Override
    public JavaVersion getSourceCompatibility() {
        if (srcCompat != null) {
            return srcCompat;
        } else if (toolchainSpec != null && toolchainSpec.isConfigured()) {
            return JavaVersion.toVersion(toolchainSpec.getLanguageVersion().get().toString());
        } else {
            return JavaVersion.current();
        }
    }

    public JavaVersion getRawSourceCompatibility() {
        return srcCompat;
    }

    @Override
    public void setSourceCompatibility(Object value) {
        setSourceCompatibility(JavaVersion.toVersion(value));
    }

    @Override
    public void setSourceCompatibility(JavaVersion value) {
        srcCompat = value;
    }

    @Override
    public JavaVersion getTargetCompatibility() {
        return targetCompat != null ? targetCompat : getSourceCompatibility();
    }

    public JavaVersion getRawTargetCompatibility() {
        return targetCompat;
    }

    @Override
    public void setTargetCompatibility(Object value) {
        setTargetCompatibility(JavaVersion.toVersion(value));
    }

    @Override
    public void setTargetCompatibility(JavaVersion value) {
        targetCompat = value;
    }

    @Override
    public Manifest manifest() {
        return manifest(Actions.doNothing());
    }

    @Override
    public Manifest manifest(@SuppressWarnings("rawtypes") Closure closure) {
        return configure(closure, createManifest());
    }

    @Override
    public Manifest manifest(Action<? super Manifest> action) {
        Manifest manifest = createManifest();
        action.execute(manifest);
        return manifest;
    }

    private Manifest createManifest() {
        return new DefaultManifest(project.getFileResolver());
    }

    @Override
    public SourceSetContainer getSourceSets() {
        return sourceSets;
    }

    @Override
    public void disableAutoTargetJvm() {
        this.autoTargetJvm = false;
    }

    @Override
    public boolean getAutoTargetJvmDisabled() {
        return !autoTargetJvm;
    }

    @Override
    public void registerFeature(String name, Action<? super FeatureSpec> configureAction) {
        DefaultJavaFeatureSpec spec = new DefaultJavaFeatureSpec(validateFeatureName(name), project);
        configureAction.execute(spec);
        spec.create();
    }

    @Override
    public void withJavadocJar() {
        maybeEmitMissingJavaComponentDeprecation("withJavadocJar()");

        if (project.getPlugins().hasPlugin(JavaPlugin.class)) {
            JavaPluginHelper.getJavaComponent(project).withJavadocJar();
        } else {
            SourceSet main = getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            JvmPluginsHelper.createDocumentationVariantWithArtifact(
                main.getJavadocElementsConfigurationName(),
                null,
                JAVADOC,
                ImmutableList.of(),
                main.getJavadocJarTaskName(),
                project.getTasks().named(main.getJavadocTaskName()),
                project
            );
        }
    }

    @Override
    public void withSourcesJar() {
        maybeEmitMissingJavaComponentDeprecation("withSourcesJar()");

        if (project.getPlugins().hasPlugin(JavaPlugin.class)) {
            JavaPluginHelper.getJavaComponent(project).withSourcesJar();
        } else {
            SourceSet main = getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            JvmPluginsHelper.createDocumentationVariantWithArtifact(
                main.getSourcesElementsConfigurationName(),
                null,
                SOURCES,
                ImmutableList.of(),
                main.getSourcesJarTaskName(),
                main.getAllSource(),
                project
            );
        }
    }

    @Override
    public ModularitySpec getModularity() {
        return modularity;
    }

    @Override
    public JavaToolchainSpec getToolchain() {
        return toolchain;
    }

    @Override
    public JavaToolchainSpec toolchain(Action<? super JavaToolchainSpec> action) {
        action.execute(toolchain);
        return toolchain;
    }

    @Override
    public void consistentResolution(Action<? super JavaResolutionConsistency> action) {
        maybeEmitMissingJavaComponentDeprecation("consistentResolution(Action)");

        final ConfigurationContainer configurations = project.getConfigurations();
        final SourceSetContainer sourceSets = getSourceSets();
        action.execute(project.getObjects().newInstance(DefaultJavaPluginExtension.DefaultJavaResolutionConsistency.class, project, sourceSets, configurations));
    }

    private static String validateFeatureName(String name) {
        if (!VALID_FEATURE_NAME.matcher(name).matches()) {
            throw new InvalidUserDataException("Invalid feature name '" + name + "'. Must match " + VALID_FEATURE_NAME.pattern());
        }
        return name;
    }

    private void maybeEmitMissingJavaComponentDeprecation(String name) {
        if (!project.getPlugins().hasPlugin(JavaPlugin.class)) {
            DeprecationLogger.deprecateBehaviour(name + " was called without the presence of the java component.")
                .withAdvice("Apply a JVM component plugin such as: java-library, application, groovy, or scala")
                .willBeRemovedInGradle9()
                .withUpgradeGuideSection(8, "java_extension_without_java_component")
                .nagUser();
        }
    }

    static class DefaultJavaResolutionConsistency implements JavaResolutionConsistency {
        private final Configuration mainCompileClasspath;
        private final Configuration mainRuntimeClasspath;
        private final Configuration testCompileClasspath;
        private final Configuration testRuntimeClasspath;
        private final SourceSetContainer sourceSets;
        private final ConfigurationContainer configurations;

        @Inject
        public DefaultJavaResolutionConsistency(Project project, SourceSetContainer sourceSets, ConfigurationContainer configurations) {
            this.sourceSets = sourceSets;
            this.configurations = configurations;

            if (project.getPlugins().hasPlugin(JavaPlugin.class)) {
                JvmFeatureInternal mainFeature = JavaPluginHelper.getJavaComponent(project).getMainFeature();
                JvmTestSuite defaultTestSuite = JavaPluginHelper.getDefaultTestSuite(project);

                mainCompileClasspath = mainFeature.getCompileClasspathConfiguration();
                mainRuntimeClasspath = mainFeature.getRuntimeClasspathConfiguration();
                testCompileClasspath = findConfiguration(defaultTestSuite.getSources().getCompileClasspathConfigurationName());
                testRuntimeClasspath = findConfiguration(defaultTestSuite.getSources().getRuntimeClasspathConfigurationName());
            } else {
                SourceSet mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
                SourceSet testSourceSet = sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME);
                mainCompileClasspath = findConfiguration(mainSourceSet.getCompileClasspathConfigurationName());
                mainRuntimeClasspath = findConfiguration(mainSourceSet.getRuntimeClasspathConfigurationName());
                testCompileClasspath = findConfiguration(testSourceSet.getCompileClasspathConfigurationName());
                testRuntimeClasspath = findConfiguration(testSourceSet.getRuntimeClasspathConfigurationName());
            }
        }

        @Override
        public void useCompileClasspathVersions() {
            sourceSets.configureEach(this::applyCompileClasspathConsistency);
            testCompileClasspath.shouldResolveConsistentlyWith(mainCompileClasspath);
        }

        @Override
        public void useRuntimeClasspathVersions() {
            sourceSets.configureEach(this::applyRuntimeClasspathConsistency);
            testRuntimeClasspath.shouldResolveConsistentlyWith(mainRuntimeClasspath);
        }

        private void applyCompileClasspathConsistency(SourceSet sourceSet) {
            Configuration compileClasspath = findConfiguration(sourceSet.getCompileClasspathConfigurationName());
            Configuration runtimeClasspath = findConfiguration(sourceSet.getRuntimeClasspathConfigurationName());
            runtimeClasspath.shouldResolveConsistentlyWith(compileClasspath);
        }

        private void applyRuntimeClasspathConsistency(SourceSet sourceSet) {
            Configuration compileClasspath = findConfiguration(sourceSet.getCompileClasspathConfigurationName());
            Configuration runtimeClasspath = findConfiguration(sourceSet.getRuntimeClasspathConfigurationName());
            compileClasspath.shouldResolveConsistentlyWith(runtimeClasspath);
        }

        private Configuration findConfiguration(String configName) {
            return configurations.getByName(configName);
        }
    }
}
