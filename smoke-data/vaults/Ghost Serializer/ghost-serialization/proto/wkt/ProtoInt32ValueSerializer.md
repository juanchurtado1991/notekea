---
tags: [ghost-serializer, serialization, proto, wkt, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoInt32ValueSerializer.kt
kind: object
---

# ProtoInt32ValueSerializer

The serializer object that maps `ProtoInt32Value` to and from the proto3 JSON form of `google.protobuf.Int32Value`.

## What it is about

Well-known types have fixed JSON forms defined by the protobuf specification, so Ghost ships hand-written serializers for them instead of generating code. `ProtoInt32ValueSerializer` handles `ProtoInt32Value`, whose JSON form is a JSON number. It is returned by the lookup table in [[GhostSerializerLookup]], so applications get it automatically. The value fits a JSON number exactly, so it is not quoted.

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

The object extends [[AbstractGhostSerializer]] and overrides `typeName` with the type name constant from [[GhostProtoConstants]]. Decoding uses `nextInt()`; when the reader is a [[GhostProtoJsonFlatReader]] that call is overridden to apply the proto integer rules. Encoding widens to `Long` and calls `value(number = ...)`, reusing the writer's integer routine.

## The code

The type name ties the serializer to the proto type:

```kotlin
object ProtoInt32ValueSerializer : AbstractGhostSerializer<ProtoInt32Value>() {
    override val typeName: String get() = PC.WKT_INT32_VALUE_TYPE
```

Decoding, shown for the byte-array channel:

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoInt32Value =
        ProtoInt32Value(value = reader.nextInt())
```

Encoding, shown for the byte writer:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoInt32Value) {
        writer.value(number = value.value.toLong())
    }
```

## Related

- [[ProtoInt32Value]]: the Kotlin model this serializer reads and writes
- [[AbstractGhostSerializer]]: the base class whose `deserialize` and `serialize` overloads are overridden here
- [[GhostSerializerLookup]]: the table that returns this serializer for the type
- [[GhostProtoConstants]]: provides the `typeName`
- [[GhostProtoJsonFlatReader]]: overrides `nextInt` with proto-specific parsing
