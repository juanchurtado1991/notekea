---
tags: [ghost-serialization, serialization, yaml, errors]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/exception/GhostYamlHintMessages.kt
kind: object
---

# GhostYamlHintMessages

The texts of the fix suggestions that Ghost appends to YAML errors.

## What it is about

When a YAML document does not match the expected shape, a bare message such as "Expected Int but found ..." tells what happened but not what to do. Ghost adds a short hint. This object stores the YAML-specific hint sentences, so they live apart from the logic that chooses them. It is the YAML twin of [[GhostJsonHintMessages]].

## How it works

Nothing runs here. `hintForYamlError` in [[GhostYamlException]] compares an error message with the known prefixes from [[GhostYamlErrorMessages]] and returns the matching constant. The finished exception then prints it on a new line after the message, as `Hint: ...`.

## The code

Six hints exist. Two mention reader options that the developer can change:

```kotlin
const val HINT_COERCE_BOOLEANS =
    "If the API sends string booleans, enable coerceBooleans on the reader options."
const val HINT_MAX_NESTING_DEPTH =
    "Reduce nesting, or raise maxDepth on the YAML reader if this document is intentionally deep."
```

The others cover a missing anchor (`HINT_ANCHOR_NOT_FOUND`: define `&name` before using `*name`), a wrong collection type (`HINT_EXPECTED_LIST`, `HINT_EXPECTED_MAP`) and a wrong scalar type (`HINT_EXPECTED_SCALAR`, which suggests `coerceStringsToNumbers`).

Because the hints are plain constants, they are easy to review and to translate or reword without touching the matching logic. Each one names a concrete setting or syntax rule, so that a developer reading a failed request can act on it right away instead of searching the documentation.

## Related

- [[GhostYamlException]]: picks and prints these hints.
- [[GhostYamlErrorMessages]]: the message prefixes that decide which hint applies.
- [[GhostJsonHintMessages]]: the hints of the JSON channel, which YAML tries first.
- [[GhostReaderOptions]]: where options such as `coerceBooleans` are set.
