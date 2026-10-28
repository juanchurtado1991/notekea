---
tags: [ghost-serializer, serialization, parser, capture, raw-json]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/strings/GhostJsonStringReaderCapture.kt
kind: functions
---

# GhostJsonStringReaderCapture

Raw JSON capture for the `String` reader: it finds the next complete value and returns it as UTF-8 bytes or a `RawJson`.

## What it is about

Capturing a value (without decoding it) is useful for deferred parsing of envelope payloads. The byte reader can often hand out a slice of its own buffer, but [[GhostJsonStringReader]] holds UTF-16 characters, so any captured value has to be encoded to UTF-8 to become a `RawJson`. This file defines both capture functions for that reader.

## How it works

`captureRawJsonBytes` skips whitespace, notes the start, and runs the shared scanner `captureJsonValueScan` over the char array (each unit read as `chars[index].code`). The scanner knows how to cross nested objects, arrays and strings. When it returns, the reader's token cache is reset and the char range is converted with `sliceUtf8Bytes`.

`sliceUtf8Bytes` is the clever part. If the reader has already built its UTF-8 view of the whole document (some bridges do), it copies a range of that cached array. Otherwise it encodes only the captured substring, so capturing a small value inside a large envelope never pays for a full-document encode. `captureRawJson` just wraps those bytes with `RawJson.fromUtf8Bytes`. Errors found by the scan set the position and go through `throwError`, so they carry the usual path and line information.

## The code

```kotlin
fun GhostJsonStringReader.captureRawJson(): RawJson =
    RawJson.fromUtf8Bytes(bytes = captureRawJsonBytes())

@OptIn(InternalGhostApi::class)
fun GhostJsonStringReader.captureRawJsonBytes(): ByteArray {
    skipWhitespace()
    val start = position
    captureStringReaderValueBytes()
    nextTokenByte = SCN.RESET_TOKEN_BYTE
    return sliceUtf8Bytes(charStart = start, charEnd = position)
}
```

The private scan adapter:

```kotlin
position = captureJsonValueScan(
    startPosition = position,
    limit = limit,
    getUnit = { index -> chars[index].code },
    onError = { message, errorPosition ->
        position = errorPosition
        throwError(message = message)
    }
)
```

## Related

- [[GhostJsonStringReader]]: the reader and its `sliceUtf8Bytes`.
- [[GhostJsonValueCaptureScan]]: the channel-independent scan.
- [[RawJson]]: the captured value type.
- [[GhostJsonFlatReaderCapture]]: the byte-reader version, which can slice without copying.
- [[GhostJsonReaderCapture]]: the streaming version.
