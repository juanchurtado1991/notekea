---
tags: [ghost-serializer, ktor, constants, errors]
source: ghost-ktor/src/commonMain/kotlin/com/ghost/serialization/ktor/GhostKtorErrorMessages.kt
kind: object
---

# GhostKtorErrorMessages

An internal object holding the text of the errors raised by Ghost's Ktor bypass functions.

## What it is about

The extension functions `bodyGhost` and `respondGhost` (and their YAML and proto variants) throw when no serializer exists for the requested class. Instead of writing the message in each place, this object stores its pieces as constants. It sits in `ghost-ktor`.

## How it works

The messages are split into a prefix and a suffix, with the class name placed between them by the caller, for example `"Ghost serializer not found for class " + name + ". Make sure it is annotated with @GhostSerialization."`. A second suffix exists for the YAML case: `". Make sure a GhostYamlSerializer is registered for it."`.

Two details are worth knowing. First, the object is `internal` but also `@PublishedApi`. The extension functions that use it are `inline`, so their bodies are copied into the caller's module, and an inline public function can only reference internal declarations that are published like this. Second, centralising the strings keeps the project's rule against inline string literals in code and keeps client and server messages consistent.

## The code

```kotlin
@PublishedApi
internal object GhostKtorErrorMessages {
    const val SERIALIZER_NOT_FOUND_PREFIX = "Ghost serializer not found for class "
    const val SERIALIZER_NOT_FOUND_SUFFIX = ". Make sure it is annotated with @GhostSerialization."
    const val YAML_SERIALIZER_NOT_FOUND_SUFFIX = ". Make sure a GhostYamlSerializer is registered for it."
}
```

## Related

- [[GhostClientExtensions]]: uses the JSON message in `bodyGhost`.
- [[GhostServerExtensions]]: uses both suffixes in `respondGhost` and `respondGhostYaml`.
- [[GhostKtorMediaTypes]]: another published-API constants object of this module.
- [[GhostSerialization]]: the annotation named in the message.
