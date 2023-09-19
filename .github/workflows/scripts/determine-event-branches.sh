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

continue=true

case "$GITHUB_EVENT_NAME" in
  create)
    # Since the create event does not support branch or tag filters in Github workflows,
    # we need to further filter it here:
    #
    # 1. If a tag is created, the tag name must follow the format `cradle-*`
    if [[ $GITHUB_REF =~ refs/tags/cradle-.* ]]; then
      # This means a new release of Cradle is ready.
      # In this case, we need to process the latest version (`v*.*.*.x` branch).
      process_branches=$(
        git ls-remote --refs --heads origin "v[0-9]*.x*" \
          | awk -F/ '{print $NF}' \
          | sed '/-/! s/$/@/' | sort -V | sed 's/@$//' \
          | tail -n 1
      )
    # 2. If a branch is created, the branch name must start with `v*.*.*.x` format
    elif [[ $GITHUB_REF =~ refs/heads/v[0-9]+\.[0-9]+\.[0-9]+\.x ]]; then
      # This means a new version has been released on the upstream Gradle.
      # In this case, we need to process this newly branch.
      process_branches=$GITHUB_REF_NAME
    else
      continue=false
      echo "👋 Ignoring create event for $GITHUB_REF"
    fi
    ;;
  schedule | workflow_dispatch)
    # In general, we only need to process `main` and `release` branches when the workflow
    # is manually or automatically triggered.
    process_branches="main,release"
    ;;
  *)
    # Unexpected event
    continue=false
    echo "⚠️ Unexpected event name: $GITHUB_EVENT_NAME"
    exit 1
    ;;
esac

# Convert to {"include":[{"branch":"foo"},{"branch":"bar"}]}
matrix=$(echo "$process_branches" | jq -c -R '[split(",")[] | select(length > 0) | {branch: .}] | {include: .}')
echo "matrix=$matrix" >> "$GITHUB_OUTPUT"
echo "continue=$continue" >> "$GITHUB_OUTPUT"
