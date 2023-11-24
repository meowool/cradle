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

package org.gradle.internal.execution.steps;

import com.google.common.collect.ImmutableSortedMap;
import org.gradle.caching.internal.origin.OriginMetadata;
import org.gradle.internal.execution.ExecutionEngine.Execution;
import org.gradle.internal.execution.UnitOfWork;
import org.gradle.internal.execution.WorkInputListeners;
import org.gradle.internal.execution.history.impl.DefaultExecutionOutputState;
import org.gradle.internal.fingerprint.CurrentFileCollectionFingerprint;
import org.gradle.internal.fingerprint.FileCollectionFingerprint;
import org.gradle.internal.id.UniqueId;
import org.gradle.internal.snapshot.ValueSnapshot;

import java.time.Duration;

import static org.gradle.internal.execution.ExecutionEngine.ExecutionOutcome.SHORT_CIRCUITED;

public class SkipEmptyNonIncrementalWorkStep extends AbstractSkipEmptyWorkStep<PreviousExecutionContext> {
    private final UniqueId buildInvocationScopeId;

    public SkipEmptyNonIncrementalWorkStep(
        UniqueId buildInvocationScopeId,
        WorkInputListeners workInputListeners,
        Step<? super PreviousExecutionContext, ? extends CachingResult> delegate
    ) {
        super(workInputListeners, delegate);
        this.buildInvocationScopeId = buildInvocationScopeId;
    }

    @Override
    protected PreviousExecutionContext recreateContextWithNewInputFiles(PreviousExecutionContext context, ImmutableSortedMap<String, CurrentFileCollectionFingerprint> inputFiles) {
        return new PreviousExecutionContext(context) {
            @Override
            public ImmutableSortedMap<String, CurrentFileCollectionFingerprint> getInputFileProperties() {
                return inputFiles;
            }
        };
    }

    @Override
    protected ImmutableSortedMap<String, ValueSnapshot> getKnownInputProperties(PreviousExecutionContext context) {
        return ImmutableSortedMap.of();
    }

    @Override
    protected ImmutableSortedMap<String, ? extends FileCollectionFingerprint> getKnownInputFileProperties(PreviousExecutionContext context) {
        return ImmutableSortedMap.of();
    }

    @Override
    protected CachingResult performSkip(UnitOfWork work, PreviousExecutionContext context) {
        OriginMetadata originMetadata = new OriginMetadata(buildInvocationScopeId.asString(), Duration.ZERO);
        DefaultExecutionOutputState emptyOutputState = new DefaultExecutionOutputState(true, ImmutableSortedMap.of(), originMetadata, false);
        return CachingResult.shortcutResult(Duration.ZERO, Execution.skipped(SHORT_CIRCUITED, work), emptyOutputState, null, null);
    }
}
