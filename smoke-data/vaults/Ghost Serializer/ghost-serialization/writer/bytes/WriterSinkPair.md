---
tags: [ghost-serializer, serialization, writer, pooling, bytes]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/bytes/WriterSinkPair.kt
kind: class
---

# WriterSinkPair

A holder that pairs a `FlatByteArrayWriter` with the `GhostJsonWriter` that writes into it.

## What it is about

In-memory byte encodes (`Ghost.encodeToBytes`, draining to a sink, discarding output) need a growable byte buffer and a JSON writer wired to it. `WriterSinkPair` bundles the two so a platform can keep one pair per thread and reuse it for every encode. String encodes use the matching `WriterStringPair`.

## How it works

The two fields are created once. `byteWriter` is a `FlatByteArrayWriter` with the initial capacity `INITIAL_WRITE_BUFFER_SIZE`; `writer` is a `GhostJsonWriter` constructed over it. Because both objects live as long as the pair, the buffer survives between encodes and only its content is reset, so after warm-up there are no allocations. The platform actuals hold the pair in a `ThreadLocal` (or `@ThreadLocal` on Native), so threads do not contend.

## The code

```kotlin
@InternalGhostApi
class WriterSinkPair {
    val byteWriter: FlatByteArrayWriter = FlatByteArrayWriter(initialCapacity = INITIAL_WRITE_BUFFER_SIZE)

    val writer: GhostJsonWriter = GhostJsonWriter(byteWriter)
}
```

After an encode the payload is in `byteWriter.array[0 until byteWriter.size]`.

## Related

- [[FlatByteArrayWriter]]: the buffer the pair owns
- [[GhostJsonWriter]]: the writer built over that buffer
- [[WriterStringPair]]: char-channel counterpart
- [[Ghost.jvmAndroid]]: keeps the pair in a `ThreadLocal`
- [[Ghost.nonJvm]]: keeps the pair in a Native `@ThreadLocal`
- [[GhostJsonWriterConstants]]: defines `INITIAL_WRITE_BUFFER_SIZE`
