---
tags: [ghost-serializer, serialization, proto, wkt, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoStringValueSerializer.kt
kind: object
---

# ProtoStringValueSerializer

The serializer object that maps `ProtoStringValue` to and from the proto3 JSON form of `google.protobuf.StringValue`.

## What it is about

Well-known types have fixed JSON forms defined by the protobuf specification, so Ghost ships hand-written serializers for them instead of generating code. `ProtoStringValueSerializer` handles `ProtoStringValue`, whose JSON form is a JSON string. It is returned by the lookup table in [[GhostSerializerLookup]], so applications get it automatically. There is no extra validation beyond what the reader does for any string.

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

The object extends [[AbstractGhostSerializer]] and overrides `typeName` with the type name constant from [[GhostProtoConstants]]. Decoding calls `nextString()` and writing calls `value(text = ...)`, so escape sequences and Unicode follow the standard JSON rules of the respective channel.

## The code

The type name ties the serializer to the proto type:

```kotlin
object ProtoStringValueSerializer : AbstractGhostSerializer<ProtoStringValue>() {
    override val typeName: String get() = PC.WKT_STRING_VALUE_TYPE
```

Decoding, shown for the byte-array channel:

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoStringValue =
        ProtoStringValue(value = reader.nextString())
```

Encoding, shown for the byte writer:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoStringValue) {
        writer.value(text = value.value)
    }
```

## Related

- [[ProtoStringValue]]: the Kotlin model this serializer reads and writes
- [[AbstractGhostSerializer]]: the base class whose `deserialize` and `serialize` overloads are overridden here
- [[GhostSerializerLookup]]: the table that returns this serializer for the type
- [[GhostProtoConstants]]: provides the `typeName`
