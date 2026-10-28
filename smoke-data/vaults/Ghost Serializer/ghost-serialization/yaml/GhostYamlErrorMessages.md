---
tags: [ghost-serialization, serialization, yaml, errors]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/GhostYamlErrorMessages.kt
kind: object
---

# GhostYamlErrorMessages

The central list of message texts (and message fragments) for YAML parse, write and lookup failures.

## What it is about

Instead of writing error strings at each throw site, Ghost keeps them here, as `const val`s of an internal `@PublishedApi` object. The reader, the writer and the serializer lookup all import it, usually under the alias `EM`. Centralizing the text also lets [[GhostYamlException]] recognize a message by its prefix and attach the right hint.

## How it works

Messages are split into pieces when they contain runtime data. A `*_PREFIX` plus a `*_SUFFIX` surround a value that the caller inserts, such as `"Maximum nesting depth ("` + depth + `") exceeded"`, or `"Anchor '"` + name + `"' not found"`. The shared position suffix is built the same way: ` (position=` + number + `)`.

The names tell the group they belong to:

- **Type mismatches:** `ERR_EXPECTED_MAP_PREFIX`, `..._INT_...`, `..._LIST_...` and so on, all of the form "Expected X but found ".
- **Block structure:** tabs in indentation, a sequence on the key line, an inline nested mapping, multiline implicit keys.
- **Flow style:** empty entries, missing `,`, `]`, `}` or `:`.
- **Quoted scalars and escapes:** unterminated strings, incomplete `\x`, `\u`, `\U` escapes, unknown escapes.
- **Block scalars, tags, anchors, directives and document markers.**
- **Writer and lookup:** depth exceeded, name outside an object, serializer not found or not a YAML serializer.

## The code

Two typical entries, one plain and one split in parts:

```kotlin
const val ERR_ANCHOR_NOT_FOUND_PREFIX = "Anchor '"
const val ERR_ANCHOR_NOT_FOUND_SUFFIX = "' not found"
```

Prefixes double as keys for hint lookup:

```kotlin
const val ERR_EXPECTED_BOOLEAN_PREFIX = "Expected Boolean but found "
```

`hintForYamlError` tests messages with `startsWith` against such prefixes. `STR_UNKNOWN_TYPE` supplies the word "unknown" when a type name is unavailable.

## Related

- [[GhostYamlException]]: matches these prefixes to choose a hint.
- [[GhostYamlErrorReporting]]: appends the position suffix to messages.
- [[GhostYamlHintMessages]]: the hints chosen for these messages.
- [[GhostYamlAnchorSubsystem]]: reports anchor errors.
- [[GhostYamlQuotedScalarDecoding]]: reports escape and quote errors.
- [[GhostYamlFlowStyleSubsystem]]: reports flow collection errors.
- [[GhostYamlSerializer]]: the interface whose absence has a message here.
