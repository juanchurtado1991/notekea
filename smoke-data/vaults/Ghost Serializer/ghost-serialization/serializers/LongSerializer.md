---
tags: [ghost-serializer, serialization, primitive-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/LongSerializer.kt
kind: object
---

# LongSerializer

The serializer object that reads and writes a Kotlin `Long` as a JSON number.

## What it is about

`LongSerializer` is the built-in serializer for Kotlin `Long`. It is a singleton `object` at the bottom of Ghost's serializer layer: generated code for data classes, and collection serializers such as [[ListSerializer]], call it whenever they meet a `Long` value. Ghost has three input channels (streaming bytes, a flat byte array and a string) and two output channels (byte writer and string writer), so the object implements all five entry points and forwards each to the matching reader or writer.

## How it works

Reading: the overload is chosen by the reader type it receives ([[GhostJsonReader]], [[GhostJsonFlatReader]] or [[GhostJsonStringReader]]). It calls `nextLong()`. Each channel has its own `nextLong` (an extension in `parser.streaming`, one in `parser.strings`, and a member on the flat reader), which is why the file imports two functions with the same name.

Writing: `writer.value(number = value)` is called with a `Long`, so the writer uses its dedicated 64-bit digit emitter instead of widening or boxing.

The serializer holds no parsing logic. The tricky work (scanning, validation, error positions) lives once per channel in the readers and writers, which keeps this file a thin, allocation-free adapter. As an `object` it is created once and never per call. 

## The code

`typeName` returns the `TOK.TYPE_NAME_LONG` constant of [[GhostJsonTokens]]; collection serializers use it to build names such as `List<Long>`, and it appears in error messages. The first read overload and the first write overload are shown here (the other channels are copies that differ only in the reader or writer type):

```kotlin
object LongSerializer : AbstractGhostSerializer<Long>() {
    override val typeName: String get() = TOK.TYPE_NAME_LONG
    // …
    override fun deserialize(reader: GhostJsonReader): Long {
        return reader.nextLong()
    }
    // …
    override fun serialize(writer: GhostJsonWriter, value: Long) {
        writer.value(number = value)
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
- [[ListSerializer]]: wraps an item serializer such as this one to read and write `List<T>`.
- [[LongArraySerializer]]: the primitive `LongArray` serializer, which has its own fast path and falls back to the same `nextLong()` reader.
