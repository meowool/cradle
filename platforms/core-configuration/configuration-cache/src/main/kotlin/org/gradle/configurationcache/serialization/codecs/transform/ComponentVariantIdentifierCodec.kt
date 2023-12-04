/*
 * Copyright 2020 the original author or authors.
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

package org.gradle.configurationcache.serialization.codecs.transform

import org.gradle.api.artifacts.component.ComponentIdentifier
import org.gradle.api.capabilities.Capability
import org.gradle.api.internal.artifacts.transform.ComponentVariantIdentifier
import org.gradle.api.internal.attributes.ImmutableAttributes
import org.gradle.configurationcache.extensions.uncheckedCast
import org.gradle.configurationcache.serialization.Codec
import org.gradle.configurationcache.serialization.ReadContext
import org.gradle.configurationcache.serialization.WriteContext
import org.gradle.configurationcache.serialization.decodePreservingSharedIdentity
import org.gradle.configurationcache.serialization.encodePreservingSharedIdentityOf
import org.gradle.configurationcache.serialization.readList
import org.gradle.configurationcache.serialization.readNonNull
import org.gradle.configurationcache.serialization.writeCollection


/**
 * Codec for [ComponentVariantIdentifier] instances.
 */
object ComponentVariantIdentifierCodec : Codec<ComponentVariantIdentifier> {
    override suspend fun WriteContext.encode(value: ComponentVariantIdentifier) {
        encodePreservingSharedIdentityOf(value) {
            write(value.componentId)
            write(value.attributes)
            writeCollection(value.capabilities)
        }
    }

    override suspend fun ReadContext.decode(): ComponentVariantIdentifier {
        return decodePreservingSharedIdentity {
            val componentId = readNonNull<ComponentIdentifier>()
            val attributes = readNonNull<ImmutableAttributes>()
            val capabilities: List<Capability> = readList().uncheckedCast()
            ComponentVariantIdentifier(componentId, attributes, capabilities)
        }
    }
}
