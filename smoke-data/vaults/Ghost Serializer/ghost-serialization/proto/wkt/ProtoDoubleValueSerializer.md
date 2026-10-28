---
tags: [ghost-serializer, serialization, proto, wkt, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoDoubleValueSerializer.kt
kind: object
---

# ProtoDoubleValueSerializer

The serializer object that maps `ProtoDoubleValue` to and from the proto3 JSON form of `google.protobuf.DoubleValue`.

## What it is about

Well-known types have fixed JSON forms defined by the protobuf specification, so Ghost ships hand-written serializers for them instead of generating code. `ProtoDoubleValueSerializer` handles `ProtoDoubleValue`, whose JSON form is a JSON number, or one of the strings `"NaN"`, `"Infinity"` and `"-Infinity"`. It is returned by the lookup table in [[GhostSerializerLookup]], so applications get it automatically. The non-finite strings are the part that differs from a plain number.

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

The object extends [[AbstractGhostSerializer]] and overrides `typeName` with the type name constant from [[GhostProtoConstants]]. Decoding calls `nextDouble()`, which the proto reader overrides to accept the quoted non-finite names. Encoding calls `protoValue(number = ...)` from [[GhostProtoFloatingValues]]: finite numbers use the normal shortest round-trip formatting and non-finite ones become strings.

## The code

The type name ties the serializer to the proto type:

```kotlin
object ProtoDoubleValueSerializer : AbstractGhostSerializer<ProtoDoubleValue>() {
    override val typeName: String get() = PC.WKT_DOUBLE_VALUE_TYPE
```

Decoding, shown for the byte-array channel:

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoDoubleValue =
        ProtoDoubleValue(value = reader.nextDouble())
```

Encoding, shown for the byte writer:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoDoubleValue) {
        writer.protoValue(number = value.value)
    }
```

## Related

- [[ProtoDoubleValue]]: the Kotlin model this serializer reads and writes
- [[AbstractGhostSerializer]]: the base class whose `deserialize` and `serialize` overloads are overridden here
- [[GhostSerializerLookup]]: the table that returns this serializer for the type
- [[GhostProtoConstants]]: provides the `typeName`
- [[GhostProtoFloatingValues]]: supplies `protoValue` for writing non-finite doubles
