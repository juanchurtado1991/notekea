---
tags: [ghost-serialization, serialization, heuristics, native]
source: ghost-serialization/src/nativeMain/kotlin/com/ghost/serialization/parser/common/GhostHeuristics.native.kt
kind: expect/actual
---

# GhostHeuristics.native

The Kotlin/Native `actual` of `GhostHeuristics`: every tuning value is the shared default.

## What it is about

Ghost lets each platform choose its own limits for collections, string pooling and warm buffers through one `expect object`, `GhostHeuristics`. Native needs no special tuning, so this file only wires the `actual` properties to [[GhostHeuristicDefaults]]. It exists because Kotlin requires every target to supply an `actual` for the `expect` object, even when nothing differs.

## How it works

There is no logic at all. Each property is initialized once, at object creation, with a constant:

- the initial capacity of lists and maps built while deserializing,
- the longest string eligible for the reuse pool,
- the maximum number of items in one collection (the denial-of-service cap, 500,000 here),
- how far a discriminator may be peeked before giving up,
- the retained capacity of the byte and char write buffers.

The only literal in the file is `encodeToStringViaUtf8Bytes = false`: Native always uses the char-based string path for `encodeToString`, because the JavaScriptCore problem that motivates the UTF-8 route exists only on Wasm.

## The code

```kotlin
actual object GhostHeuristics {
    actual val initialCollectionCapacity: Int = GhostHeuristicDefaults.INITIAL_COLLECTION_CAPACITY
    actual val maxStringPoolLength: Int = GhostHeuristicDefaults.MAX_STRING_POOL_LENGTH
    actual val maxCollectionSize: Int = GhostHeuristicDefaults.MAX_COLLECTION_SIZE
    // …
    actual val encodeToStringViaUtf8Bytes: Boolean = false
}
```

Compared with the siblings: [[GhostHeuristics.android]] lowers the collection cap to 50,000, [[GhostHeuristics.jvm]] raises several caps and lets system properties override them, and [[GhostHeuristics.wasmJs]] decides the UTF-8 flag at runtime. Native is the plain baseline.

The file-level `@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")` silences the compiler warning about `expect`/`actual` objects being beta.

## Related

- [[GhostHeuristics]]: the `expect object` this file implements.
- [[GhostHeuristicDefaults]]: the constants every value is read from.
- [[GhostHeuristics.android]]: Android actual, same shape with a tighter collection cap.
- [[GhostHeuristics.jvm]]: JVM actual with larger limits and property overrides.
- [[GhostHeuristics.wasmJs]]: Wasm actual with a runtime-selected encode path.
