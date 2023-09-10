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

package org.gradle.smoketests

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import org.gradle.internal.reflect.validation.ValidationMessageChecker
import org.gradle.test.fixtures.file.TestFile
import org.gradle.test.precondition.Requires
import org.gradle.test.preconditions.IntegTestPreconditions
import org.gradle.test.preconditions.SmokeTestPreconditions
import org.gradle.test.preconditions.UnitTestPreconditions

/**
 * Smoke test verifying the external plugins.
 *
 */
@Requires([
    UnitTestPreconditions.Jdk9OrLater,
    IntegTestPreconditions.NotConfigCached,
    SmokeTestPreconditions.GradleBuildJvmSpecAvailable
])
class GradleBuildExternalPluginsValidationSmokeTest extends AbstractGradleceptionSmokeTest implements WithPluginValidation, ValidationMessageChecker {

    def setup() {
        allPlugins.projectPathToBuildDir = new GradleBuildDirLocator()
    }

    def "performs static validation of plugins used by the Gradle build"() {
        when:
        passingPlugins { id ->
            id.startsWith('gradlebuild') ||
            id.startsWith('Gradlebuild') ||
            id in [
                'com.diffplug.spotless',
                'com.gradleup.gr8',
                'org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin',
                'org.jetbrains.kotlin.gradle.targets.js.yarn.YarnPlugin',
                'me.champeau.jmh',
                'kotlin-sam-with-receiver',
                'org.jetbrains.gradle.plugin.idea-ext',
                'org.jetbrains.kotlin.gradle.scripting.internal.ScriptingGradleSubplugin',
                'org.jetbrains.kotlin.gradle.scripting.internal.ScriptingKotlinGradleSubplugin',
                'org.jetbrains.kotlin.jvm',
                'org.jlleitschuh.gradle.ktlint',
                'org.jlleitschuh.gradle.ktlint.KtlintBasePlugin',
                'org.jlleitschuh.gradle.ktlint.KtlintIdeaPlugin',
                'org.jetbrains.kotlin.gradle.targets.js.npm.NpmResolverPlugin',
                'org.jetbrains.kotlin.js',
                'org.asciidoctor.gradle.base.AsciidoctorBasePlugin',
                'org.asciidoctor.gradle.jvm.AsciidoctorJBasePlugin',
                'org.asciidoctor.gradle.jvm.AsciidoctorJPlugin',
                'org.asciidoctor.jvm.convert',
                'com.gradle.plugin-publish',
                'kotlin',
                'com.autonomousapps.dependency-analysis',
                'dev.adamko.dokkatoo.DokkatooBasePlugin$Inject',
                'dev.adamko.dokkatoo.adapters.DokkatooJavaAdapter$Inject',
                'dev.adamko.dokkatoo.adapters.DokkatooKotlinAdapter$Inject',
                'dev.adamko.dokkatoo.adapters.DokkatooAndroidAdapter$Inject',
                'dev.adamko.dokkatoo.formats.DokkatooHtmlPlugin$Inject',
            ]
        }

        then:
        validatePlugins()
    }

    void passingPlugins(Closure<Boolean> spec) {
        allPlugins.passing(spec)
    }

    void validatePlugins() {
        allPlugins.performValidation([
            "--no-parallel" // make sure we have consistent execution ordering as we skip cached tasks
        ])
    }

    void inProject(String projectPath, @DelegatesTo(value = ProjectValidation, strategy = Closure.DELEGATE_FIRST) Closure<?> spec) {
        def validation = new ProjectValidation(projectPath)
        spec.delegate = validation
        spec.resolveStrategy = Closure.DELEGATE_FIRST
        spec()
    }

    class ProjectValidation {
        private final String projectPath

        ProjectValidation(String projectPath) {
            this.projectPath = projectPath
        }

        void onPlugin(String id, @DelegatesTo(value = PluginValidation, strategy = Closure.DELEGATE_FIRST) Closure<?> spec) {
            allPlugins.onPlugin(id, projectPath, spec)
        }
    }

    private static class GradleBuildDirLocator implements ProjectBuildDirLocator {

        private Map<String, String> subprojects

        @Override
        TestFile getBuildDir(String projectPath, TestFile projectRoot) {
            if (projectPath == ':') {
                return projectRoot.file("build")
            } else {
                if (subprojects == null) {
                    ArrayNode arr = (ArrayNode) projectRoot.file(".teamcity/subprojects.json").withInputStream {
                        new ObjectMapper().readTree(it)
                    }
                    subprojects = [:]
                    for (int i = 0; i < arr.size(); i++) {
                        JsonNode node = arr.get(i)
                        subprojects.put(node.get("name").asText(), node.get("path").asText())
                    }
                }

                assert projectPath.startsWith(":")
                projectPath = projectPath.substring(1)
                assert !projectPath.contains(":")

                def path = subprojects.get(projectPath)
                if (path == null) {
                    throw new IllegalArgumentException("Cannot find build dir for project path '$projectPath'")
                }
                return projectRoot.file(path).file("build")
            }
        }
    }
}
