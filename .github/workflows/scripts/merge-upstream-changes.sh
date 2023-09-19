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

gradle_url=https://github.com/gradle/gradle

git checkout "$cradle_branch"

# Set remote upstream to Gradle repository
git remote add upstream "$gradle_url" || true

# Fetch the latest changes from upstream
git fetch upstream "$gradle_branch"

# Retrieve commits of Gradle that are ahead of the local branch
gradle_commits=$(git log HEAD..upstream/"$gradle_branch" --pretty=format:"%h %s")
gradle_commit_count=$(echo "$gradle_commits" | wc -l | tr -d ' ')

# Check if there are new commits
if [ -z "$gradle_commits" ]; then
  echo "🎉 Our branch is not behind the upstream!"
  exit 0
fi

echo "🔍 Found new commits upstream $gradle_commit_count"

# Format the new commits as footers for the merge commit message
footers=""
space_regex="[[:space:]]"
number_regex="#([0-9]+)($space_regex|$)"

while read -r hash subject; do
  # Replace "Merge pull request #<number>" with GitHub PR link
  subject=$(echo "$subject" | sed -E "s@Merge pull request $number_regex@$gradle_url/pull/\1\2@g")
  # Replace other "#<number>" with GitHub issue link
  subject=$(echo "$subject" | sed -E "s@(^|$space_regex)$number_regex@\1$gradle_url/issues/\2\3@g")

  # Append the commit to the footers
  footers+=$'\n'"$hash: $subject"
done < <(echo "$gradle_commits")

message="chore: merge upstream branch \`gradle/$gradle_branch\` into \`$cradle_branch\`

This commit synchronizes the latest $gradle_commit_count changes from the $gradle_url/tree/$gradle_branch. The changes include:
$footers"

# Merge upstream branch into local branch
git merge upstream/"$gradle_branch" --no-edit -m "$message" || true

# Resolve merge conflicts if any
conflict_directories=(".github" ".idea")
conflict_files=$(git diff --name-only --diff-filter=U "${conflict_directories[@]}")

if [ -n "$conflict_files" ]; then
  echo "🔧 Resolving merge conflicts for '${conflict_files[*]}'"

  # Iterate over each conflict file
  for file in $conflict_files; do
    # Check if the file status contains "deleted by us:"
    if [[ $(git status "$file") == *"deleted by us:"* ]]; then
      # In this case, we just need to delete it, following the Cradle
      git rm "$file"
    fi
  done

  # Continue the merge (https://stackoverflow.com/a/56814897/22202978)
  git -c core.editor=/bin/true merge --continue
fi

# Push the changes to the remote branch
git push origin --verbose "$cradle_branch"
