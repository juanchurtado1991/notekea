---
tags: [ghost-serializer, serialization, primitive-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/StringSerializer.kt
kind: object
---

# StringSerializer

The serializer object that reads and writes a Kotlin `String` as a quoted JSON string.

## What it is about

`StringSerializer` is the built-in serializer for Kotlin `String`. It is a singleton `object` at the bottom of Ghost's serializer layer: generated code for data classes, and collection serializers such as [[ListSerializer]], call it whenever they meet a `String` value. Ghost has three input channels (streaming bytes, a flat byte array and a string) and two output channels (byte writer and string writer), so the object implements all five entry points and forwards each to the matching reader or writer.

## How it works

Reading: the overload is chosen by the reader type it receives ([[GhostJsonReader]], [[GhostJsonFlatReader]] or [[GhostJsonStringReader]]). The streaming reader exposes `readQuotedString()` as a member, while the flat reader and the string reader provide extension functions of the same name. The file imports those two with aliases (`readQuotedStringBytes`, `readQuotedStringChars`) so that the three calls do not clash.

Writing: `writer.value(text = value)` selects the `String` overload, which quotes and escapes the text.

The serializer holds no parsing logic. The tricky work (scanning, validation, error positions) lives once per channel in the readers and writers, which keeps this file a thin, allocation-free adapter. As an `object` it is created once and never per call. 

## The code

`typeName` returns the `TOK.TYPE_NAME_STRING` constant of [[GhostJsonTokens]]; collection serializers use it to build names such as `List<String>`, and it appears in error messages. The first read overload and the first write overload are shown here (the other channels are copies that differ only in the reader or writer type):

```kotlin
object StringSerializer : AbstractGhostSerializer<String>() {
    override val typeName: String get() = TOK.TYPE_NAME_STRING
    // …
    override fun deserialize(reader: GhostJsonReader): String {
        return reader.readQuotedString()
    }
    // …
    override fun serialize(writer: GhostJsonWriter, value: String) {
        writer.value(text = value)
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
- [[GhostJsonEscapeHelpers]]: used by the writers to escape special characters in the text.
- [[MapSerializer]]: uses `String` keys, which it reads and writes through the reader and writer directly.
