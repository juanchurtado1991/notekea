---
tags: [ghost-serializer, serialization, types, raw-json, serializer]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/types/RawJsonSerializer.kt
kind: object
---

# RawJsonSerializer

The built-in serializer for `RawJson`, which passes opaque JSON through without parsing it.

## What it is about

Some payloads contain a field whose shape is unknown or irrelevant, and the application wants to forward or store it. `RawJson` represents that, and this serializer reads and writes it. `Ghost` returns it from its primitive fast path, so no registration is needed.

## How it works

Reading calls `captureRawJson()` on whichever reader is in use (streaming, flat or string), which scans over the next JSON value and records it, without building objects. Writing calls `rawValue(raw = value)` on the byte or string writer, which copies the stored bytes (the writer handles the comma and does not escape them). The class extends `AbstractGhostSerializer`, so the other channels use the bridges.

`warmUp` parses a tiny sample payload with a streaming reader and a flat reader inside `try` blocks that ignore exceptions, so the JIT sees those paths before real use.

## The code

```kotlin
object RawJsonSerializer : AbstractGhostSerializer<RawJson>() {

    override val typeName: String = TOK.TYPE_NAME_RAW_JSON

    override fun deserialize(reader: GhostJsonReader): RawJson =
        reader.captureRawJson()

    override fun serialize(writer: GhostJsonWriter, value: RawJson) {
        writer.rawValue(raw = value)
    }
    // …
}
```

## Related

- [[AbstractGhostSerializer]]: base class that provides the other channels
- [[RawJson]]: the value this serializer handles
- [[RawJsonDecode]]: decodes the captured value into a typed object
- [[GhostSerializerLookup]]: returns this serializer from `getPrimitiveSerializer`
- [[GhostJsonFlatReader]]: one of the readers that implements `captureRawJson`
- [[GhostJsonWriter]]: writes the stored bytes with `rawValue`
