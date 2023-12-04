/*
 * Copyright 2022 the original author or authors.
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

package org.gradle.internal.classpath;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.specs.Spec;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * A special ClassPath that keeps track of the transformed "doubles" of the original classpath entries (JARs and class directories).
 * An entry can only appear once in the class path, regardless of having a transformed "double".
 * <p>
 * Operations on this ClassPath ({@code plus}, {@code removeIf}) keep the transforms for retained entries.
 * However, it is possible to override the entry when combining class paths, when both the receiver and the argument of {@code plus} contain the same entry.
 * As the class path is typically searched left-to-right when looking for a class, the entry (and its transformed "double" if present) from the receiver wins.
 * <p>
 * The class loaders constructed from this classpath can replace classes from the original classpath entries with transformed ones (from "double") when loading.
 */
public class TransformedClassPath implements ClassPath {
    private final ClassPath originalClassPath;
    // mapping of original -> "double"
    private final ImmutableMap<File, File> transforms;

    private TransformedClassPath(ClassPath originalClassPath, Map<File, File> transforms) {
        assert !(originalClassPath instanceof TransformedClassPath);
        this.originalClassPath = originalClassPath;
        this.transforms = ImmutableMap.copyOf(transforms);
    }

    @Override
    public boolean isEmpty() {
        return originalClassPath.isEmpty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method returns the list of original JAR/class directory URIs.
     */
    @Override
    public List<URI> getAsURIs() {
        return originalClassPath.getAsURIs();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method returns the list of original JARs/class directories.
     */
    @Override
    public List<File> getAsFiles() {
        return originalClassPath.getAsFiles();
    }

    /**
     * Returns the list of JARs/class directories that this class path consists of, but original entries are replaced with corresponding transformed "doubles".
     * The entries that have no "doubles" are returned as is.
     *
     * @return the list of JARs/class directories
     */
    public List<File> getAsTransformedFiles() {
        List<File> originals = new ArrayList<File>(originalClassPath.getAsFiles());
        ListIterator<File> iter = originals.listIterator();
        while (iter.hasNext()) {
            File original = iter.next();
            iter.set(transforms.getOrDefault(original, original));
        }
        return originals;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method returns the list of original JAR/class directory URLs.
     */
    @Override
    public List<URL> getAsURLs() {
        return originalClassPath.getAsURLs();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This method returns the array of original JAR/class directory URLs.
     */
    @Override
    public URL[] getAsURLArray() {
        return originalClassPath.getAsURLArray();
    }

    /**
     * Prepends the given classPath to this classpath.
     * No transformations are applied to the prepended class path.
     * <p>
     * This is a helper to implement DefaultClassPath.plus(TransformedClassPath) efficiently.
     *
     * @param classPath the classPath to prepend to this classpath
     * @return the new transformed classpath
     */
    TransformedClassPath prepend(DefaultClassPath classPath) {
        // If some entries from this classpath are also in the prepended classpath, then the prepended ones win.
        // Existing transforms for these entries have to be discarded.
        // We can think of the prepended classpath as the TransformedClassPath without actual transforms,
        // and then just append this classpath to it to achieve the desired behavior.
        return new TransformedClassPath(classPath, ImmutableMap.<File, File>of()).plusWithTransforms(this);
    }

    private TransformedClassPath plusWithTransforms(TransformedClassPath classPath) {
        ClassPath mergedOriginals = originalClassPath.plus(classPath.originalClassPath);

        // Merge transformations, keeping in mind that classpath is searched left-to-right.
        ImmutableMap.Builder<File, File> mergedTransforms = ImmutableMap.builderWithExpectedSize(transforms.size() + classPath.transforms.size());
        Set<File> thisClassPathFiles = ImmutableSet.copyOf(originalClassPath.getAsFiles());
        mergedTransforms.putAll(transforms);
        for (Map.Entry<File, File> appendedTransform : classPath.transforms.entrySet()) {
            // If the file is already present on this classpath, it keeps its transform (or lack thereof).
            if (!thisClassPathFiles.contains(appendedTransform.getKey())) {
                mergedTransforms.put(appendedTransform);
            }
        }

        // In the end, at most one instance of a transformed JAR should be recorded for any given file.
        return new TransformedClassPath(mergedOriginals, mergedTransforms.buildOrThrow());
    }

    /**
     * {@inheritDoc}
     * <p>
     * The appended classpath is not transformed.
     */
    @Override
    public TransformedClassPath plus(Collection<File> classPath) {
        return new TransformedClassPath(originalClassPath.plus(classPath), transforms);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The appended classpath is not additionally transformed.
     */
    @Override
    public TransformedClassPath plus(ClassPath classPath) {
        if (classPath instanceof TransformedClassPath) {
            return plusWithTransforms((TransformedClassPath) classPath);
        }
        return new TransformedClassPath(originalClassPath.plus(classPath), transforms);
    }

    /**
     * {@inheritDoc}
     * <p>
     * The predicate is applied to the original classpath entries. The returned classpath keeps corresponding transformations.
     */
    @Override
    public TransformedClassPath removeIf(Spec<? super File> filter) {
        ClassPath filteredClassPath = originalClassPath.removeIf(filter);
        Set<File> remainingOriginals = ImmutableSet.copyOf(filteredClassPath.getAsFiles());
        ImmutableMap.Builder<File, File> remainingTransforms = ImmutableMap.builderWithExpectedSize(Math.min(remainingOriginals.size(), transforms.size()));
        for (Map.Entry<File, File> remainingEntry : transforms.entrySet()) {
            if (remainingOriginals.contains(remainingEntry.getKey())) {
                remainingTransforms.put(remainingEntry);
            }
        }
        return new TransformedClassPath(filteredClassPath, remainingTransforms.build());
    }

    /**
     * Looks up the transformed JAR corresponding to the given classpath entry (JAR/classes directory), if it is available. Otherwise, returns {@code null}.
     *
     * @param originalClassPathEntry the original classpath entry
     * @return the transformed JAR for the entry or {@code null} if there is none
     */
    @Nullable
    public File findTransformedJarFor(File originalClassPathEntry) {
        return transforms.get(originalClassPathEntry);
    }

    @Override
    public int hashCode() {
        return originalClassPath.hashCode() + transforms.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        TransformedClassPath other = (TransformedClassPath) obj;
        return originalClassPath.equals(other.originalClassPath) && transforms.equals(other.transforms);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("[");
        boolean first = true;
        for (File original : originalClassPath.getAsFiles()) {
            if (!first) {
                builder.append(", ");
            }
            builder.append(original);
            File transformed = findTransformedJarFor(original);
            if (transformed != null) {
                builder.append("->").append(transformed);
            }
            first = false;
        }
        builder.append("]");
        return builder.toString();
    }

    /**
     * Creates a builder for the classpath with {@code size} original entries.
     *
     * @param size the number of the original entries in the classpath
     * @return the builder
     */
    public static Builder builderWithExactSize(int size) {
        return new Builder(size);
    }

    /**
     * Constructs a transformed classpath.
     */
    public static class Builder {
        private final DefaultClassPath.Builder originals;
        private final ImmutableMap.Builder<File, File> transforms;

        private Builder(int exactSize) {
            originals = DefaultClassPath.builderWithExactSize(exactSize);
            transforms = ImmutableMap.builderWithExpectedSize(exactSize);
        }

        /**
         * Adds the classpath entry with the corresponding transformed JAR.
         *
         * @param original the original JAR or classes directory
         * @param transformed the transformed JAR
         * @return this builder
         */
        public Builder add(File original, File transformed) {
            originals.add(original);
            if (!original.equals(transformed)) {
                transforms.put(original, transformed);
            }
            return this;
        }

        /**
         * Constructs the TransformedClassPath instance.
         *
         * @return the new classpath instance
         */
        public TransformedClassPath build() {
            Map<File, File> transformedMap = transforms.build();
            return new TransformedClassPath(originals.build(), transformedMap);
        }
    }
}
