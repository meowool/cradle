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

package com.meowool.cradle.internal

import com.meowool.cradle.util.ConfigurableInclude
import org.gradle.api.file.FileVisitDetails
import org.gradle.api.file.FileVisitor
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.SettingsInternal
import org.gradle.api.internal.file.collections.DefaultDirectoryFileTreeFactory
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * @author chachako
 */
@PublishedApi
internal class ConfigurableIncludeImpl(
    private val outerDirectory: File,
    private val patternSet: PatternSet = PatternSet(),
) : PatternFilterable by patternSet, ConfigurableInclude {

    fun includeAll(settings: Settings) {
        // Exclude all the composite builds that have been included
        (settings as? SettingsInternal)?.includedBuilds?.map { it.rootDir }?.let(::exclude)
        // Skipping folders to exclude and to reserve
        exclude(*reservedNames)
        exclude { !it.isDirectory || it.file.resolve(".project.exclude").exists() }
        // Now, we can walk the outermost directory to identify the final directories to be included
        DefaultDirectoryFileTreeFactory().create(outerDirectory, patternSet).visit(object : FileVisitor {
            override fun visitFile(fileDetails: FileVisitDetails) = Unit
            override fun visitDir(dirDetails: FileVisitDetails) {
                // Only include it when this directory is a Gradle project
                if (gradleScriptNames.any { name -> dirDetails.file.resolve(name).exists() }) {
                    settings.include(":${dirDetails.relativePath.pathString.replace(File.separatorChar, ':')}")
                }
            }
        })
    }

    override fun include(vararg directories: File): ConfigurableInclude = apply {
        include(directories.map { it.absolutePath })
    }

    override fun include(vararg directories: Path): ConfigurableInclude = apply {
        include(directories.map { it.absolutePathString() })
    }

    override fun include(directories: Iterable<Any>): ConfigurableInclude = apply {
        directories.forEach {
            when (it) {
                is File -> include(it)
                is Path -> include(it)
                is String -> include(it)
                else -> throw IllegalArgumentException("Unsupported directory type: ${it::class}")
            }
        }
    }

    override fun exclude(vararg directories: File): ConfigurableInclude = apply {
        exclude(directories.map { it.absolutePath })
    }

    override fun exclude(vararg directories: Path): ConfigurableInclude = apply {
        exclude(directories.map { it.absolutePathString() })
    }

    override fun exclude(directories: Iterable<Any>): ConfigurableInclude = apply {
        directories.forEach {
            when (it) {
                is File -> exclude(it)
                is Path -> exclude(it)
                is String -> exclude(it)
                else -> throw IllegalArgumentException("Unsupported directory type: ${it::class}")
            }
        }
    }

    companion object {
        private val reservedNames = arrayOf(
            ".git", ".gradle", ".fleet", ".vscode", ".idea",
            "build", "buildSrc",
        )

        private val gradleScriptNames = arrayOf(
            "build.gradle.kts", "build.gradle",
            "settings.gradle.kts", "settings.gradle"
        )
    }
}
