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

@file:Suppress("SpellCheckingInspection", "ConstPropertyName")

package com.meowool.cradle

/**
 * Represents the mirror repository of the maven repository.
 *
 * @author chachako
 */
object MavenMirrorRepository {
    /**
     * An accessor to access the "huaweicloud" mirror repository.
     *
     * See [Home](https://mirrors.huaweicloud.com/home) for more details.
     */
    const val huaweicloud: String = "https://repo.huaweicloud.com/repository/maven/"

    /**
     * An accessor to access the "tencent" mirror repository.
     */
    val tencent: Tencent inline get() = Tencent

    /**
     * See [Home](https://mirrors.cloud.tencent.com/) for more details.
     */
    object Tencent {
        private const val baseUrl = "https://mirrors.cloud.tencent.com/"

        const val maven: String = baseUrl + "maven/"
        const val gradle: String = baseUrl + "gradle/"
    }

    /**
     * An accessor to access the "aliyun" mirror repository.
     */
    val aliyun: Aliyun inline get() = Aliyun

    /**
     * See [Guide](https://developer.aliyun.com/mvn/guide) for more details.
     */
    object Aliyun {
        private const val baseUrl = "https://maven.aliyun.com/repository/"

        const val google: String = baseUrl + "google"
        const val public: String = baseUrl + "public"
        const val spring: String = baseUrl + "spring"
        const val central: String = baseUrl + "central"
        const val jcenter: String = baseUrl + "jcenter"
        const val grailsCore: String = baseUrl + "grails-core"
        const val springPlugin: String = baseUrl + "spring-plugin"
        const val gradlePlugin: String = baseUrl + "gradle-plugin"
        const val apacheSnapshots: String = baseUrl + "apache-snapshots"
    }
}
