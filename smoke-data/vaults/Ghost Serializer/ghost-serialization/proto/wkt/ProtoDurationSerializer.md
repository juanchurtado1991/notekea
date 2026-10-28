---
tags: [ghost-serializer, serialization, proto, wkt, serializer, duration]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoDurationSerializer.kt
kind: object
---

# ProtoDurationSerializer

The serializer object that maps `ProtoDuration` to and from its JSON string form such as `"3.5s"`.

## What it is about

Proto3 JSON writes a `Duration` as seconds with an optional fraction and an `s` suffix. `ProtoDurationSerializer` plugs the string functions of [[ProtoDurationCodec]] into Ghost's reader and writer API for [[ProtoDuration]]. [[GhostSerializerLookup]] returns it automatically.

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

Decoding reads a string with `nextString()` and runs `parseDuration` inside `parseWktString`, so validation errors such as a missing `s`, too many fraction digits or an out-of-range value are reported through `reader.throwError`. Encoding calls `formatDuration` and writes the text.

## The code

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoDuration {
        return parseWktString(
            text = reader.nextString(),
            throwError = { reader.throwError(message = it) }
        ) { parseDuration(durationString = it) }
    }
```

Encoding:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoDuration) {
        writer.value(text = formatDuration(duration = value))
    }
```

## Related

- [[ProtoDuration]]: the model class
- [[ProtoDurationCodec]]: parses and formats the text
- [[ProtoWktStringParsing]]: turns codec exceptions into reader errors
- [[AbstractGhostSerializer]]: the base class whose overloads are overridden here
- [[GhostSerializerLookup]]: returns this serializer for the type
