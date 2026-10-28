---
tags: [ghost-serializer, serialization, primitive-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/DoubleSerializer.kt
kind: object
---

# DoubleSerializer

The serializer object that reads and writes a Kotlin `Double` as a JSON number.

## What it is about

`DoubleSerializer` is the built-in serializer for Kotlin `Double`. It is a singleton `object` at the bottom of Ghost's serializer layer: generated code for data classes, and collection serializers such as [[ListSerializer]], call it whenever they meet a `Double` value. Ghost has three input channels (streaming bytes, a flat byte array and a string) and two output channels (byte writer and string writer), so the object implements all five entry points and forwards each to the matching reader or writer.

## How it works

Reading: the overload is chosen by the reader type it receives ([[GhostJsonReader]], [[GhostJsonFlatReader]] or [[GhostJsonStringReader]]). It calls `nextDouble()`, so the decimal grammar, rounding and exponent handling are decided inside the reader of that channel, not here.

Writing: `writer.value(number = value)` is called with a `Double`; formatting the number is the writer's job.

The serializer holds no parsing logic. The tricky work (scanning, validation, error positions) lives once per channel in the readers and writers, which keeps this file a thin, allocation-free adapter. As an `object` it is created once and never per call. 

## The code

`typeName` returns the `TOK.TYPE_NAME_DOUBLE` constant of [[GhostJsonTokens]]; collection serializers use it to build names such as `List<Double>`, and it appears in error messages. The first read overload and the first write overload are shown here (the other channels are copies that differ only in the reader or writer type):

```kotlin
object DoubleSerializer : AbstractGhostSerializer<Double>() {
    override val typeName: String get() = TOK.TYPE_NAME_DOUBLE
    // …
    override fun deserialize(reader: GhostJsonReader): Double {
        return reader.nextDouble()
    }
    // …
    override fun serialize(writer: GhostJsonWriter, value: Double) {
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
- [[DoubleArraySerializer]]: the primitive `DoubleArray` serializer, which parses elements with the same `nextDouble()`.
