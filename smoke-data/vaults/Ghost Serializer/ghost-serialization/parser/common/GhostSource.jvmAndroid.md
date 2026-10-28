---
tags: [ghost-serialization, serialization, parser, jvm, android]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/parser/common/GhostSource.jvmAndroid.kt
kind: expect/actual
---

# GhostSource.jvmAndroid

The JVM and Android `actual` of `createByteArraySource`, which returns a byte-array source that decodes plain-ASCII strings faster.

## What it is about

Readers get their input through the `GhostSource` interface. This file supplies the JVM and Android implementation of the factory, and defines the public class `JvmByteArraySource`, a subclass of `ByteArrayGhostSource`. The subclass has one purpose: when the scanner has already proved that a quoted string contains only 7-bit bytes, build the Java `String` without validating UTF-8.

## How it works

`JvmByteArraySource` overrides `decodeJsonStringRange(start, end, isKnown7BitContent)`:

- If `isKnown7BitContent` is `false`, it calls `decodeToString(start, end)`, the normal UTF-8 decode.
- If it is `true`, it calls the `String(bytes, offset, length, charset)` constructor with `StandardCharsets.ISO_8859_1`. ISO-8859-1 maps each byte to the char with the same value, which for bytes below 128 equals ASCII, so the result is correct and the JVM can copy the bytes with no validation (and may use a compact one-byte-per-char string representation).

The guarantee comes from the scan step: the packed result of `scanString` carries a "pure 7-bit" bit, and callers pass it down as `isKnown7BitContent`. The `actual` function then wraps the data with `JvmByteArraySource`.

## The code

```kotlin
override fun decodeJsonStringRange(
    start: Int,
    end: Int,
    isKnown7BitContent: Boolean
): String {
    if (!isKnown7BitContent) {
        return decodeToString(start = start, end = end)
    }
    return String(data, start, end - start, StandardCharsets.ISO_8859_1)
}
```

And the factory:

```kotlin
@InternalGhostApi
actual fun createByteArraySource(
    data: ByteArray
): GhostSource = JvmByteArraySource(data = data)
```

## Related

- [[GhostSource]]: the interface and `expect` factory, whose KDoc defines the 7-bit hint.
- [[ByteArrayGhostSource]]: the open base class this subclass extends.
- [[GhostSource.wasmJs]]: Wasm actual that returns the unspecialized source.
- [[GhostSource.apple]]: Apple actual that returns the unspecialized source.
- [[GhostSourceCommon]]: contains the scan kernels that produce the 7-bit flag.
- [[GhostJsonFlatReader]]: a reader that creates sources through this factory.
