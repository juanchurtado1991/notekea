---
tags: [ghost-serializer, serialization, parser, capture, raw-json]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/bytes/extensions/GhostJsonFlatReaderCapture.kt
kind: functions
---

# GhostJsonFlatReaderCapture

Extension functions that capture the next complete JSON value of a byte reader as raw UTF-8 bytes, without decoding it.

## What it is about

Sometimes a serializer wants to keep a JSON value for later: an envelope payload whose type is not yet known, or a field typed as `RawJson`. Capturing means remembering the bytes of that value and moving on. This file does it for [[GhostJsonFlatReader]]: `captureRawJson` returns a `RawJson`, and `captureRawJsonBytes` returns just the byte array.

## How it works

The reader first skips whitespace and notes the start. A pure byte-level scan (`captureJsonValueScan`, shared across channels) then finds where the value ends, including nested objects and arrays and strings with escapes. It does not touch the depth, comma masks or path state, which is the difference from `skipValue`; the scan reports errors through a callback that sets the position and throws.

Afterwards the result is built one of two ways. If `materializeRawJsonCaptures` is on, the bytes are copied into an owned array (needed when the reader's buffer is temporary, as in the `String` bridge). Otherwise `RawJson.fromBufferSlice` records the buffer, offset and length with no copy at all. The cached token is then reset so reading continues after the value.

## The code

```kotlin
fun GhostJsonFlatReader.captureRawJson(): RawJson {
    skipWhitespace()
    val start = position
    captureJsonValueBytes()
    nextTokenByte = SCN.RESET_TOKEN_BYTE
    return if (materializeRawJsonCaptures) {
        RawJson.fromUtf8Bytes(
            // …
        )
    } else {
        // …
    }
}
```

The private scanner adapts the shared routine to byte access:

```kotlin
position = captureJsonValueScan(
    startPosition = position,
    limit = limit,
    getUnit = { index -> data[index].toInt() and TOK.BYTE_MASK },
    onError = { message, errorPosition ->
        position = errorPosition
        throwError(message = message)
    }
)
```

## Related

- [[GhostJsonFlatReader]]: the reader being extended.
- [[GhostJsonValueCaptureScan]]: the shared scan that finds the end of the value.
- [[RawJson]]: the value type returned.
- [[GhostJsonReaderCapture]]: the same feature for the streaming reader.
- [[GhostJsonStringReaderCapture]]: the same feature for the `String` reader.
