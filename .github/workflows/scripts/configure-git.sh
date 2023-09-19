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

git config --local user.name "Meowool Robot"
git config --local user.email "meowool@proton.me"

# We can automatically resolve some conflicts because there are
# certain choices we can make without hesitation.
git config --local merge.ours.driver true
# All these files should be based on the Cradle, and in case
# of conflicts during merging, the Cradle files should be
# chosen without hesitation.
cat << EOF > .git/info/attributes
.idea/** merge=ours
.github/** merge=ours
.gitattributes merge=ours
.gitignore merge=ours
.editorconfig merge=ours
README.md merge=ours
EOF
