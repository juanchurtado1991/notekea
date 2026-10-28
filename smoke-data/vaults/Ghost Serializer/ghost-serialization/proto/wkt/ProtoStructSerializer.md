---
tags: [ghost-serializer, serialization, proto, wkt, serializer, struct]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoStructSerializer.kt
kind: object
---

# ProtoStructSerializer

The serializer object for `ProtoStruct`, a map of string keys to `ProtoValue`, read and written as a JSON object.

## What it is about

A Struct is a JSON object whose values can be anything. `ProtoStructSerializer` reads such an object into a `MutableMap<String, ProtoValue>` and writes it back out, delegating every value to [[ProtoValueSerializer]]. It is for code that asks for a serializer of the [[ProtoStruct]] alias directly (the lookup table has no entry for the alias, as `KClass` erases it to `Map`).

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

Decoding opens an object and loops until the closing brace: it reads a key, consumes the key separator, decodes the value with `ProtoValueSerializer.deserialize(reader = reader)`, stores it, and consumes a comma when one follows. The map is returned after `endObject()`. Encoding writes `beginObject()`, then for each entry a `name(key)` followed by the value serializer, then `endObject()`.

## The code

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoStruct {
        val map = mutableMapOf<String, ProtoValue>()
        reader.beginObject()
        while (reader.peekNextToken() != TOK.CLOSE_OBJ_INT) {
            val key = reader.nextString()
            reader.consumeKeySeparator()
            val value = ProtoValueSerializer.deserialize(reader = reader)
            map[key] = value
            if (reader.peekNextToken() == TOK.COMMA_INT) {
                reader.consumeArraySeparator()
            }
        }
        reader.endObject()
```

Encoding:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoStruct) {
        writer.beginObject()
        for ((mapKey, mapValue) in value) {
            writer.name(key = mapKey)
            ProtoValueSerializer.serialize(writer = writer, value = mapValue)
        }
        writer.endObject()
```

## Related

- [[ProtoStruct]]: the alias for the map type handled here
- [[ProtoValueSerializer]]: decodes and encodes each value
- [[ProtoValue]]: the value type of the map
- [[AbstractGhostSerializer]]: the base class whose overloads are overridden here
- [[GhostSerializerLookup]]: its comment explains why `ProtoStruct` has no direct entry
