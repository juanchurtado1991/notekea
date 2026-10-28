---
tags: [ghost-serializer, serialization, primitive-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/IntSerializer.kt
kind: object
---

# IntSerializer

The serializer object that reads and writes a Kotlin `Int` as a JSON number.

## What it is about

`IntSerializer` is the built-in serializer for Kotlin `Int`. It is a singleton `object` at the bottom of Ghost's serializer layer: generated code for data classes, and collection serializers such as [[ListSerializer]], call it whenever they meet a `Int` value. Ghost has three input channels (streaming bytes, a flat byte array and a string) and two output channels (byte writer and string writer), so the object implements all five entry points and forwards each to the matching reader or writer.

## How it works

Reading: the overload is chosen by the reader type it receives ([[GhostJsonReader]], [[GhostJsonFlatReader]] or [[GhostJsonStringReader]]). It calls `nextInt()`, the same primitive that [[UByteSerializer]] and [[UShortSerializer]] reuse before range-checking.

Writing: `writer.value(number = value)` is called with an `Int`; no conversion is needed.

The serializer holds no parsing logic. The tricky work (scanning, validation, error positions) lives once per channel in the readers and writers, which keeps this file a thin, allocation-free adapter. As an `object` it is created once and never per call. 

## The code

`typeName` returns the `TOK.TYPE_NAME_INT` constant of [[GhostJsonTokens]]; collection serializers use it to build names such as `List<Int>`, and it appears in error messages. The first read overload and the first write overload are shown here (the other channels are copies that differ only in the reader or writer type):

```kotlin
object IntSerializer : AbstractGhostSerializer<Int>() {
    override val typeName: String get() = TOK.TYPE_NAME_INT
    // …
    override fun deserialize(reader: GhostJsonReader): Int {
        return reader.nextInt()
    }
    // …
    override fun serialize(writer: GhostJsonWriter, value: Int) {
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
- [[IntArraySerializer]]: the primitive `IntArray` serializer, which falls back to this element reading.
- [[UByteSerializer]]: reads through `nextInt()` as well, then narrows.
