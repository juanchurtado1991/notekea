---
tags: [ghost-serializer, serialization, primitive-serializer, array]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/BooleanArraySerializer.kt
kind: object
---

# BooleanArraySerializer

The serializer object for a primitive `BooleanArray`, with a fast path for plain `true`/`false` literals.

## What it is about

`BooleanArraySerializer` reads and writes `BooleanArray` as a JSON array of booleans. It is a singleton `object` that implements the three reader channels and two writer channels. Like the numeric array serializers it first tries a tight scan over the input bytes and only then falls back to the general loop, but its fallback also accepts the coerced boolean forms that `nextBoolean()` understands.

## How it works

In each read overload, `beginArray()` is called and an immediately closed array returns `BooleanArray(0)`. Then `tryFastBooleanArrayCore` is given the reader's `position`, `limit` and a `getByte` lambda. It matches the `true` and `false` literals one after another, separated by commas, up to `]`. If everything fits, the reader is repositioned at the `]`, `endArray()` is called and the array is returned.

If the fast path returns `null` (for example the array holds `1`, `0` or quoted strings, or has spaces), the position is restored and an `ArrayList<Boolean>` is filled with `nextBoolean()`; in strict mode `consumeArraySeparator()` is called before every element except the first. The list is finally converted with `toBooleanArray()`. This slow path boxes, which is acceptable because it is the rare shape. Writing uses the shared `writeArrayElements` loop with `writer.value(value = ...)`.

## The code

The type name and the fast path call:

```kotlin
    override val typeName: String = TOK.TYPE_NAME_BOOLEAN_ARRAY

        val fast = tryFastBooleanArrayCore(
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

The fallback loop:

```kotlin
        val list = ArrayList<Boolean>()
        val strict = reader.strictMode
        while (reader.hasNext()) {
            if (strict && list.isNotEmpty()) { reader.consumeArraySeparator() }
            list.add(element = reader.nextBoolean())
        }
        reader.endArray()
        return list.toBooleanArray()
```

The write side is a three-line function repeated for the string writer:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: BooleanArray) {
        writer.beginArray()
        writeArrayElements(size = value.size) { writer.value(value = value[it]) }
        writer.endArray()
    }
```

## Related

- [[AbstractGhostSerializer]]: the base class this object extends.
- [[GhostFastPrimitiveArrayHelpers]]: hosts `tryFastBooleanArrayCore` and `writeArrayElements`.
- [[GhostBooleanList]]: the buffer the fast path fills.
- [[BooleanSerializer]]: the scalar serializer, whose `nextBoolean()` the slow path uses for each element.
- [[GhostJsonReader]]: the streaming reader channel.
- [[GhostJsonFlatReader]]: the flat byte reader channel.
- [[GhostJsonStringReader]]: the string reader channel.
- [[GhostSerializerLookup]]: maps `BooleanArray::class` to this serializer.
