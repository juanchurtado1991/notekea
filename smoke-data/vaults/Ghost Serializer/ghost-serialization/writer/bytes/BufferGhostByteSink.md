---
tags: [ghost-serializer, serialization, writer, sink, okio]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/bytes/BufferGhostByteSink.kt
kind: class
---

# BufferGhostByteSink

The `GhostByteSink` implementation that writes into an Okio `BufferedSink`, used for streaming output.

## What it is about

When the caller encodes straight to a stream (for instance a network response or a file), `GhostJsonWriter` writes through this class. It is the streaming backend; the in-memory backend is `FlatByteArrayWriter`.

## How it works

The class holds the sink and, for speed, a reference to the sink's own `Buffer`; all writes go to that buffer and `flush()` calls `sink.emit()`. The literal helpers write pre-built `ByteString`s (`DOT_ZERO`, `FALSE_BS`, `NULL_BS`, `TRUE_BS`), and `writeQuotedAscii` writes a quote, the text as UTF-8 and a quote. The BMP code unit operation delegates to the `Buffer.writeQuotedBmpCodeUnit` extension. The class is `internal`; users get it only through the `GhostJsonWriter(sink)` constructor.

## The code

```kotlin
internal class BufferGhostByteSink(private val sink: BufferedSink) : GhostByteSink {

    private val buffer = sink.buffer

    override fun flush() {
        sink.emit()
    }

    override fun write(bytes: ByteArray, offset: Int, length: Int) {
        buffer.write(bytes, offset, length)
    }
    // …
}
```

## Related

- [[GhostByteSink]]: the interface it implements
- [[GhostJsonWriter]]: creates it in its `BufferedSink` constructor
- [[FlatByteArrayWriter]]: the in-memory alternative
- [[GhostJsonWriterBmpChar]]: provides the quoted character write
