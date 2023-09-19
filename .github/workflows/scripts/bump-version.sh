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

# Read the version number from the file
version=$(<version.txt tr -d '[:space:]')

echo "🌀 Current version: $version"

# Count the number of dots in the version number
dot_count=$(grep -o "\." <<< "$version" | wc -l | tr -d ' ')

# Perform the appropriate transformation based on dot count
case $dot_count in
  # In the case of "<major>.<minor>", pad <patch> to 0 and initialize <cradle> to 1
  1) version="$version.0.1" ;;
  # In the case of "<major>.<minor>.<patch>", initialize <cradle> to 1
  2) version="$version.1" ;;
  # In the case of "<major>.<minor>.<patch>.<cradle>", just increment <cradle>
  3)
    # Split the version number into components using dot as the delimiter
    IFS='.' read -r major minor patch cradle <<< "$version"
    # Increment the cradle version
    ((cradle++))
    version="${major}.${minor}.${patch}.${cradle}"
    ;;
  # Otherwise, the version number is invalid
  *)
    echo "::error::'version.txt' format is invalid."
    exit 1
    ;;
esac

# Write the new version number to the file
echo "$version" > version.txt
echo "current_version=$version" >> "$GITHUB_ENV"
echo "🆕 New version bumped: $version"
