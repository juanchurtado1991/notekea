---
tags: [ghost-serializer, serialization, platform, jvm, android, utf8]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/GhostUtf8String.jvmAndroid.kt
kind: expect/actual
---

# GhostUtf8String.jvmAndroid

The JVM and Android actual of `ghostUtf8BytesToString`.

## What it is about

Ghost occasionally needs a `String` from a UTF-8 byte range, for instance when it encodes to bytes and then converts. On the JVM and Android the standard decoder is fast, so this actual just calls it.

## How it works

It forwards to `ByteArray.decodeToString` with `startIndex = offset` and `endIndex = offset + length`. There are no caches or buffers; only the result string is allocated.

Having this as an actual rather than calling `decodeToString` directly from `Ghost` keeps the platform decision in one place, so the Wasm exception does not leak into common code. The `@InternalGhostApi` marker signals that the function is not part of the supported public surface.

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
- [[GhostUtf8String.native]]: native actual with the same body
- [[GhostUtf8String.wasmJs]]: Wasm actual that uses the browser `TextDecoder`
