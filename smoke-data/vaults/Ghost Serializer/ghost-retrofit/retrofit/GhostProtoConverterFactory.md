---
tags: [ghost-serializer, retrofit, converter, proto]
source: ghost-retrofit/src/main/kotlin/com/ghost/serialization/retrofit/GhostProtoConverterFactory.kt
kind: class
---

# GhostProtoConverterFactory

The Retrofit converter factory for proto3 JSON, used with `@GhostProtoSerialization` models.

## What it is about

Proto3 JSON has its own leniency rules, and real protobuf libraries produce output that plain JSON parsers may reject. This factory lets a Retrofit client talk to such services. It is in `ghost-retrofit` and differs from [[GhostConverterFactory]] only on the read path.

## How it works

The structure mirrors the JSON factory: a private constructor, `create(registry)` with the global `Ghost` as default, and a `GhostReflectTypeSerializers` that resolves the Retrofit `Type` to a serializer, also unwrapping `List<T>`, `Set<T>` and `Map<String, V>` when the element serializer is registered. If no serializer exists, it returns `null`.

For **requests**, it encodes with `Ghost.encodeToBytes` and sends `application/json; charset=UTF-8`; proto3 rules (lowerCamelCase, omitted defaults, quoted 64-bit values) are already generated into the serializer's own `serialize()`.

For **responses**, bytes are read into a pooled buffer and parsed with `ghostProtoInternalUseFlatReader`, which provides the proto3-aware reader. It accepts quoted or bare int64 and uint64, lenient int32 and quoted `"NaN"` and `"Infinity"`. A JSON `null` body returns `null`. Unlike the JSON factory, this one does not read `@GhostStrict` or `@GhostCoerce`.

## The code

```kotlin
ghostProtoInternalUseFlatReader(bytes = scratch, length = offset) { reader ->
    if (reader.isNextNullValue()) {
        reader.consumeNull()
        null
    } else {
        serializer.deserialize(reader)
    }
}
```

## Related

- [[GhostConverterFactory]]: the plain JSON sibling this one is modelled on.
- [[GhostYamlConverterFactory]]: the YAML sibling.
- [[GhostRetrofitBuffers]]: shared response-reading loop.
- [[GhostRetrofitMediaTypes]]: media type strings.
- [[GhostProtoJsonFlatReader]]: the lenient reader that does the parsing.
- [[GhostProtoSerialization]]: annotation of the models it handles.
- [[GhostReflectTypeSerializers]]: type-to-serializer resolution.
