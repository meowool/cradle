/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.kotlin.dsl.resolver

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.repositories.IvyArtifactRepository
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformParameters
import org.gradle.api.artifacts.transform.TransformSpec
import org.gradle.api.attributes.Attribute
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.kotlin.dsl.*
import org.gradle.util.GradleVersion
import java.io.File


interface SourceDistributionProvider {
    fun sourceDirs(): Collection<File>
}


class SourceDistributionResolver(private val project: Project) : SourceDistributionProvider {

    companion object {
        val artifactType = Attribute.of("artifactType", String::class.java)
        val zipType = "zip"
        val unzippedDistributionType = "unzipped-distribution"
        val sourceDirectory = "src-directory"
    }

    override fun sourceDirs(): Collection<File> =
        try {
            sourceDirs
        } catch (ex: Exception) {
            project.logger.warn("Unexpected exception while resolving Gradle distribution sources: ${ex.message}", ex)
            emptyList()
        }

    private
    val sourceDirs by lazy {
        createSourceRepository()
        registerTransforms()
        transientConfigurationForSourcesDownload().files
    }

    private
    fun registerTransforms() {
        registerTransform<UnzipDistribution> {
            from.attribute(artifactType, zipType)
            to.attribute(artifactType, unzippedDistributionType)
        }
        registerTransform<FindGradleSources> {
            from.attribute(artifactType, unzippedDistributionType)
            to.attribute(artifactType, sourceDirectory)
        }
    }

    private
    fun transientConfigurationForSourcesDownload() =
        detachedConfigurationFor(gradleSourceDependency()).apply {
            attributes.attribute(artifactType, sourceDirectory)
        }

    private
    fun detachedConfigurationFor(dependency: Dependency) =
        configurations.detachedConfiguration(dependency)

    private
    fun gradleSourceDependency() = dependencies.create(
        group = "cradle",
        name = "cradle",
        version = gradleVersion,
        configuration = null,
        classifier = "src",
        ext = "zip"
    )

    private
    fun createSourceRepository() = ivy {
        name = "Cradle distributions"
        setUrl("https://github.com/meowool/cradle/releases/download/v$gradleVersion")
        metadataSources {
            artifact()
        }
        patternLayout {
            artifact("[module]-[revision](-[classifier])(.[ext])")
        }
    }

    private
    inline fun <reified T : TransformAction<TransformParameters.None>> registerTransform(configure: Action<TransformSpec<TransformParameters.None>>) =
        dependencies.registerTransform(T::class.java, configure)

    private
    fun ivy(configure: Action<IvyArtifactRepository>) =
        repositories.ivy(configure)

    private
    fun previous(versionDigit: String) =
        Integer.parseInt(versionDigit) - 1

    private
    val resolver by lazy { projectInternal.newDetachedResolver() }

    private
    val projectInternal
        get() = project as ProjectInternal

    private
    val repositories
        get() = resolver.repositories

    private
    val configurations
        get() = resolver.configurations

    private
    val dependencies
        get() = resolver.dependencies

    private
    val gradleVersion
        get() = project.gradle.gradleVersion
}
