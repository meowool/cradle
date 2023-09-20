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

package com.meowool.cradle.util

import com.meowool.cradle.MavenMirrorRepository
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.kotlin.dsl.*

/**
 * Returns to the [MavenMirrorRepository] instance to get the mirror repository.
 *
 * @author chachako
 */
val mavenMirror: MavenMirrorRepository inline get() = MavenMirrorRepository

/**
 * Adds and configures a Maven repository.
 *
 * The provided [url] value is evaluated as per [org.gradle.api.Project.uri]. This means, for example, you can pass in a `File` object, or a relative path to be evaluated relative
 * to the project directory.
 *
 * @param name the name for this repository.
 * @param url The base URL of this repository. This URL is used to find both POMs and artifact files.
 * @param snapshotsOnly If `true`, it means that the repository only contains snapshot artifacts.
 * @param action The action to use to configure the repository.
 * @return The added repository.
 *
 * @see [RepositoryHandler.maven]
 * @see [MavenArtifactRepository.setUrl]
 *
 * @author chachako
 */
fun RepositoryHandler.maven(
    name: String,
    url: Any,
    snapshotsOnly: Boolean = false,
    action: MavenArtifactRepository.() -> Unit = {}
): MavenArtifactRepository = maven {
    setName(name)
    setUrl(url)
    if (snapshotsOnly) mavenContent { snapshotsOnly() }
    action()
}

/**
 * Adds and configures a Maven repository.
 *
 * The provided [url] value is evaluated as per [org.gradle.api.Project.uri]. This means, for example, you can pass in a `File` object, or a relative path to be evaluated relative
 * to the project directory.
 *
 * @param url the base URL of this repository. This URL is used to find both POMs and artifact files.
 * @param snapshotsOnly If `true`, it means that the repository only contains snapshot artifacts.
 * @return The added repository.
 *
 * @see [RepositoryHandler.maven]
 * @see [MavenArtifactRepository.setUrl]
 *
 * @author chachako
 */
fun RepositoryHandler.maven(
    url: Any,
    snapshotsOnly: Boolean
): MavenArtifactRepository = maven {
    setUrl(url)
    if (snapshotsOnly) mavenContent { snapshotsOnly() }
}

/**
 * Adds and configures a Maven repository.
 *
 * The provided [url] value is evaluated as per [org.gradle.api.Project.uri]. This means, for example, you can pass in a `File` object, or a relative path to be evaluated relative
 * to the project directory.
 *
 * @param url The base URL of this repository. This URL is used to find both POMs and artifact files.
 * @param snapshotsOnly If `true`, it means that the repository only contains snapshot artifacts.
 * @param action The action to use to configure the repository.
 * @return The added repository.
 *
 * @see [RepositoryHandler.maven]
 * @see [MavenArtifactRepository.setUrl]
 *
 * @author chachako
 */
fun RepositoryHandler.maven(
    url: Any,
    snapshotsOnly: Boolean,
    action: MavenArtifactRepository.() -> Unit
): MavenArtifactRepository =
    maven {
        setUrl(url)
        if (snapshotsOnly) mavenContent { snapshotsOnly() }
        action()
    }

/**
 * Adds and configures a Sonatype repository.
 *
 * @param includeS01 Whether to include the new sonatype repository,
 *   see [blog](https://central.sonatype.org/news/20210223_new-users-on-s01/).
 * @param includeOld Whether to include the old sonatype repository.
 *
 * @see maven
 *
 * @author chachako
 */
fun RepositoryHandler.sonatype(
    includeS01: Boolean = true,
    includeOld: Boolean = true,
    action: MavenArtifactRepository.() -> Unit = {}
) {
    if (includeS01) maven(
        name = "Sonatype OSS S01",
        url = "https://s01.oss.sonatype.org/content/repositories/public",
        action = action
    )
    if (includeOld) maven(
        name = "Sonatype OSS",
        url = "https://oss.sonatype.org/content/repositories/public",
        action = action
    )
}

/**
 * Adds and configures a Sonatype snapshots repository.
 *
 * @param includeS01 Whether to include the new sonatype repository,
 *   see [blog](https://central.sonatype.org/news/20210223_new-users-on-s01/).
 * @param includeOld Whether to include the old sonatype repository.
 *
 * @see maven
 *
 * @author chachako
 */
fun RepositoryHandler.sonatypeSnapshots(
    includeS01: Boolean = true,
    includeOld: Boolean = true,
    action: MavenArtifactRepository.() -> Unit = {}
) {
    if (includeS01) maven(
        name = "Sonatype OSS S01 Snapshots",
        url = "https://s01.oss.sonatype.org/content/repositories/snapshots",
        snapshotsOnly = true,
        action
    )
    if (includeOld) maven(
        name = "Sonatype OSS Snapshots",
        url = "https://oss.sonatype.org/content/repositories/snapshots",
        snapshotsOnly = true,
        action
    )
}

/**
 * Adds a repository which looks in Bintray's JCenter repository of Bintray for [Project.dependencies].
 *
 * @author chachako
 */
@Deprecated(
    message = "JCenter sunset in February 2021, it is recommended to use mavenCentral, but you can also use mirrors.",
    replaceWith = ReplaceWith("maven(mavenMirror.aliyun.jcenter)")
)
fun RepositoryHandler.jcenter(snapshotsOnly: Boolean = false, action: MavenArtifactRepository.() -> Unit = {}) =
    maven(name = "JCenter", url = mavenMirror.aliyun.jcenter, snapshotsOnly, action)

/**
 * Adds a repository which looks in Jitpack repository for [Project.dependencies].
 *
 * @author chachako
 */
fun RepositoryHandler.jitpack(snapshotsOnly: Boolean = false, action: MavenArtifactRepository.() -> Unit = {}) =
    maven(name = "Jitpack", url = "https://jitpack.io", snapshotsOnly, action)
