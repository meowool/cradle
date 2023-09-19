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

minimum_version="v8.3.0"

# Set remote upstream to Gradle repository
git remote add upstream https://github.com/gradle/gradle || true

# Retrieve local branches
local_branches=$(git ls-remote --heads --refs origin | awk -F/ '{print $NF}')

# Retrieve upstream tags greater than minimum version
upstream_tags=$(git ls-remote --tags --refs upstream 'v*' | awk -F/ '{print $NF}' | sed '/-/! s/$/@/' | sort -Vr | sed 's/@$//')

needs_checkout=false

# Iterate over each upstream tag
for tag in $upstream_tags; do
  corresponding_branch=$(echo "$tag" | sed -E 's/(v)([0-9]+\.[0-9]+\.[0-9]+)(.*)/\1\2.x\3/')

  # Check if the branch is already checked out
  if [[ ! $local_branches =~ $corresponding_branch ]]; then
    needs_checkout=true

    echo "🔍 Checking out $tag..."
    git fetch upstream --no-tags "refs/tags/$tag:refs/remotes/upstream/$tag"
    git checkout --no-track -b "$corresponding_branch" "upstream/$tag"

    echo "🚀 Pushing $corresponding_branch to origin..."
    git push origin --verbose "$corresponding_branch"
  fi

  # Stop checking out tags if minimum version is reached
  if [ "$tag" == "$minimum_version" ]; then
    break
  fi
done

# Check if there are new versions
if [ "$needs_checkout" = false ]; then
  echo "🎉 We already have the latest version!"
fi
