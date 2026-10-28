---
tags: [ghost-serializer, serialization, primitive-serializer, array]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/DoubleArraySerializer.kt
kind: object
---

# DoubleArraySerializer

The serializer object for a primitive `DoubleArray`, written as a JSON array of decimal numbers with a fast path.

## What it is about

`DoubleArraySerializer` reads and writes `DoubleArray` as a JSON array of numbers. It is a singleton `object` covering all three reader channels and both writer channels. Decimal arrays (coordinates, vectors, sensor samples) can be large, so it uses a fast path that skips the per-element bookkeeping of the general loop, while keeping the number grammar in a single place: the reader's own `nextDouble()`.

## How it works

Each read overload calls `beginArray()` and returns `DoubleArray(0)` for an empty array. Otherwise it creates an `ArrayList<Double>` and runs `tryFastDecimalArrayCore`, passing `getByte`, `getPosition`, `setPosition`, the list and `parseNext = { reader.nextDouble() }`. The helper checks that each element starts like a number, parses it with the reader, then expects a comma or `]`. If it succeeds, `endArray()` is called and the list is converted with `toDoubleArray()`.

If the fast path fails, the list is cleared (`list.clear()`) and the position was already reset, so the general loop takes over: `hasNext()`, a `consumeArraySeparator()` in strict mode when the list is not empty, and `nextDouble()` per element. Both routes box each element in an `ArrayList`, because the decimal helper is generic. Writing uses `writeArrayElements` with `writer.value(number = ...)`.

## The code

The fast path call, with the lambdas that adapt it to the reader:

```kotlin
        val fastSucceeded = tryFastDecimalArrayCore(
            startPosition = reader.position,
            limit = reader.limit,
            getByte = { reader.getByte(index = it) },
            getPosition = { reader.position },
            setPosition = { reader.position = it; reader.nextTokenByte = SCN.RESET_TOKEN_BYTE },
            addTo = list,
            parseNext = { reader.nextDouble() },
        )
```

After a failed fast path the list is cleared and the general loop runs:

```kotlin
        list.clear()

        val strict = reader.strictMode
        while (reader.hasNext()) {
            if (strict && list.isNotEmpty()) { reader.consumeArraySeparator() }
            list.add(element = reader.nextDouble())
        }
        reader.endArray()
        return list.toDoubleArray()
```

Writing:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: DoubleArray) {
        writer.beginArray()
        writeArrayElements(size = value.size) { writer.value(number = value[it]) }
        writer.endArray()
    }
```

## Related

- [[AbstractGhostSerializer]]: the base class this object extends.
- [[GhostFastPrimitiveArrayHelpers]]: hosts `tryFastDecimalArrayCore` and `writeArrayElements`.
- [[DoubleSerializer]]: the scalar serializer for the element type, which uses the same `nextDouble()`.
- [[GhostJsonReader]]: the streaming reader channel.
- [[GhostJsonFlatReader]]: the flat byte reader channel.
- [[GhostJsonStringReader]]: the string reader channel.
- [[GhostSerializerLookup]]: maps `DoubleArray::class` to this serializer.
