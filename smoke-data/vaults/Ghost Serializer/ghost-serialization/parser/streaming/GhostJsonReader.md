---
tags: [ghost-serializer, serialization, parser, json, streaming]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/streaming/GhostJsonReader.kt
kind: class
---

# GhostJsonReader

The general JSON reader: it works over any `GhostSource`, which means either a byte array or an Okio stream.

## What it is about

Generated serializers need a reader, and Ghost has one per input type. [[GhostJsonFlatReader]] is the fastest for a `ByteArray` and [[GhostJsonStringReader]] handles a `String`. This reader is the flexible one: it takes a `GhostSource`, including [[StreamingGhostSource]], so JSON can be parsed from a file or network without loading it all. For in-memory arrays it keeps a "fast path" that reads `rawData` directly.

## How it works

The primary constructor takes a source and a `limit`. Two convenience constructors build one from a `ByteArray` (via `createByteArraySource`, limit = size) or from an Okio `BufferedSource` (via `createSourceBridge`, limit = `Int.MAX_VALUE`, because a stream's length is unknown).

The `isStreaming` flag decides how bytes are read: `getByte` reads `rawData` directly for arrays and goes through the source for streams. Numbers use `numberByteAt`, which for streams returns a NUL byte at end of input so a number ends cleanly instead of throwing.

Streaming adds housekeeping. `skipWhitespace` calls `releaseStreamingPrefix`, which tells the source it may discard bytes behind the cursor. `throwError` clamps the reported position to bytes actually received. It also carries the machinery the other readers have: a string pool keyed by a rolling hash from the scan, `predictedFieldIndex`, comma masks, and a `pathTracker`. Unlike the flat reader, which rebuilds the path by re-scanning, this one updates the tracker as it reads (`pushKey`, `enterArrayElement`).

`reset` rebinds the reader to new data and restores every option to its default so a pooled instance can be reused.

## The code

The two constructor forms for streaming input:

```kotlin
constructor(
    okioSource: BufferedSource,
    // …
) : this(
    createSourceBridge(source = okioSource),
    Int.MAX_VALUE, // Limit is unknown for streaming
    // …
)
```

Streaming-aware byte access:

```kotlin
internal inline fun numberByteAt(index: Int): Int {
    if (!isStreaming) return rawData[index].toInt() and TOK.BYTE_MASK
    val byte = source.byteOrEof(index = index)
    return if (byte == SCN.MATCH_END) TOK.NUL_BYTE else byte
}
```

`peekDiscriminator` hands its work to `GhostDiscriminatorPeeker`, which looks ahead for the polymorphic type key without moving the cursor.

## Related

- [[GhostSource]]: the abstraction the reader sits on.
- [[StreamingGhostSource]]: the streaming implementation.
- [[ByteArrayGhostSource]]: the in-memory implementation.
- [[GhostJsonReaderSubsystem]]: structure, strings and field selection.
- [[GhostJsonReaderNumbers]]: number reading.
- [[GhostJsonReaderCapture]]: raw JSON capture.
- [[GhostJsonPathTracker]]: the JSONPath breadcrumbs.
- [[GhostDiscriminatorPeeker]]: used by `peekDiscriminator`.
