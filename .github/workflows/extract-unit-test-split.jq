.
 | map(select(.unitTests) | .name)
 | to_entries
 | group_by(.key % 5)
 | map({
     name: map(.value) | join(", "),
     tasks: map(.value + ":test") | join(" "),
 })
