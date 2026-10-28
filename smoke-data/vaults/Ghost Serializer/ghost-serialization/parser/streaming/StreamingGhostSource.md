---
tags: [ghost-serializer, serialization, parser, streaming, okio]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/streaming/StreamingGhostSource.kt
kind: class
---

# StreamingGhostSource

A `GhostSource` that reads from an Okio `BufferedSource`, so Ghost can parse a JSON document of unknown size without holding all of it in memory.

## What it is about

The in-memory source knows its length and can index its array freely. A network response or file stream does not: bytes arrive on demand and the total length is unknown. This class gives the streaming reader ([[GhostJsonReader]]) the same index-based view while discarding bytes that are already behind the cursor.

## How it works

Indices are absolute positions in the stream. `discarded` is the absolute index of the first byte still held by Okio, so anything before it is gone. Reads go through a cached window, `bufferBytes`, of `STREAMING_BUFFER_SIZE` bytes aligned to that size; `bufferStart` and `bufferEnd` say which absolute range it holds. A hit in the window is one array read. A miss calls `getSlow`, which asks Okio to `request` enough bytes, copies the aligned window into `bufferBytes` (looping, because a single Okio `read` can stop at a segment boundary and leave stale data), and returns the byte. Reading past the end throws a `GhostJsonException` ("Unexpected end of input"), like the in-memory sources.

`releaseBefore` is what keeps memory bounded: the reader calls it as it advances, and the source skips whole windows behind it. It never discards past an active pin. `pin` and `unpin` form a nested stack that protects ranges which may be re-read, such as a resilient decode that rolls back, or a raw JSON capture that copies the value after scanning.

The scanning methods (`findNextNonWhitespace`, `findClosingQuote`, `scanString`, `contentEqualsString`) work inside the window with unrolled loops and SWAR reads, falling back to `getSlow` at window edges.

## The code

The window fast path in `get`:

```kotlin
override fun get(index: Int): Int {
    if (index in bufferStart..<bufferEnd) {
        return bufferBytes[index - bufferStart].toInt() and TOK.BYTE_MASK
    }
    return getSlow(index = index)
}
```

Releasing consumed bytes, honoring pins:

```kotlin
var retainFrom = (absoluteIndex - WR.STREAMING_BUFFER_SIZE).coerceAtLeast(0)
var pinIndex = 0
while (pinIndex < pinCount) {
    retainFrom = minOf(retainFrom, pinStack[pinIndex])
    pinIndex++
}
val aligned = (retainFrom / WR.STREAMING_BUFFER_SIZE) *
        WR.STREAMING_BUFFER_SIZE
val toSkip = aligned - discarded
if (toSkip < WR.STREAMING_BUFFER_SIZE) return
```

`byteOrEof` returns `MATCH_END` instead of throwing at end of stream, which number parsing uses to stop a number.

## Related

- [[AbstractGhostSource]]: the base class it extends.
- [[GhostSource]]: the interface it implements.
- [[GhostJsonReader]]: the reader that drives `releaseBefore` and pins.
- [[GhostSwar]]: `ghostReadLong8` skips runs of spaces in a window.
- [[GhostSourceCommon]]: shares helpers such as `rollingHashImpl` and `swarHasZeroByte`.
- [[GhostJsonException]]: thrown at an unexpected end of input.
- [[ByteArrayGhostSource]]: the in-memory counterpart.
