/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.language.objectivecpp.plugins

import org.gradle.api.Plugin
import org.gradle.api.Task
import org.gradle.language.base.LanguageSourceSet
import org.gradle.language.objectivecpp.ObjectiveCppSourceSet
import org.gradle.language.AbstractNativeComponentPluginTest
import org.gradle.language.objectivecpp.tasks.ObjectiveCppCompile
import spock.lang.Ignore

@Ignore("https://github.com/gradle/gradle-private/issues/3818")
class ObjectiveCppPluginTest extends AbstractNativeComponentPluginTest {
    @Override
    Class<? extends Plugin> getPluginClass() {
        return ObjectiveCppPlugin
    }

    @Override
    Class<? extends LanguageSourceSet> getSourceSetClass() {
        return ObjectiveCppSourceSet
    }

    @Override
    Class<? extends Task> getCompileTaskClass() {
        return ObjectiveCppCompile
    }

    @Override
    String getPluginName() {
        return "objcpp"
    }
}
