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

# Before we start, we wait 20 seconds to ensure that the workflow has been created
sleep 20

# Find the latest created "tests" workflow ID associated with the current branch
workflow_id=$(
  gh api "repos/$GITHUB_REPOSITORY/actions/runs?branch=$current_branch" \
    | jq -r '.workflow_runs | map(select(.path == ".github/workflows/subprojects-test.yml")) | max_by(.created_at) | .id'
)
workflow_url="$GITHUB_SERVER_URL/$GITHUB_REPOSITORY/actions/runs/$workflow_id"

echo "🚗 Waiting for all tests for $workflow_url to complete..."

# Afterwards, we poll the workflow's run status every minute
while true; do
  result=$(gh api "repos/$GITHUB_REPOSITORY/actions/runs/$workflow_id")

  if [ "$(echo "$result" | jq -r '.status')" = "completed" ]; then
    conclusion=$(echo "$result" | jq -r '.conclusion')

    # If it succeeded, then we safely exit
    if [ "$conclusion" = "success" ]; then
      echo "✅ All tests have passed!"
      exit 0
    # Otherwise, if it failed, then we need to terminate immediately
    else
      echo "🚫 One or more tests have failed!"
      exit 1
    fi
  fi

  sleep 60
done
