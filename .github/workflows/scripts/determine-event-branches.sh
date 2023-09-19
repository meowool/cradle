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

# If the workflow is triggered automatically at a scheduled time or manually triggered
# with the selected release type as "nightly", it means that we need to handle both the
# 'main' and 'release' branches simultaneously.
if [ "$RELEASE_TYPE" = "nightly" ] || [ "$GITHUB_EVENT_NAME" = "schedule" ]; then
  process_branches="main,release"

# If the release type is "bump_latest", we need to process the latest version (`v*.*.*.x` branch).
elif [ "$RELEASE_TYPE" = "bump_latest" ]; then
  process_branches=$(
    git ls-remote --refs --heads origin "v[0-9]*.x*" \
      | awk -F/ '{print $NF}' \
      | sed '/-/! s/$/@/' | sort -V | sed 's/@$//' \
      | tail -n 1
  )

# If the release type is "specific", we only need to process the specified branches.
elif [ "$RELEASE_TYPE" = "specific" ]; then
  process_branches=${SPECIFIC_BRANCH:-$GITHUB_REF_NAME}

# Otherwise, this is an unexpected release type.
else
  echo "⚠️ An error has occurred! release type: $RELEASE_TYPE, event name: $GITHUB_EVENT_NAME"
  exit 1
fi

# Convert to {"include":[{"branch":"foo"},{"branch":"bar"}]}
matrix=$(echo "$process_branches" | jq -c -R '[split(",")[] | select(length > 0) | {branch: .}] | {include: .}')
echo "matrix=$matrix" >> "$GITHUB_OUTPUT"
