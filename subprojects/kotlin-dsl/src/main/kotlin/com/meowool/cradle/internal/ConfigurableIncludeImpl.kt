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
import org.gradle.api.initialization.Settings
import org.gradle.api.internal.SettingsInternal
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import java.io.File
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.isRegularFile
import kotlin.io.path.pathString
import kotlin.io.path.relativeTo

/**
 * @author chachako
 */
@PublishedApi
internal class ConfigurableIncludeImpl(private val outerDirectory: Path) :
    PatternFilterable by PatternSet(),
    ConfigurableInclude {

    fun includeAll(settings: Settings) {
        // Exclude all the composite builds that have been included
        (settings as? SettingsInternal)?.includedBuilds?.map { it.rootDir }?.let(::exclude)

        val includes = includes.map(::Path).toMutableSet()
        val excludes = excludes.map(::Path)

        // Walking the outermost directory to identify the final directories to be included
        Files.walkFileTree(outerDirectory, object : SimpleFileVisitor<Path>() {
            override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
                // Skip the excluded and reserved directory
                if (
                    dir in excludes ||
                    dir.fileName.toString() in reservedNames ||
                    dir.resolve(".project.exclude").isRegularFile()
                ) {
                    return FileVisitResult.SKIP_SUBTREE
                }

                // If the directory is a Gradle project, include it
                if (gradleScriptNames.any { dir.resolve(it).isRegularFile() }) {
                    includes.add(dir)
                }

                return super.preVisitDirectory(dir, attrs)
            }
        })

        // Now, we can add all the directories to be included to the build
        settings.include(includes.map {
            val relativePath = it.absolute().relativeTo(outerDirectory.absolute())
            val projectPath = relativePath.pathString.replace(File.separatorChar, ':')

            ":$projectPath"
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
