---
tags: [ghost-serializer, serialization, proto, wkt, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoInt64ValueSerializer.kt
kind: object
---

# ProtoInt64ValueSerializer

The serializer object that maps `ProtoInt64Value` to and from the proto3 JSON form of `google.protobuf.Int64Value`.

## What it is about

Well-known types have fixed JSON forms defined by the protobuf specification, so Ghost ships hand-written serializers for them instead of generating code. `ProtoInt64ValueSerializer` handles `ProtoInt64Value`, whose JSON form is a JSON string, because 64-bit values can exceed what many JSON parsers hold exactly in a number. It is returned by the lookup table in [[GhostSerializerLookup]], so applications get it automatically. Reading goes through `nextLong()`, which the proto reader overrides with its own 64-bit parsing.

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

The object extends [[AbstractGhostSerializer]] and overrides `typeName` with the type name constant from [[GhostProtoConstants]]. Decoding calls `nextLong()`. Encoding formats the number with `formatLong` from [[ProtoLongDigits]] and writes the digits as a JSON string, which is what makes the output quoted. That helper handles `Long.MIN_VALUE` without overflow.

## The code

The type name ties the serializer to the proto type:

```kotlin
object ProtoInt64ValueSerializer : AbstractGhostSerializer<ProtoInt64Value>() {
    override val typeName: String get() = PC.WKT_INT64_VALUE_TYPE
```

Decoding, shown for the byte-array channel:

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoInt64Value =
        ProtoInt64Value(value = reader.nextLong())
```

Encoding, shown for the byte writer:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoInt64Value) {
        writer.value(text = formatLong(value = value.value))
    }
```

## Related

- [[ProtoInt64Value]]: the Kotlin model this serializer reads and writes
- [[AbstractGhostSerializer]]: the base class whose `deserialize` and `serialize` overloads are overridden here
- [[GhostSerializerLookup]]: the table that returns this serializer for the type
- [[GhostProtoConstants]]: provides the `typeName`
- [[ProtoLongDigits]]: supplies `formatLong`
