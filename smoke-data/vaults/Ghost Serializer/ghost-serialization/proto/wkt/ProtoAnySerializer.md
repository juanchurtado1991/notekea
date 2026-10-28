---
tags: [ghost-serializer, serialization, proto, wkt, serializer, any]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoAnySerializer.kt
kind: object
---

# ProtoAnySerializer

The serializer object for `ProtoAny`: it reads `@type` and captures the `value` payload as raw JSON bytes.

## What it is about

In proto3 JSON an `Any` is an object with an `@type` URL and the packed message's content. This serializer handles the two keys Ghost cares about, `@type` and `value`, and ignores the rest. It does not interpret the payload; [[ProtoAnyRegistry]] can do that later. [[GhostSerializerLookup]] registers it for [[ProtoAny]].

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

Decoding opens an object and loops over its keys. `@type` is read as a string; `value` is captured with `captureRawJsonBytes()`, which copies the raw JSON text of that value without parsing it; any other key is skipped. The result is a [[ProtoAny]] with the type URL and the captured bytes. Writing emits `@type` and, only when the payload is not empty, a `value` key followed by `rawValue(bytes)`, which inserts the stored JSON verbatim.

Capturing raw bytes means a nested message goes through the parser once and is never converted to an intermediate tree.

## The code

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoAny {
        reader.beginObject()
        var typeUrl = ""
        var payload = TOK.EMPTY_BYTES
        while (reader.peekNextToken() != TOK.CLOSE_OBJ_INT) {
            val key = reader.nextString()
            reader.consumeKeySeparator()
            when (key) {
                PC.PROTO_TYPE_URL_KEY -> typeUrl = reader.nextString()
                PC.PROTO_VALUE_KEY -> payload = reader.captureRawJsonBytes()
                else -> reader.skipValue()
            }
            if (reader.peekNextToken() == TOK.COMMA_INT) {
                reader.consumeArraySeparator()
```

Encoding:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoAny) {
        writer.beginObject()
        writer.name(key = PC.PROTO_TYPE_URL_KEY).value(text = value.typeUrl)
        if (value.value.isNotEmpty()) {
            writer.name(key = PC.PROTO_VALUE_KEY)
            writer.rawValue(bytes = value.value)
        }
        writer.endObject()
```

## Related

- [[ProtoAny]]: the model class
- [[ProtoAnyRegistry]]: packs and unpacks the captured payload into typed messages
- [[GhostProtoConstants]]: defines the `@type` and `value` key names
- [[AbstractGhostSerializer]]: the base class whose overloads are overridden here
- [[GhostSerializerLookup]]: returns this serializer for the type
