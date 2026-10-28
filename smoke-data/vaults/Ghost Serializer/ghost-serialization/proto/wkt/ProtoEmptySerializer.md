---
tags: [ghost-serializer, serialization, proto, wkt, serializer, empty]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoEmptySerializer.kt
kind: object
---

# ProtoEmptySerializer

The serializer object for `ProtoEmpty`: it accepts any JSON object and writes `{}`.

## What it is about

The `Empty` message has no fields, so its JSON is an empty object. `ProtoEmptySerializer` follows protobuf's rule of being lenient on input: if the object contains keys, it ignores them rather than failing. It always returns the singleton [[ProtoEmpty]]. [[GhostSerializerLookup]] returns it for the class.

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

Decoding opens an object, then loops until the next token is the closing brace; in each iteration it reads a key with `nextString()`, consumes the key separator and calls `skipValue()` to discard the value. It then calls `endObject()` and returns the singleton. Writing just emits `beginObject()` followed by `endObject()`.

## The code

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoEmpty {
        reader.beginObject()
        while (reader.peekNextToken() != TOK.CLOSE_OBJ_INT) {
            reader.nextString()
            reader.consumeKeySeparator()
            reader.skipValue()
        }
        reader.endObject()
        return ProtoEmpty
```

Encoding:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoEmpty) {
        writer.beginObject().endObject()
    }
```

## Related

- [[ProtoEmpty]]: the singleton returned after decoding
- [[AbstractGhostSerializer]]: the base class whose overloads are overridden here
- [[GhostSerializerLookup]]: returns this serializer for the type
- [[GhostJsonTokens]]: supplies the closing-brace token constant
