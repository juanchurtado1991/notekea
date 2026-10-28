---
tags: [ghost-serializer, serialization, primitive-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/ByteSerializer.kt
kind: object
---

# ByteSerializer

The serializer object that reads and writes a Kotlin `Byte` as a JSON number in the range of a byte.

## What it is about

`ByteSerializer` is the built-in serializer for Kotlin `Byte`. It is a singleton `object` at the bottom of Ghost's serializer layer: generated code for data classes, and collection serializers such as [[ListSerializer]], call it whenever they meet a `Byte` value. Ghost has three input channels (streaming bytes, a flat byte array and a string) and two output channels (byte writer and string writer), so the object implements all five entry points and forwards each to the matching reader or writer.

## How it works

Reading: the overload is chosen by the reader type it receives ([[GhostJsonReader]], [[GhostJsonFlatReader]] or [[GhostJsonStringReader]]). It calls `nextByte()` as a member of each reader; the range check that rejects values outside `-128..127` is done by the reader.

Writing: A `Byte` is widened with `value.toInt()` and written through the `Int` overload, so no dedicated byte writer is needed.

The serializer holds no parsing logic. The tricky work (scanning, validation, error positions) lives once per channel in the readers and writers, which keeps this file a thin, allocation-free adapter. As an `object` it is created once and never per call. 

## The code

`typeName` returns the `TOK.TYPE_NAME_BYTE` constant of [[GhostJsonTokens]]; collection serializers use it to build names such as `List<Byte>`, and it appears in error messages. The first read overload and the first write overload are shown here (the other channels are copies that differ only in the reader or writer type):

```kotlin
object ByteSerializer : AbstractGhostSerializer<Byte>() {
    override val typeName: String get() = TOK.TYPE_NAME_BYTE
    // …
    override fun deserialize(reader: GhostJsonReader): Byte = reader.nextByte()
    // …
    override fun serialize(writer: GhostJsonWriter, value: Byte) {
        writer.value(number = value.toInt())
    }
}
```

## Related

- [[AbstractGhostSerializer]]: the base class this object extends; it supplies defaults such as `isResilient = false`.
- [[GhostJsonReader]]: the streaming reader behind the first `deserialize` overload.
- [[GhostJsonFlatReader]]: the flat byte-array reader behind the second overload.
- [[GhostJsonStringReader]]: the string reader behind the third overload.
- [[GhostJsonWriter]]: the byte writer behind the first `serialize` overload.
- [[GhostJsonStringWriter]]: the string writer behind the second `serialize` overload.
- [[GhostJsonTokens]]: holds `TYPE_NAME_BYTE`.
- [[ListSerializer]]: reads `List<Byte>` by wrapping this serializer.
