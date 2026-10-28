---
tags: [ghost-serializer, serialization, collection-serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/MapSerializer.kt
kind: class
---

# MapSerializer

A generic serializer class that reads and writes a Kotlin `Map<String, V>` as a JSON object, delegating each value to a value serializer.

## What it is about

`MapSerializer<V>` handles maps whose keys are `String`, the only key type that maps naturally onto JSON object member names. It is constructed with the `valueSerializer: GhostSerializer<V>` for the value type, and works on all three reader channels and both writer channels. Unlike [[ListSerializer]] it has no resilient branch: a bad value always fails the read.

## How it works

Reading calls `beginObject()` and returns `emptyMap()` right away if the next byte is `}`. Otherwise it builds the result with `buildMap`, in a loop: `nextKey()` returns the next member name or `null` when the object ends, `consumeKeySeparator()` consumes the colon, and the value is read with `valueSerializer.deserialize(reader = reader)`. When `nextKey()` returns `null` the loop breaks and `endObject()` is called. The loop is duplicated for each reader type.

Writing is `beginObject()`, then for each entry `writer.name(key = entry.key)` followed by the value serializer, then `endObject()`. `typeName` combines `TOK.TYPE_NAME_MAP_STRING_PREFIX` (`Map<String, `), the value's type name and the generic suffix.

## The code

The read loop:

```kotlin
        reader.beginObject()
        if (reader.peekByte() == TOK.CLOSE_OBJ) {
            reader.endObject()
            return emptyMap()
        }
```

The write side:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: Map<String, V>) {
        writer.beginObject()
        for (entry in value.entries) {
            writer.name(key = entry.key)
            valueSerializer.serialize(writer = writer, value = entry.value)
        }
        writer.endObject()
    }
```

## Related

- [[AbstractGhostSerializer]]: the base class this class extends.
- [[GhostSerializer]]: the contract of the value serializer passed to the constructor.
- [[StringSerializer]]: the serializer of `String` values, a common value type; the keys are read with `nextKey()` instead.
- [[Ghost]]: its `getSerializer(KType)` builds this serializer around the resolved value serializer.
- [[GhostJsonReader]]: the streaming reader channel.
- [[GhostJsonFlatReader]]: the flat byte reader channel.
- [[GhostJsonStringWriter]]: the string writer channel.
