#!/bin/bash
set -euo pipefail

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

# Retrieve commits of Cradle that are ahead of the upstream Gradle
cradle_commits=$(git log upstream/master..main --no-merges --reverse --pretty=format:"%H")

# Filter commits that have been cherry-picked
picked_commits=$(eval "git log $(echo "$cradle_commits" | sed 's/.*/--grep=\"Cherry-picked-from: &\"/' | tr '\n' ' ')")

# Iterate over each commit
while read -r commit_hash; do
  # Check if the commit has already been cherry-picked
  if [[ $picked_commits =~ Cherry-picked-from:\ $commit_hash ]]; then
    echo "⏭️ Commit $commit_hash already cherry-picked, skipping."
    continue
  fi

  # Extract the commit message
  commit_message=$(git show --no-patch --format=%B "$commit_hash")

  # Cherry pick the commit and keep the original commit message
  footer_origin="Cherry-picked-from: $commit_hash"
  footer_coauthor="Co-authored-by: $(git config user.name) <$(git config user.email)>"
  git cherry-pick "$commit_hash"
  git commit --amend --no-edit --message="$commit_message" --message="$footer_origin"$'\n'"$footer_coauthor"

  echo "🎉 Successfully cherry-picked commit $commit_hash."
done < <(echo "$cradle_commits")

# Push the changes to the remote branch
git push origin --verbose "$current_branch"
echo "🚀 Applied latest Cradle commits to '$current_branch'."
