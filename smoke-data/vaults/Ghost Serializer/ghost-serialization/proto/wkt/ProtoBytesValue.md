---
tags: [ghost-serializer, serialization, proto, wkt, value-class, base64]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoBytesValue.kt
kind: class
---

# ProtoBytesValue

A Kotlin value class for the protobuf `google.protobuf.BytesValue` wrapper message, holding a `ByteArray`.

## What it is about

Proto3's only JSON form for binary data is a base64 string, and `BytesValue` is the wrapper that gives an optional `bytes` field presence semantics. This class is the Kotlin model; encoding to and decoding from base64 is done by [[ProtoBytesValueSerializer]].

## How it works

It is a `@JvmInline value class` around a `ByteArray`. Be aware that arrays use reference equality, so two `ProtoBytesValue` instances with identical contents are not `==` unless they share the same array; compare `value.contentEquals(...)` in tests. (Contrast with [[ProtoAny]], a data class that overrides `equals` to compare contents.)

The class has no logic of its own. [[GhostSerializerLookup]] returns the serializer, which uses a fast path when the reader is a [[GhostProtoJsonFlatReader]].

A practical consequence of the array inside is that the wrapper is mutable through its `value`: the bytes can be changed after construction, and the JSON written later reflects the change. Treat instances as read-only if they are shared between threads. On the wire the payload is padded base64 when written, while the decoder is more tolerant and also accepts the URL-safe alphabet (`-` and `_`), as the tables in [[GhostProtoConstants]] show.

## The code

The declaration:

```kotlin
@JvmInline
value class ProtoBytesValue(val value: ByteArray)
```

Nothing else is needed: the base64 conversion is isolated in the serializer and in [[GhostBase64]].

## Related

- [[ProtoBytesValueSerializer]]: converts the bytes to and from base64 text
- [[GhostSerializerLookup]]: registers the serializer for this class
- [[GhostBase64]]: the base64 helpers used by the serializer
- [[ProtoAny]]: another WKT that holds a `ByteArray` but overrides equality
