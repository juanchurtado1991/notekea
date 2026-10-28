---
tags: [ghost-serializer, serialization, writer, sink, bytes]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/bytes/GhostGenericByteSink.kt
kind: interface
---

# GhostGenericByteSink

The basic byte-sink interface with no JSON-specific operations.

## What it is about

Ghost's YAML writer needs to push bytes to either an Okio sink or a flat array, but it never uses the JSON literal and quoting shortcuts. This interface is the minimal set it depends on. `GhostByteSink` extends it for JSON.

## How it works

It declares `flush`, the byte-array and `ByteString` writes (with and without an offset), `write2Bytes` for two bytes in one bounds check, `writeByte`, and two `writeUtf8` overloads for a whole string or a range. Keeping the YAML writer on this narrower type means it does not depend on methods it never calls.

The interface has two implementations through its child: the Okio-backed `BufferGhostByteSink` and the in-memory `FlatByteArrayWriter`. The YAML writer holds one of them behind this type. Having `write2Bytes` in the generic set lets both channels write two-byte escapes with a single bounds check.

## The code

```kotlin
interface GhostGenericByteSink {
    fun flush()
    fun write(bytes: ByteArray)
    fun write(bytes: ByteArray, offset: Int, length: Int)
    fun write(byteString: ByteString)
    fun write2Bytes(firstByte: Int, secondByte: Int)
    fun writeByte(byteAsInt: Int)
    fun writeUtf8(text: String)
    fun writeUtf8(text: String, beginIndex: Int, endIndex: Int)
}
```

## Related

- [[GhostByteSink]]: extends it with JSON-only intrinsics
- [[GhostYamlWriter]]: depends on this interface alone
- [[FlatByteArrayWriter]]: one of the implementations (through `GhostByteSink`)
- [[BufferGhostByteSink]]: the Okio implementation (through `GhostByteSink`)
