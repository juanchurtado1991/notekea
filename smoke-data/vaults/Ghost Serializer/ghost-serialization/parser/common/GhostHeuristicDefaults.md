---
tags: [ghost-serialization, serialization, heuristics, constants]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostHeuristicDefaults.kt
kind: object
---

# GhostHeuristicDefaults

An internal object that holds the baseline numbers behind every platform's `GhostHeuristics`.

## What it is about

Ghost tunes performance and memory per platform through the `expect object` `GhostHeuristics`. Most platforms want the same value for most knobs, so those values live once, here, and each `actual` points at them. Native and Wasm use them as they are; JVM and Android name only the numbers they change.

## How it works

The object is pure constants, all `const val`, so they are inlined at compile time and there is nothing to initialize at runtime. Two helpers exist only to make sizes readable: `BYTES_PER_KIB = 1024` and `BYTES_PER_MIB = 1024 * BYTES_PER_KIB`. The tuned values are:

- `INITIAL_COLLECTION_CAPACITY = 10`: starting size of lists and maps built while deserializing.
- `MAX_STRING_POOL_LENGTH = 64`: longest string that is interned in the reuse pool.
- `MAX_COLLECTION_SIZE = 500_000`: the denial-of-service cap on items in one collection.
- `MAX_DISCRIMINATOR_PEEK_DISTANCE = 1024`: how many bytes to scan looking for a polymorphic type key.
- `MAX_WARM_WRITE_BUFFER_CAPACITY = 1 MiB` and `MAX_WARM_CHAR_WRITE_BUFFER_CAPACITY = 512 KiB`: the largest writer buffer kept for reuse after a reset.

## The code

```kotlin
internal object GhostHeuristicDefaults {
    const val BYTES_PER_KIB = 1024
    const val BYTES_PER_MIB = 1024 * BYTES_PER_KIB

    const val INITIAL_COLLECTION_CAPACITY = 10
    const val MAX_STRING_POOL_LENGTH = 64
    const val MAX_COLLECTION_SIZE = 500_000
    // …
    const val MAX_WARM_WRITE_BUFFER_CAPACITY = BYTES_PER_MIB
    const val MAX_WARM_CHAR_WRITE_BUFFER_CAPACITY = 512 * BYTES_PER_KIB
}
```

It is `internal`, so applications cannot change it; they can only override values where a platform exposes a hook (on the JVM, through system properties). The JVM actual reuses `BYTES_PER_KIB` and `BYTES_PER_MIB` to express its larger limits, for example 2 KiB for the peek distance and 8 MiB for the warm byte buffer.

## Related

- [[GhostHeuristics]]: the `expect object` whose actuals read these constants.
- [[GhostHeuristics.native]]: uses every default as is.
- [[GhostHeuristics.wasmJs]]: uses every numeric default as is.
- [[GhostHeuristics.android]]: overrides only the collection cap.
- [[GhostHeuristics.jvm]]: overrides the cap, peek distance and warm buffers, and reads properties.
