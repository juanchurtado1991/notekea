---
tags: [ghost-serializer, serialization, proto, wkt, serializer, timestamp]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoTimestampSerializer.kt
kind: object
---

# ProtoTimestampSerializer

The serializer object that maps `ProtoTimestamp` to and from an RFC 3339 JSON string.

## What it is about

`ProtoTimestampSerializer` handles the proto3 JSON form of `Timestamp`, a quoted date-time like `"1970-01-01T00:00:00Z"`. It is a thin adapter: the calendar and text work lives in [[ProtoTimestampCodec]], and this object wires it to Ghost's readers and writers. [[GhostSerializerLookup]] returns it for [[ProtoTimestamp]].

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

Each decode overload reads the quoted string with `nextString()` and passes it to `parseWktString`, with `parseTimestamp` as the parsing lambda and `reader.throwError` as the error callback. A malformed timestamp therefore becomes the channel's own decode error, with the JSON path attached. Encoding calls `formatTimestamp` and writes the result as a string.

## The code

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoTimestamp {
        return parseWktString(
            text = reader.nextString(),
            throwError = { reader.throwError(message = it) }
        ) { parseTimestamp(timestampString = it) }
    }
```

Encoding:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoTimestamp) {
        writer.value(text = formatTimestamp(timestamp = value))
    }
```

## Related

- [[ProtoTimestamp]]: the model class
- [[ProtoTimestampCodec]]: parses and formats the text
- [[ProtoWktStringParsing]]: turns codec exceptions into reader errors
- [[AbstractGhostSerializer]]: the base class whose overloads are overridden here
- [[GhostSerializerLookup]]: returns this serializer for the type
- [[GhostProtoConstants]]: provides the `typeName`
