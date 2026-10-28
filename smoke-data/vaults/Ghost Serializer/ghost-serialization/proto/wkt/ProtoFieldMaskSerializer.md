---
tags: [ghost-serializer, serialization, proto, wkt, serializer, field-mask]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoFieldMaskSerializer.kt
kind: object
---

# ProtoFieldMaskSerializer

The serializer object that maps `ProtoFieldMask` to and from a single comma-separated JSON string.

## What it is about

The proto3 JSON form of a FieldMask is one string with camelCase paths joined by commas. `ProtoFieldMaskSerializer` reads or writes that string and delegates the case conversion to [[ProtoFieldMaskCodec]]. [[GhostSerializerLookup]] registers it for [[ProtoFieldMask]].

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across. There is no YAML writer overload.

Decoding calls `nextString()` and passes the text to `parseFieldMask`, which splits on commas and turns camelCase into snake_case. Encoding calls `formatFieldMask`, which does the reverse, and writes the result as a string. Unlike the Timestamp and Duration serializers, it does not wrap the call in `parseWktString`, since the mask parser does not throw on malformed input.

## The code

```kotlin
    override fun deserialize(reader: GhostJsonReader): ProtoFieldMask {
        return parseFieldMask(pathsText = reader.nextString())
    }
```

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoFieldMask) {
        writer.value(text = formatFieldMask(mask = value))
    }
```

## Related

- [[ProtoFieldMask]]: the model class
- [[ProtoFieldMaskCodec]]: does the case conversion
- [[AbstractGhostSerializer]]: the base class whose overloads are overridden here
- [[GhostSerializerLookup]]: returns this serializer for the type
