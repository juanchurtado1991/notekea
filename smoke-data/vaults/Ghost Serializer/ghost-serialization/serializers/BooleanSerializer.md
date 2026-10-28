---
tags: [ghost-serializer, serialization, primitive-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/BooleanSerializer.kt
kind: object
---

# BooleanSerializer

The serializer object that reads and writes a Kotlin `Boolean` as a bare JSON `true` or `false`.

## What it is about

`BooleanSerializer` is the built-in serializer for Kotlin `Boolean`. It is a singleton `object` at the bottom of Ghost's serializer layer: generated code for data classes, and collection serializers such as [[ListSerializer]], call it whenever they meet a `Boolean` value. Ghost has three input channels (streaming bytes, a flat byte array and a string) and two output channels (byte writer and string writer), so the object implements all five entry points and forwards each to the matching reader or writer.

## How it works

Reading: the overload is chosen by the reader type it receives ([[GhostJsonReader]], [[GhostJsonFlatReader]] or [[GhostJsonStringReader]]). It calls `nextBoolean()`. The readers are the place where a boolean may be accepted in other shapes (the coercion rules are applied there, not in this object).

Writing: `writer.value(value = value)` selects the `Boolean` overload of `value`.

The serializer holds no parsing logic. The tricky work (scanning, validation, error positions) lives once per channel in the readers and writers, which keeps this file a thin, allocation-free adapter. As an `object` it is created once and never per call. 

## The code

`typeName` returns the `TOK.TYPE_NAME_BOOLEAN` constant of [[GhostJsonTokens]]; collection serializers use it to build names such as `List<Boolean>`, and it appears in error messages. The first read overload and the first write overload are shown here (the other channels are copies that differ only in the reader or writer type):

```kotlin
object BooleanSerializer : AbstractGhostSerializer<Boolean>() {
    override val typeName: String get() = TOK.TYPE_NAME_BOOLEAN
    // …
    override fun deserialize(reader: GhostJsonReader): Boolean {
        return reader.nextBoolean()
    }
    // …
    override fun serialize(writer: GhostJsonWriter, value: Boolean) {
        writer.value(value = value)
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
- [[BooleanArraySerializer]]: the `BooleanArray` serializer, which tries a fast path for plain literals and otherwise calls the same reader.
- [[GhostBooleanCoercionHelpers]]: helpers that decide how non-literal values are turned into booleans.
