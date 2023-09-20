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
@file:Suppress("SpellCheckingInspection")

package com.meowool.cradle.util

import com.meowool.cradle.internal.ConfigurableIncludeImpl
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.util.PatternFilterable
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * A configurable include interface that allows you to include or exclude certain directories to make
 * [includeAllProjects] run as expected.
 *
 * ## Important
 *
 * All 'include**' functions in this interface will only take effect on the specified directory itself.
 * For example, if "root" directory is included by [include], it will only include the "root" directory
 * itself, and any of its subdirectories will not be included.
 *
 * However, all 'exclude**' functions in this interface will take effect on the specified directory and
 * its subdirectories.
 *
 * @author chachako
 */
interface ConfigurableInclude : PatternFilterable {
    /**
     * Adds the given [directories] as projects to the build by [Settings.include].
     */
    fun include(vararg directories: File): ConfigurableInclude

    /**
     * Adds the given [directories] as projects to the build by [Settings.include].
     */
    fun include(vararg directories: Path): ConfigurableInclude

    /**
     * Adds the given [directories] as projects to the build by [Settings.include].
     *
     * @param directories The item type is allowed to be a [File], or a [Path],
     *   or a [String].
     */
    fun include(directories: Iterable<Any>): ConfigurableInclude

    /**
     * Excludes the given [directories] to avoid them and their subdirectories being
     * added to the build as projects by [Settings.include].
     */
    fun exclude(vararg directories: File): ConfigurableInclude

    /**
     * Excludes the given [directories] to avoid them and their subdirectories being
     * added to the build as projects by [Settings.include].
     */
    fun exclude(vararg directories: Path): ConfigurableInclude

    /**
     * Excludes the given [directories] to avoid them and their subdirectories being
     * added to the build as projects by [Settings.include].
     *
     * @param directories The item type is allowed to be a [File], or a [Path],
     *   or a [String].
     */
    fun exclude(directories: Iterable<Any>): ConfigurableInclude
}

/**
 * Recursively include all projects that contain `build.gradle`, `build.gradle.kts`,
 * `settings.gradle` or `settings.gradle.kts` in the [parent].
 *
 * This is useful for (large) projects with multiple modules, as the function allows you to not
 * have to manually hardcode the paths of the modules one by one.
 *
 * ## Skills
 *
 * For the project directory you don't want to add to the build, you can add a file named
 * ".project.exclude" to it or use [ConfigurableInclude.exclude] in the [pattern] to declare
 * it, so that it and its subdirectories will not be included.
 *
 * ## Important
 *
 * By default, this function will not import any subdirectories in any directory
 * named 'build', 'buildSrc', '.git', '.gradle', '.fleet', '.vscode', or '.idea',
 * because they are the default reserved names, so if necessary, please explicitly
 * import them by [Settings.include] or [ConfigurableInclude.include] in [pattern].
 *
 * @param parent A parent directory including all projects.
 * @param pattern A pattern that can be used to configure directories to be included.
 *
 * @see Settings.include
 *
 * @author chachako
 */
inline fun Settings.includeAllProjects(
    parent: File = rootDir,
    pattern: ConfigurableInclude.() -> Unit = {},
) = ConfigurableIncludeImpl(parent).apply(pattern).includeAll(this)

/**
 * Recursively include all projects that contain `build.gradle`, `build.gradle.kts`,
 * `settings.gradle` or `settings.gradle.kts` in the [parent].
 *
 * This is useful for (large) projects with multiple modules, as the function allows you to not
 * have to manually hardcode the paths of the modules one by one.
 *
 * ## Skills
 *
 * For the project directory you don't want to add to the build, you can add a file named
 * ".project.exclude" to it or use [ConfigurableInclude.exclude] in the [pattern] to declare
 * it, so that it and its subdirectories will not be included.
 *
 * ## Important
 *
 * By default, this function will not import any subdirectories in any directory
 * named 'build', 'buildSrc', '.git', '.gradle', '.fleet', '.vscode', or '.idea',
 * because they are the default reserved names, so if necessary, please explicitly
 * import them by [Settings.include] or [ConfigurableInclude.include] in [pattern].
 *
 * @param parent A parent directory including all projects, and it will not be included by itself.
 * @param pattern A pattern that can be used to configure directories to be included.
 *
 * @see Settings.include
 *
 * @author chachako
 */
inline fun Settings.includeAllProjects(
    parent: Path,
    pattern: ConfigurableInclude.() -> Unit = {},
) = includeAllProjects(parent.toFile(), pattern)

/**
 * Recursively include all projects that contain `build.gradle`, `build.gradle.kts`,
 * `settings.gradle` or `settings.gradle.kts` in the [parent].
 *
 * This is useful for (large) projects with multiple modules, as the function allows you to not
 * have to manually hardcode the paths of the modules one by one.
 *
 * ## Skills
 *
 * For the project directory you don't want to add to the build, you can add a file named
 * ".project.exclude" to it or use [ConfigurableInclude.exclude] in the [pattern] to declare
 * it, so that it and its subdirectories will not be included.
 *
 * ## Important
 *
 * By default, this function will not import any subdirectories in any directory
 * named 'build', 'buildSrc', '.git', '.gradle', '.fleet', '.vscode', or '.idea',
 * because they are the default reserved names, so if necessary, please explicitly
 * import them by [Settings.include] or [ConfigurableInclude.include] in [pattern].
 *
 * @param parent A parent directory including all projects.
 * @param pattern A pattern that can be used to configure directories to be included.
 *
 * @see Settings.include
 *
 * @author chachako
 */
inline fun Settings.includeAllProjects(
    parent: String,
    pattern: ConfigurableInclude.() -> Unit = {},
) = includeAllProjects(File(parent), pattern)

