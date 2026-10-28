---
tags: [ghost-serializer, serialization, proto, wkt, serializer, validation]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoUInt32ValueSerializer.kt
kind: object
---

# ProtoUInt32ValueSerializer

The serializer object that maps `ProtoUInt32Value` to and from the proto3 JSON form of `google.protobuf.UInt32Value`, with range checks.

## What it is about

The JSON form of `UInt32Value` is a plain number, but its valid range is 0 to 4294967295, which a signed `Int` cannot hold. [[ProtoUInt32Value]] therefore stores a `Long`, and this serializer is the guard that keeps out-of-range values from entering the model. [[GhostSerializerLookup]] returns it for the class.

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

Decoding reads a `Long` with `nextLong()` and passes it through a private inline function, `requireUInt32`. A negative value reports the shared unsigned-negative message and a value above `PROTO_UINT32_MAX` reports the overflow message from [[GhostProtoConstants]]. The error is raised through the reader's own `throwError`, so the exception carries the JSON path. Encoding writes the `Long` with `value(number = ...)`.

## The code

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoUInt32Value =
        ProtoUInt32Value(value = requireUInt32(value = reader.nextLong()) { reader.throwError(message = it) })
```

The validator:

```kotlin
private inline fun requireUInt32(
    value: Long,
    throwError: (String) -> Nothing
): Long {
    if (value < 0) throwError(EM.ERR_NEGATIVE_UNSIGNED)
    if (value > PC.PROTO_UINT32_MAX) throwError(PC.ERR_PROTO_UINT32_OVERFLOW)
    return value
}
```

Because the function is `inline`, the error lambda is inlined at each call site and costs nothing on the success path.

## Related

- [[ProtoUInt32Value]]: the model class, which stores the value as a `Long`
- [[AbstractGhostSerializer]]: the base class whose `deserialize` and `serialize` overloads are overridden here
- [[GhostSerializerLookup]]: the table that returns this serializer
- [[GhostProtoConstants]]: holds `PROTO_UINT32_MAX` and the overflow message
- [[GhostJsonErrorMessages]]: holds the negative-unsigned message
