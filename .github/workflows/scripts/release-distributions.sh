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

github_url=https://github.com/meowool/cradle
dist_dir=subprojects/distributions-full/build/distributions

is_nightly=$( [[ "$current_branch" == "main" || "$current_branch" == "release" ]] && echo true || echo false )

# Parse build version
# gradle-7.5.1.1-milestone-1-bin.zip > 7.5.1.1-milestone-1
# gradle-8.5.0.1-20230909160000+0000-src.zip > 8.5.0.1-20230909160000+0000
raw_full_version=$(find $dist_dir -name '*.zip' | head -n 1 | xargs basename | sed 's/gradle-\(.*\)-[^.]*.zip/\1/')
# 7.5.1.1-milestone-1 > 7.5.1.1-M1
# 8.5.0.1-rc-1 > 8.5.0.1-RC1
full_version=$(echo "$raw_full_version" | sed 's/-milestone-/-M/g; s/-rc-/-RC/g')
tag="v$full_version"

# Rename the prefix of the distribution files
for file in "$dist_dir"/gradle-*; do
  mv "$file" "$dist_dir/cradle-$(basename "$file" | sed 's/^gradle-//')"
done

echo "🔄 Uploading distributions for '$full_version'"

# We don't need docs distribution
find $dist_dir -type f -name "*-docs.zip" -delete

# Write the release notes to a file
release_url=$github_url/releases/download/$tag
note_file=$dist_dir/NOTES.md
{
  if [[ "$is_nightly" == "true" ]]; then
    if [[ "$current_branch" == "release" ]]; then
      echo "## Release Nightly build for \`v$current_version\`"
    else
      echo "## Nightly build for \`v$current_version\`"
    fi
  else
    echo "## Release \`v$current_version\`"
  fi
  echo ""
  echo "#### Please refer to the [commit history]($github_url/commits/$tag) for a full list of changes."

  if [[ "$is_nightly" == "true" ]]; then
    echo ""
    echo "> [!WARNING]"
    echo "> This is an automatically generated nightly build of **Cradle**, which does not come with any guarantees of quality or stability. Please note that it may not be suitable for production use."
  fi

  echo ""
  echo "### Upgrade"
  echo ""
  echo "To quickly switch your build to **Cradle** \`$full_version\`, run the following command in your project's root directory:"
  echo '```'
  echo "./gradlew wrapper --gradle-distribution-url=$release_url/cradle-$raw_full_version-bin.zip"
  echo '```'
  echo ""
  echo " If you are unable to use the command above, you can manually update the \`distributionUrl\` property in the \`gradle/wrapper/gradle-wrapper.properties\` file. Simply replace it with the download link for this version."
  echo ""
  echo "> [!NOTE]"
  echo "> You have the flexibility to choose a zip file with either the \`-bin\` or \`-all\` suffix."
  echo ""
  echo "### Verification"
  echo ""
  echo "To verify the integrity of the downloaded zip file, you can compare its SHA-256 checksum with the one listed below. For more information on how to do this, please refer to the [Gradle documentation](https://docs.gradle.org/current/userguide/gradle_wrapper.html#sec:verification)."
  echo ""
  echo "<table>"
  echo "  <tr>"
  echo "    <th>Download</th>"
  echo "    <th>SHA-256 Checksum</th>"
  echo "  <tr>"
  for zip in "$dist_dir"/*.zip; do
    zip_name=$(basename "$zip")
    sha=$(shasum -a 256 "$zip" | awk '{print $1}')
    echo "  <tr>"
    echo "    <td><a href=\"$release_url/$zip_name\">$zip_name<a></td>"
    echo "    <td><code>$sha</code></td>"
    echo "  </tr>"
    echo "$sha" > "$dist_dir/$zip_name.sha256"
  done
  echo "</table>"
  echo ""
  echo "Alternatively, if you prefer, you can download the corresponding \`*.sha256\` file and manually copy the checksum from there."
} > $note_file

echo "::group::Release notes"; cat $note_file; echo "::endgroup::"

# Create a release and upload all these
files_to_upload=$(find $dist_dir -type f \( -name "*.zip" -o -name "*.sha256" \) | tr '\n' ' ')
branch="main"
title="$full_version"

if [[ "$is_nightly" == "true" ]]; then
  title+=" (Nightly)"
fi

echo "🚀 Creating release '$title' on branch '$branch' with files '$files_to_upload'"
echo "========================================"

# shellcheck disable=SC2086
gh release create $tag \
  --title "$title" \
  --notes-file "$note_file" \
  --target "$branch" \
  --prerelease=$is_nightly \
  $files_to_upload
