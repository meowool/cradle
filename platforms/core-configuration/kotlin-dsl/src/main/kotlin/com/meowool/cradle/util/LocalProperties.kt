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

@file:Suppress("GrazieInspection")

package com.meowool.cradle.util

import groovy.lang.MissingPropertyException
import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import java.io.File
import java.util.Properties

/**
 * Returns a [Properties] read from this file.
 * If this file does not exist, return `null`.
 *
 * @author chachako
 */
fun File.toProperties(): Properties? = when {
    exists() -> Properties().also { it.load(bufferedReader()) }
    else -> null
}

/**
 * Returns the value of the specified property in the `local.properties` file
 * in the project.
 *
 * If not found, return `null`.
 */
fun Project.localPropertyOrNull(key: String): String? = localProperties.getProperty(key)

/**
 * Returns the value of the specified property in the `local.properties` file
 * in the project.
 *
 * If not found, a [MissingPropertyException] will be thrown.
 */
fun Project.localProperty(key: String): String = localPropertyOrNull(key) ?: throw MissingPropertyException(
    "The property `$key` is not found in the `local.properties` file.",
    key,
    null,
)

/**
 * Return the properties of the `local.properties` file in the project.
 *
 * @author chachako
 */
val Project.localProperties: Properties
    get() = projectDir.resolve("local.properties").toProperties()
        ?: error("There is no `local.properties` file in the project(${projectDir.absolutePath})")

/**
 * Return the properties of the `local.properties` file in the root directory
 * of this settings.
 *
 * @author chachako
 */
val Settings.localProperties: Properties
    get() = rootDir.resolve("local.properties").toProperties()
        ?: error("There is no `local.properties` file in the directory: ${rootDir.absolutePath}")
