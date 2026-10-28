---
tags: [ghost-serializer, serialization, primitive-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/CharSerializer.kt
kind: object
---

# CharSerializer

The serializer object that reads and writes a Kotlin `Char` as a length-1 JSON string (not a number).

## What it is about

`CharSerializer` is the built-in serializer for Kotlin `Char`. It is a singleton `object` at the bottom of Ghost's serializer layer: generated code for data classes, and collection serializers such as [[ListSerializer]], call it whenever they meet a `Char` value. Ghost has three input channels (streaming bytes, a flat byte array and a string) and two output channels (byte writer and string writer), so the object implements all five entry points and forwards each to the matching reader or writer.

## How it works

Reading: the overload is chosen by the reader type it receives ([[GhostJsonReader]], [[GhostJsonFlatReader]] or [[GhostJsonStringReader]]). It calls `nextChar()`, which expects a quoted string of exactly one character; the file imports a separate `nextChar` for each channel.

Writing: `writer.value(char = value)` selects the `Char` overload, which writes the character quoted and escaped like any string.

The serializer holds no parsing logic. The tricky work (scanning, validation, error positions) lives once per channel in the readers and writers, which keeps this file a thin, allocation-free adapter. As an `object` it is created once and never per call. 

## The code

`typeName` returns the `TOK.TYPE_NAME_CHAR` constant of [[GhostJsonTokens]]; collection serializers use it to build names such as `List<Char>`, and it appears in error messages. The first read overload and the first write overload are shown here (the other channels are copies that differ only in the reader or writer type):

```kotlin
object CharSerializer : AbstractGhostSerializer<Char>() {
    override val typeName: String get() = TOK.TYPE_NAME_CHAR
    // …
    override fun deserialize(reader: GhostJsonReader): Char = reader.nextChar()
    // …
    override fun serialize(writer: GhostJsonWriter, value: Char) {
        writer.value(char = value)
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
- [[CharArraySerializer]]: delegates its elements to this serializer through a `ListSerializer`.
- [[GhostJsonWriterBmpChar]]: helper that handles characters of the Basic Multilingual Plane when writing.
