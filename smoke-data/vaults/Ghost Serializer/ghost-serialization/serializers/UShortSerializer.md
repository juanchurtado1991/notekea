---
tags: [ghost-serializer, serialization, primitive-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/UShortSerializer.kt
kind: object
---

# UShortSerializer

The serializer object that reads and writes a Kotlin `UShort` as a JSON number from 0 to 65535.

## What it is about

`UShortSerializer` is the built-in serializer for Kotlin `UShort`. It is a singleton `object` at the bottom of Ghost's serializer layer: generated code for data classes, and collection serializers such as [[ListSerializer]], call it whenever they meet a `UShort` value. Ghost has three input channels (streaming bytes, a flat byte array and a string) and two output channels (byte writer and string writer), so the object implements all five entry points and forwards each to the matching reader or writer.

## How it works

Reading: the overload is chosen by the reader type it receives ([[GhostJsonReader]], [[GhostJsonFlatReader]] or [[GhostJsonStringReader]]). It reads an `Int` with `nextInt()` and hands it to `narrowToUShort`, which rejects negatives and values above 65535, reporting through `reader.throwError`.

Writing: A `UShort` is widened with `value.toInt()` and written through the `Int` overload.

The serializer holds no parsing logic. The tricky work (scanning, validation, error positions) lives once per channel in the readers and writers, which keeps this file a thin, allocation-free adapter. As an `object` it is created once and never per call. 

## The code

`typeName` returns the `TOK.TYPE_NAME_USHORT` constant of [[GhostJsonTokens]]; collection serializers use it to build names such as `List<UShort>`, and it appears in error messages. The first read overload and the first write overload are shown here (the other channels are copies that differ only in the reader or writer type):

```kotlin
object UShortSerializer : AbstractGhostSerializer<UShort>() {
    override val typeName: String get() = TOK.TYPE_NAME_USHORT
    // …
    override fun deserialize(reader: GhostJsonReader): UShort = narrowToUShort(
        value = reader.nextInt()
    ) { reader.throwError(message = it) }
    // …
    override fun serialize(writer: GhostJsonWriter, value: UShort) {
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
- [[GhostNumericParseHelpers]]: defines `narrowToUShort` and its siblings.
- [[IntSerializer]]: reads through the same `nextInt()`.
