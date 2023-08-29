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

private val IdeaPrefix get() = System.getProperty("idea.platform.prefix")

/**
 * Returns `true` if the current ide is Intellij-IDEA.
 *
 * @author chachako
 */
val isIntelliJ: Boolean get() = IdeaPrefix?.startsWith("IDEA", ignoreCase = true) ?: false

/**
 * Returns `true` if the current ide is Android Studio.
 *
 * @author chachako
 */
val isAndroidStudio: Boolean get() = IdeaPrefix?.startsWith("AndroidStudio", ignoreCase = true) ?: false

/**
 * Returns true if it is currently running in a CI environment.
 *
 * @author chachako
 */
val isCiEnvironment: Boolean get() = System.getenv("CI") != null
