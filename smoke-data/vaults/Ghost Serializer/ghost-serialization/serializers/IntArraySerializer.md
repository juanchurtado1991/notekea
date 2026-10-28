---
tags: [ghost-serializer, serialization, primitive-serializer, array]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/IntArraySerializer.kt
kind: object
---

# IntArraySerializer

The serializer object for a primitive `IntArray`, written as a JSON array of numbers with a fast path for compact input.

## What it is about

`IntArraySerializer` handles the primitive `IntArray` type, which Kotlin does not treat as a `List<Int>`. It is a singleton `object` for the three reader channels and two writer channels of Ghost. Because large numeric arrays are common, it is one of the places where Ghost trades some code duplication for speed: it first tries a very tight scan, and only if that does not apply does it use the general element-by-element loop.

## How it works

Reading goes through three steps in each overload. First `beginArray()` is called, and if the next byte is `]` the array is empty and an empty `IntArray(0)` is returned. Second, `tryFastIntArrayCore` is tried with the reader's `position`, `limit` and `getByte`; on success the reader is moved to the closing bracket, `endArray()` is called and the finished array is returned. Third, if the fast path returned `null` (spaces, an exponent, too many digits...), the position is already restored, and a [[GhostIntList]] collects the values with `reader.nextInt()` in a loop, calling `consumeArraySeparator()` between elements when `strictMode` is on.

Writing is just `beginArray()`, one `writer.value(number = value[it])` per element through `writeArrayElements`, and `endArray()`. The three read overloads are textual copies that differ only in the reader type, a deliberate choice that avoids a shared abstraction on the hot path.

## The code

The fast path attempt, copied from the first overload:

```kotlin
        val fast = tryFastIntArrayCore(
            startPosition = reader.position,
            limit = reader.limit,
            getByte = { reader.getByte(index = it) },
            setPosition = { reader.position = it; reader.nextTokenByte = SCN.RESET_TOKEN_BYTE },
        )
        if (fast != null) {
            reader.endArray()
            return fast
        }
```

The general loop after it:

```kotlin
        val list = GhostIntList()
        val strict = reader.strictMode
        while (reader.hasNext()) {
            if (strict && !list.isEmpty()) {
                reader.consumeArraySeparator()
            }
            list.add(value = reader.nextInt())
        }

        reader.endArray()
        return list.toArray()
```

The write side:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: IntArray) {
        writer.beginArray()
        writeArrayElements(size = value.size) { writer.value(number = value[it]) }
        writer.endArray()
    }
```

## Related

- [[AbstractGhostSerializer]]: the base class this object extends.
- [[GhostFastPrimitiveArrayHelpers]]: supplies the fast scan and `writeArrayElements`.
- [[GhostIntList]]: collects the elements of the slow path without boxing.
- [[GhostJsonReader]]: the streaming reader channel.
- [[GhostJsonFlatReader]]: the flat byte reader channel.
- [[GhostJsonStringReader]]: the string reader channel.
- [[GhostSerializerLookup]]: maps `IntArray::class` to this serializer.
