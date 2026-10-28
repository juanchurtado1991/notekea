---
tags: [ghost-serializer, serialization, proto, wkt, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoUInt64ValueSerializer.kt
kind: object
---

# ProtoUInt64ValueSerializer

The serializer object that maps `ProtoUInt64Value` to and from the proto3 JSON form of `google.protobuf.UInt64Value`.

## What it is about

Well-known types have fixed JSON forms defined by the protobuf specification, so Ghost ships hand-written serializers for them instead of generating code. `ProtoUInt64ValueSerializer` handles `ProtoUInt64Value`, whose JSON form is a JSON string holding an unsigned decimal. It is returned by the lookup table in [[GhostSerializerLookup]], so applications get it automatically. Using `ULong` keeps the full range up to 18446744073709551615.

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

The object extends [[AbstractGhostSerializer]] and overrides `typeName` with the type name constant from [[GhostProtoConstants]]. Decoding calls `nextULong()` on the reader, which is where range and sign checks live. Encoding writes `value.value.toString()` as a string, since `Long` formatting helpers could not print values above `Long.MAX_VALUE`.

## The code

The type name ties the serializer to the proto type:

```kotlin
object ProtoUInt64ValueSerializer : AbstractGhostSerializer<ProtoUInt64Value>() {
    override val typeName: String get() = PC.WKT_UINT64_VALUE_TYPE
```

Decoding, shown for the byte-array channel:

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoUInt64Value =
        ProtoUInt64Value(value = reader.nextULong())
```

Encoding, shown for the byte writer:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoUInt64Value) {
        writer.value(text = value.value.toString())
    }
```

## Related

- [[ProtoUInt64Value]]: the Kotlin model this serializer reads and writes
- [[AbstractGhostSerializer]]: the base class whose `deserialize` and `serialize` overloads are overridden here
- [[GhostSerializerLookup]]: the table that returns this serializer for the type
- [[GhostProtoConstants]]: provides the `typeName`
