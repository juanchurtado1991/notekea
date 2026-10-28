---
tags: [ghost-serializer, serialization, parser, capture, streaming]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/streaming/GhostJsonReaderCapture.kt
kind: functions
---

# GhostJsonReaderCapture

Raw JSON capture for the general reader: `captureRawJson` and `captureRawJsonBytes`, which work for both in-memory and streaming sources.

## What it is about

Capturing keeps the verbatim bytes of the next JSON value for later parsing, without building a value tree. For [[GhostJsonReader]] this is harder than for the flat reader, because on a stream the bytes may be discarded as the scan advances. This file solves that with pinning.

## How it works

After skipping whitespace the function remembers `start`. If the source is a [[StreamingGhostSource]], it calls `pin(start)` first, so the scan cannot trigger `releaseBefore` past the value's start. The scan itself is the shared `captureJsonValueScan`, reading units through `source.byteOrEof`. When it ends, the reader's token cache is reset and the result is created.

The result differs by source. For a stream, the bytes are copied one by one into an owned `ByteArray` with `getByte`, and wrapped with `RawJson.fromUtf8Bytes`; the data is guaranteed to be still available because of the pin. For an in-memory array, `RawJson.fromBufferSlice` references the existing buffer with an offset and a length and copies nothing. A `finally` block always unpins, so an error in the scan cannot leave the stream pinned forever.

## The code

```kotlin
fun GhostJsonReader.captureRawJson(): RawJson {
    // …
    val streaming = source as? StreamingGhostSource
    streaming?.pin(absoluteIndex = start)
    try {
        captureReaderValueBytes()
        // …
        return if (streaming != null) { /* … */ } else {
            RawJson.fromBufferSlice(buffer = rawData, offset = start, length = length)
        }
    } finally {
        streaming?.unpin()
    }
}
```

`captureRawJsonBytes` simply returns `captureRawJson().bytes`.

## Related

- [[GhostJsonReader]]: the reader extended.
- [[StreamingGhostSource]]: provides `pin` and `unpin`.
- [[GhostJsonValueCaptureScan]]: the shared scanner.
- [[RawJson]]: the value type.
- [[GhostJsonFlatReaderCapture]]: the byte-reader version.
- [[GhostJsonStringReaderCapture]]: the `String` reader version.
