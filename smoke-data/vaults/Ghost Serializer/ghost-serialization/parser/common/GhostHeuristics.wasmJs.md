---
tags: [ghost-serialization, serialization, heuristics, wasm]
source: ghost-serialization/src/wasmJsMain/kotlin/com/ghost/serialization/parser/common/GhostHeuristics.wasmJs.kt
kind: expect/actual
---

# GhostHeuristics.wasmJs

The Kotlin/Wasm-JS `actual` of `GhostHeuristics`: the tuning knobs for Ghost on browsers, with one value that is decided at runtime.

## What it is about

`GhostHeuristics` is an `expect object` in common code that exposes the size limits and buffer caps the parsers and writers consult. This file supplies the Wasm values. Almost everything is taken unchanged from [[GhostHeuristicDefaults]], so Wasm behaves like Native; the one real difference is `encodeToStringViaUtf8Bytes`, which depends on which JavaScript engine the page is running on.

## How it works

Six properties (`initialCollectionCapacity`, `maxStringPoolLength`, `maxCollectionSize`, `maxDiscriminatorPeekDistance`, `maxWarmWriteBufferCapacity`, `maxWarmCharWriteBufferCapacity`) simply alias the matching constants in the defaults object.

The seventh, `encodeToStringViaUtf8Bytes`, is backed by a private `utf8EncodePreference` that is computed once, when the object is first touched, by calling `ghostJsEnginePrefersUtf8EncodeToString()` (see [[JsEngineDetect.wasmJs]]). The trade-off is explained in the comment: on JavaScriptCore (Safari and every iOS browser) the char-based string writer is slow, so Ghost goes through the UTF-8 flat writer and a text decoder; on V8 (Chrome) the string writer `GhostJsonStringWriter` stays faster. The getter returns the cached boolean, so there is no per-call probing.

## The code

```kotlin
actual val encodeToStringViaUtf8Bytes: Boolean
    get() = utf8EncodePreference

private val utf8EncodePreference: Boolean =
    ghostJsEnginePrefersUtf8EncodeToString()
```

The getter-over-private-val shape is used so the probe runs once at initialization but the public declaration still matches the `expect` signature of a plain `val`.

The other values follow the pattern:

```kotlin
actual val maxCollectionSize: Int = GhostHeuristicDefaults.MAX_COLLECTION_SIZE
```

The file also suppresses the `EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING`, because `expect object` / `actual object` is still a beta feature in Kotlin.

## Related

- [[GhostHeuristics]]: the `expect` declaration this file implements.
- [[GhostHeuristicDefaults]]: provides all the baseline values reused here.
- [[JsEngineDetect.wasmJs]]: the runtime probe behind `encodeToStringViaUtf8Bytes`.
- [[GhostHeuristics.native]]: the sibling actual that also uses the defaults but hard-codes the UTF-8 flag to false.
- [[GhostHeuristics.jvm]]: the JVM actual, which overrides several values and reads system properties.
- [[GhostJsonStringWriter]]: the char-based writer that V8 keeps using.
