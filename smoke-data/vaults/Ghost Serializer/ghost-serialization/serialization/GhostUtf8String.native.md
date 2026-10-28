---
tags: [ghost-serializer, serialization, platform, native, utf8]
source: ghost-serialization/src/nativeMain/kotlin/com/ghost/serialization/GhostUtf8String.native.kt
kind: expect/actual
---

# GhostUtf8String.native

The Kotlin/Native actual of `ghostUtf8BytesToString`.

## What it is about

Ghost sometimes needs to turn encoded UTF-8 bytes into a `String`. On Kotlin/Native the standard library decoder is fast enough, so this file simply delegates to it. The special-case decoder exists only for Wasm.

## How it works

The function calls `ByteArray.decodeToString` with the start and end indices computed from `offset` and `length`. No buffers are cached and nothing is allocated beyond the result string. It is marked `@InternalGhostApi` like the other actuals.

The design choice here is what is absent: Native does not need the cached typed array that Wasm uses, so the call does no pooling and has no shared state. It can be called from any thread without synchronization.

## The code

```kotlin
@InternalGhostApi
internal actual fun ghostUtf8BytesToString(
    bytes: ByteArray,
    offset: Int,
    length: Int
): String = bytes.decodeToString(
    startIndex = offset,
    endIndex = offset + length
)
```

## Related

- [[GhostUtf8String]]: declares the `expect` function
- [[GhostUtf8String.wasmJs]]: Wasm actual that uses `TextDecoder` instead
- [[GhostUtf8String.jvmAndroid]]: JVM/Android actual with the same body
- [[InternalGhostApi]]: the opt-in marker on the function
