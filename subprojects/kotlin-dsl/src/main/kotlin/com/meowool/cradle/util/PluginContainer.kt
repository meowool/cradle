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


import org.gradle.api.Plugin
import org.gradle.api.plugins.PluginContainer

/**
 * Kotlin's extension function taking [T] for [org.gradle.kotlin.dsl.apply].
 *
 * @author chachako
 */
inline fun <reified T : Plugin<*>> PluginContainer.apply(): T = apply(T::class.java)

/**
 * An extension function taking [T] for [PluginContainer.hasPlugin].
 *
 * @author chachako
 */
inline fun <reified T : Plugin<*>> PluginContainer.hasPlugin(): Boolean = hasPlugin(T::class.java)

/**
 * Returns `true` if no plugin of the [T] is applied.
 *
 * @see hasPlugin
 * @author chachako
 */
inline fun <reified T : Plugin<*>> PluginContainer.hasNotPlugin(): Boolean = hasPlugin(T::class.java).not()
