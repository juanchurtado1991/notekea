---
tags: [ghost-serializer, serialization, proto, wkt, serializer, base64]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoBytesValueSerializer.kt
kind: object
---

# ProtoBytesValueSerializer

The serializer object that maps `ProtoBytesValue` to and from a base64 JSON string.

## What it is about

Proto3 JSON has exactly one representation for `bytes`: a base64 string. This serializer implements it for the `BytesValue` wrapper. It is the one scalar serializer with a performance fast path, because binary payloads can be large and decoding them through an intermediate `String` wastes memory.

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

The `GhostJsonReader` and `GhostJsonStringReader` overloads read the string with `nextString()` and decode it with `decodeBase64String`, reporting failures through `throwError`. The `GhostJsonFlatReader` overload first checks whether the reader is actually a [[GhostProtoJsonFlatReader]]; if so it calls `nextProtoBytes()`, which decodes straight from the input into a byte array via a pooled scratch buffer. Otherwise it falls back to the shared string decoder. Encoding converts with `encodeBase64String` and writes the text.

## The code

```kotlin
    // Fast path for GhostProtoJsonFlatReader (pooled scratch buffer); else shared decoder.
    override fun deserialize(reader: GhostJsonFlatReader): ProtoBytesValue {
        if (reader is GhostProtoJsonFlatReader) {
            return ProtoBytesValue(value = reader.nextProtoBytes())
        }
        return ProtoBytesValue(value = decodeBase64String(value = reader.nextString()) { reader.throwError(message = it) })
    }
```

Encoding:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoBytesValue) {
        writer.value(text = encodeBase64String(source = value.value))
    }
```

## Related

- [[ProtoBytesValue]]: the model class holding the `ByteArray`
- [[AbstractGhostSerializer]]: the base class whose `deserialize` and `serialize` overloads are overridden here
- [[GhostProtoJsonFlatReader]]: provides the allocation-light `nextProtoBytes` fast path
- [[ProtoJsonFlatReaderBase64]]: the reader-side base64 decoding behind that fast path
- [[GhostBase64]]: the shared base64 encoder and decoder
- [[GhostSerializerLookup]]: the table that returns this serializer
