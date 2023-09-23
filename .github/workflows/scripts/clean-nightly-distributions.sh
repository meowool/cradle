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

releases_url="/repos/$GITHUB_REPOSITORY/releases"
# Set the number of months ago to check for outdated data
months_ago=$1
# Loop to retrieve each page of data
page=1
# Total of outdated data
total_count=0

# JQ filters for selecting outdated data
is_prerelease=".prerelease == true"
is_nightly="(.name | contains(\"Nightly\"))"
is_outdated="(.published_at | fromdateiso8601) < (now - $months_ago * 30 * 24 * 60 * 60)"

echo "🔍 Checking for outdated nightly distributions..."

while true; do
  # Retrieve the JSON data for the current page
  json=$(gh api "$releases_url?page=$page&per_page=100")

  # Break the loop if there are no more pages
  if [ "$(echo "$json" | jq length)" -eq 0 ]; then
    break
  fi

  # Extract the outdated data using jq
  outdated=$(echo "$json" | jq "[.[] | select($is_prerelease and $is_nightly and $is_outdated)]")
  total_count=$((total_count + $(echo "$outdated" | jq length)))

  # Iterate over each item in the outdated data
  echo "$outdated" | jq -r '.[] | "\(.id) \(.html_url) \(.tag_name)"' | while read -r id url tag; do
    echo "🗑️ Deleting $id: $url"
    # Delete the outdated release
    gh api --method DELETE "$releases_url/$id"
    # Delete corresponding tag
    gh api --method DELETE "/repos/$GITHUB_REPOSITORY/git/refs/tags/$tag"
  done

  # Increment the page number for the next iteration
  page=$((page + 1))
done


if [ "$total_count" -eq 0 ]; then
  echo "🎉 No outdated nightly distributions found."
else
  echo "♻️ Total '$total_count' outdated nightly distributions cleaned up."
fi
