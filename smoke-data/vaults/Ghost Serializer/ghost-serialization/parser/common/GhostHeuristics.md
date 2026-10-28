---
tags: [ghost-serialization, serialization, heuristics, expect-actual]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostHeuristics.kt
kind: expect/actual
---

# GhostHeuristics

The `expect object` that declares the performance and safety knobs Ghost reads at runtime, implemented separately for each platform.

## What it is about

Different environments need different limits: Android has little memory, the JVM has plenty, browsers have a JavaScript engine whose speed profile varies. `GhostHeuristics` is the single place the parsers, writers and pools ask "how big may this be?" or "which path is faster here?". The common file only declares the seven properties; the numbers come from the platform `actual` files. It is marked `@InternalGhostApi`.

## How it works

Nothing runs here: it is a declaration with KDoc. Callers read the properties as plain `Int` or `Boolean` values, and each platform decides how they are initialized: constants for Native and Android, system properties on the JVM, a runtime engine probe for Wasm.

The seven properties are:

- `initialCollectionCapacity`: starting capacity for lists and maps while deserializing.
- `maxStringPoolLength`: longest string eligible for pooling.
- `maxCollectionSize`: a denial-of-service cap on collection items (Android 50k, Native and Wasm 500k, JVM 1M).
- `maxDiscriminatorPeekDistance`: how far to scan for a type discriminator.
- `maxWarmWriteBufferCapacity` and `maxWarmCharWriteBufferCapacity`: capacity above which a writer releases its buffer on reset instead of keeping it.
- `encodeToStringViaUtf8Bytes`: whether `Ghost.encodeToString` goes through the UTF-8 flat writer. Only Wasm can make it true, and only on JavaScriptCore.

## The code

```kotlin
@InternalGhostApi
expect object GhostHeuristics {
    val initialCollectionCapacity: Int
    val maxStringPoolLength: Int
    val maxCollectionSize: Int
    val maxDiscriminatorPeekDistance: Int
    val maxWarmWriteBufferCapacity: Int
    val maxWarmCharWriteBufferCapacity: Int
    val encodeToStringViaUtf8Bytes: Boolean
}
```

The file suppresses `EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING` because expect/actual objects are a beta feature. The KDoc also notes that registry discovery is empty on iOS and Wasm, which need manual registration.

## Related

- [[GhostHeuristicDefaults]]: the shared baseline constants.
- [[GhostHeuristics.jvm]]: JVM actual with property overrides.
- [[GhostHeuristics.android]]: Android actual with a tighter cap.
- [[GhostHeuristics.native]]: Native actual using defaults.
- [[GhostHeuristics.wasmJs]]: Wasm actual with a runtime engine choice.
- [[GhostDiscriminatorPeeker]]: reads `maxDiscriminatorPeekDistance`.
- [[FlatByteArrayWriter]]: uses the warm buffer cap.
