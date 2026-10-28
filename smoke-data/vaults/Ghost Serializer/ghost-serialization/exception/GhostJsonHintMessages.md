---
tags: [ghost-serializer, serialization, exception, errors]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/exception/GhostJsonHintMessages.kt
kind: object
---

# GhostJsonHintMessages

A holder of constant, developer-facing hint strings for common JSON parse errors.

## What it is about

A bare "expected a number" error is of limited help. These hints say what to change: enable a reader option, use an annotation, or fix the payload. `GhostJsonException` appends one of them to its message when the error text matches a known prefix.

## How it works

Each constant is a single sentence tied to one error family, and many name concrete switches. For example the coercion hints refer to `coerceStringsToNumbers` and `coerceBooleans`; depth and size hints name `maxDepth` and `maxCollectionSize`; enum and discriminator hints mention `@GhostFallback`, `@GhostResilient` and `@GhostDiscriminator`; required field hints mention `@SerialName` and `@GhostName`. Protobuf hints cover base64 and integer range problems. There is no logic in this file.

## The code

```kotlin
const val HINT_COERCION_DISABLED =
    "Enable coerceStringsToNumbers: Ghost.deserialize(…) { it.coerceStringsToNumbers = true }."

const val HINT_DEPTH_EXCEEDED =
    "Reduce nesting, or raise maxDepth on the reader if this payload is intentionally deep."

const val HINT_LEADING_ZEROS =
    "JSON numbers cannot have leading zeros (e.g. 01). Send an unpadded number or a quoted string."
```

The object is public (not `internal`), so the YAML hint code can use it as well.

## Related

- [[GhostJsonException]]: selects and appends these hints
- [[GhostYamlHintMessages]]: YAML equivalent
- [[GhostJsonErrorMessages]]: the error prefixes the hints are keyed on
- [[GhostFallback]]: annotation named in the enum and discriminator hints
- [[GhostResilient]]: annotation named in the enum hint
