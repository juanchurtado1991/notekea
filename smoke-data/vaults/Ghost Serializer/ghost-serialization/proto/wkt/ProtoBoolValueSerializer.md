---
tags: [ghost-serializer, serialization, proto, wkt, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoBoolValueSerializer.kt
kind: object
---

# ProtoBoolValueSerializer

The serializer object that maps `ProtoBoolValue` to and from the proto3 JSON form of `google.protobuf.BoolValue`.

## What it is about

Well-known types have fixed JSON forms defined by the protobuf specification, so Ghost ships hand-written serializers for them instead of generating code. `ProtoBoolValueSerializer` handles `ProtoBoolValue`, whose JSON form is a JSON boolean. It is returned by the lookup table in [[GhostSerializerLookup]], so applications get it automatically. It is deliberately thin.

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

The object extends [[AbstractGhostSerializer]] and overrides `typeName` with the type name constant from [[GhostProtoConstants]]. Each decode overload calls `nextBoolean()` on its reader and wraps the result; each encode overload calls `value(value = ...)`.

## The code

The type name ties the serializer to the proto type:

```kotlin
object ProtoBoolValueSerializer : AbstractGhostSerializer<ProtoBoolValue>() {
    override val typeName: String get() = PC.WKT_BOOL_VALUE_TYPE
```

Decoding, shown for the byte-array channel:

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoBoolValue =
        ProtoBoolValue(value = reader.nextBoolean())
```

Encoding, shown for the byte writer:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoBoolValue) {
        writer.value(value = value.value)
    }
```

## Related

- [[ProtoBoolValue]]: the Kotlin model this serializer reads and writes
- [[AbstractGhostSerializer]]: the base class whose `deserialize` and `serialize` overloads are overridden here
- [[GhostSerializerLookup]]: the table that returns this serializer for the type
- [[GhostProtoConstants]]: provides the `typeName`
