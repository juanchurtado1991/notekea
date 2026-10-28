---
tags: [ghost-serializer, serialization, contract, serializer, bridge]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/contract/AbstractGhostSerializer.kt
kind: class
---

# AbstractGhostSerializer

A skeletal base class for `GhostSerializer` that fills in the channels a serializer does not implement itself.

## What it is about

The interface has seven read and write overloads, but a serializer is usually written, or generated, for only a couple of them. `AbstractGhostSerializer` implements the rest by converting to a channel that is implemented. Generated and hot-path serializers override the specific overloads for speed; simple or custom ones can inherit the bridges.

## How it works

Reading bridges between readers. `deserialize(BufferedSource)` reads all bytes and parses them with a flat reader. `deserialize(GhostJsonFlatReader)` builds a temporary streaming `GhostJsonReader` over the same buffer, copies position, limit, strictness, coercion flags and depth and collection limits into it, runs the streaming version, then copies the position back and resets the cached token byte. `deserialize(GhostJsonStringReader)` converts the string reader to UTF-8 bytes, translates the char position to a byte position, parses with a flat reader (with `materializeRawJsonCaptures` on) and maps the position back. The source warns that these bridges allocate and lose monomorphy.

Writing bridges: `serialize(BufferedSink)` encodes with the pooled byte writer and drains it to the sink; `serialize(GhostJsonStringWriter)` encodes to bytes and inserts them with `rawValue`. Defaults: `isResilient` and `isProto` are `false`, and `warmUp` does nothing.

## The code

```kotlin
override fun serialize(sink: BufferedSink, value: T) {
    ghostInternalEncodeAndDrainTo(sink = sink) { writer ->
        serialize(writer, value)
    }
}

override fun serialize(writer: GhostJsonStringWriter, value: T) {
    val bytes = ghostInternalEncodeWithWriter { flatWriter ->
        serialize(flatWriter, value)
    }
    writer.rawValue(bytes)
}
```

`deserialize(GhostJsonFlatReader)` sets `reader.nextTokenByte = SCN.RESET_TOKEN_BYTE` after delegating, so the flat reader does not reuse a stale peeked token. If a subclass implements none of the object overloads the delegation will loop, so at least one `deserialize` and `serialize` must be overridden.

## Related

- [[GhostSerializer]]: the interface it implements
- [[RawJsonSerializer]]: built-in serializer that extends it
- [[GhostJsonFlatReader]]: reader the bridge builds or reads from
- [[GhostJsonReader]]: streaming reader used as the delegate
- [[GhostJsonStringReader]]: string reader converted to bytes
- [[Ghost.jvmAndroid]]: provides the pooled encode helpers used by the write bridges
