---
tags: [ghost-serializer, serialization, writer, sink, bytes]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/bytes/GhostByteSink.kt
kind: interface
---

# GhostByteSink

The JSON-specific byte sink interface that `GhostJsonWriter` writes through.

## What it is about

`GhostJsonWriter` can write to two backends: an Okio `BufferedSink` for streaming and a `FlatByteArrayWriter` for in-memory encodes. `GhostByteSink` is the contract both satisfy, so the writer's logic is written once. It extends `GhostGenericByteSink` with the JSON-only fast paths for literals and quoting.

## How it works

Each backend chooses the fastest way to implement every operation. The interface adds `writeDotZero`, `writeFalse`, `writeNull`, `writeTrue`, `writeQuotedAscii(text, length)` and `writeQuotedBmpCodeUnit(codePoint)` to the generic byte operations. YAML's writer depends on the smaller generic interface instead, so it is not forced to carry methods it never calls.

Declaring the intrinsics here, and not on the generic interface, is a deliberate separation. It lets the flat writer implement `writeTrue`, `writeNull` and friends as fused array stores, while the Okio version writes pre-built `ByteString` constants.

## The code

```kotlin
interface GhostByteSink : GhostGenericByteSink {
    fun writeDotZero()
    fun writeFalse()
    fun writeNull()
    fun writeQuotedAscii(text: String, length: Int)
    fun writeQuotedBmpCodeUnit(codePoint: Int)
    fun writeTrue()
}
```

## Related

- [[GhostGenericByteSink]]: the parent interface with the basic byte operations
- [[FlatByteArrayWriter]]: in-memory implementation
- [[BufferGhostByteSink]]: Okio-backed implementation
- [[GhostJsonWriter]]: the main consumer
- [[GhostYamlWriter]]: uses the generic interface only
