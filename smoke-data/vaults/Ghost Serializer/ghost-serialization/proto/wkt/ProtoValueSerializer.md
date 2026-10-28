---
tags: [ghost-serializer, serialization, proto, wkt, serializer, struct]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoValueSerializer.kt
kind: object
---

# ProtoValueSerializer

The serializer object for `ProtoValue`: it decodes arbitrary JSON into the sealed variants and writes them back.

## What it is about

`google.protobuf.Value` is JSON itself, so this serializer is essentially a small JSON tree reader and writer. It is also the recursive core of [[ProtoStructSerializer]], which calls it for each map entry. It is registered for [[ProtoValue]] in [[GhostSerializerLookup]].

## How it works

It implements the three reading channels of Ghost (the streaming [[GhostJsonReader]], the byte-array [[GhostJsonFlatReader]] and the string-based [[GhostJsonStringReader]]) and the two writers ([[GhostJsonWriter]] for bytes and [[GhostJsonStringWriter]] for strings). The overloads are near copies of each other because each channel has its own reader class with no shared interface the compiler can inline across.

Decoding first checks `isNextNullValue()` and returns `ProtoValue.Null`. Otherwise it peeks the next token and dispatches: a quote gives `Str`, `t` or `f` gives `Bool`, `[` reads a list by calling `deserialize` recursively through `readList`, `{` reads a map with string keys through `readMap`, and anything else is read as a `Number` using `nextDouble()`. Encoding is a `when` over the sealed class, writing `nullValue()`, a number, text, a boolean, or an object or array whose children are written recursively.

The dispatch on the first byte avoids backtracking, and because `when` over a sealed class is exhaustive, adding a variant forces an update here.

## The code

```kotlin
    override fun deserialize(reader: GhostJsonFlatReader): ProtoValue {
        if (reader.isNextNullValue()) {
            reader.consumeNull()
            return ProtoValue.Null
        }
        val token = reader.peekNextToken()
        return when (token) {
            TOK.QUOTE_INT -> ProtoValue.Str(value = reader.nextString())
            TOK.TRUE_CHAR_INT, TOK.FALSE_CHAR_INT -> ProtoValue.Bool(value = reader.nextBoolean())
            TOK.OPEN_ARR_INT -> ProtoValue.List(value = reader.readList { deserialize(reader = reader) })
            TOK.OPEN_OBJ_INT -> ProtoValue.Struct(
                value = reader.readMap(
                    keyParser = { reader.nextString() },
                    valueParser = { deserialize(reader = reader) }
                )
```

Encoding:

```kotlin
    override fun serialize(writer: GhostJsonWriter, value: ProtoValue) {
        when (value) {
            is ProtoValue.Null -> writer.nullValue()
            is ProtoValue.Number -> writer.value(number = value.value)
            is ProtoValue.Str -> writer.value(text = value.value)
            is ProtoValue.Bool -> writer.value(value = value.value)
            is ProtoValue.Struct -> {
                writer.beginObject()
                for ((mapKey, mapValue) in value.value) {
                    writer.name(key = mapKey)
                    serialize(writer = writer, value = mapValue)
                }
                writer.endObject()
            }
```

## Related

- [[ProtoValue]]: the sealed model this serializer handles
- [[ProtoStructSerializer]]: calls this serializer for every map value
- [[ProtoStruct]]: the map alias built from these values
- [[AbstractGhostSerializer]]: the base class whose overloads are overridden here
- [[GhostSerializerLookup]]: returns this serializer for the type
- [[GhostJsonTokens]]: supplies the token constants used for dispatch
