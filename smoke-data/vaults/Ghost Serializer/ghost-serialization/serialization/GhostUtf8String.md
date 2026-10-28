---
tags: [ghost-serializer, serialization, utf8, platform]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/GhostUtf8String.kt
kind: expect/actual
---

# GhostUtf8String

The `expect` declaration of `ghostUtf8BytesToString`, which converts a UTF-8 byte range into a `String`.

## What it is about

Ghost has a case where it holds encoded bytes and needs a string (for example when `Ghost.encodeToString` goes through bytes). The cheapest way to do that differs by platform, so the function is declared in common code and implemented per target.

## How it works

Common code only declares the signature. On the JVM, Android and Native the actual calls `decodeToString`. On Wasm it uses the browser's `TextDecoder`, because Kotlin/Wasm's own decoder was far slower on payloads of about 20 KB (the source refers to issue #16). The caller supplies `offset` and `length`, so no sub-array copy is needed.

The signature takes an array, an offset and a length rather than a sub-array, so no copy is made before the decode. The function is `internal`, which keeps the platform detail private to the module.

## The code

```kotlin
@InternalGhostApi
internal expect fun ghostUtf8BytesToString(
    bytes: ByteArray,
    offset: Int,
    length: Int
): String
```

## Related

- [[GhostUtf8String.jvmAndroid]]: JVM and Android actual
- [[GhostUtf8String.native]]: Native actual
- [[GhostUtf8String.wasmJs]]: Wasm actual using `TextDecoder`
- [[Ghost]]: calls it from `encodeToString` when the heuristic asks for the bytes path
