#!/bin/bash

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

cradle_version=$1

if ! [[ $(cat version.txt | tr -d '[:space:]') =~ ^([0-9]+\.[0-9]+)(\.([0-9]+))?$ ]]; then
  echo "::error::'version.txt' format is invalid."
  exit 1
fi

major_minor=${BASH_REMATCH[1]}
patch=${BASH_REMATCH[2]}

echo "Current version: $major_minor$patch"

# 1. If there is no patch version, we need to fill in 0
# 2. Append the cradle version to the end
#    7.5 -> 7.5.0.1
#    7.5.1 -> 7.5.1.1
new_version="$major_minor${patch:-".0"}.$cradle_version"

echo $new_version > version.txt
echo "BASE_VERSION=$new_version" >> "$GITHUB_ENV"

echo "🆕 New version bumped: $new_version"
