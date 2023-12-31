#!/bin/bash
set -euo pipefail

#
# Copyright 2023 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Check if the branch name contains "-RC<number>"
if [[ $current_branch == *"-RC"* ]]; then
  version_property="-PrcNumber=${current_branch#*-RC}"
# Check if the branch name contains "-M<number>"
elif [[ $current_branch == *"-M"* ]]; then
  version_property="-PmilestoneNumber=${current_branch#*-M}"
# Check if the branch name is "main" or "release"
elif [[ $current_branch == "main" || $current_branch == "release" ]]; then
  # In this case, we don't need to specify any properties, which
  # means we will build a nightly version.
  version_property=""
else
  version_property="-PfinalRelease=true"
fi

# Start the build
./gradlew clean \
  :distributions-full:buildDists :distributions-integ-tests:forkingIntegTest \
  -Ddistribution-full-name=true -PmaxParallelForks=3 $version_property \
  --exclude-task docs
