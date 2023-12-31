/*
 * Copyright 2009 the original author or authors.
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

package org.gradle.launcher.daemon.client;

import org.gradle.internal.deprecation.Documentation;
import org.gradle.internal.exceptions.DefaultMultiCauseException;
import org.gradle.internal.exceptions.ResolutionProvider;

import java.util.Collections;
import java.util.List;

public class NoUsableDaemonFoundException extends DefaultMultiCauseException implements ResolutionProvider {

    private static final List<String> RESOLUTION = Collections.singletonList(Documentation.userManual("troubleshooting", "network_connection").getConsultDocumentationMessage());

    public NoUsableDaemonFoundException(String message, Iterable<? extends Throwable> causes) {
        super(message, causes);
    }

    @Override
    public List<String> getResolutions() {
        return RESOLUTION;
    }
}
