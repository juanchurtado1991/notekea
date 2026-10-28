---
tags: [ghost-serializer, ktor, content-negotiation, proto]
source: ghost-ktor/src/commonMain/kotlin/com/ghost/serialization/ktor/GhostProtoContentConverter.kt
kind: class
---

# GhostProtoContentConverter

The Ktor content converter for the proto3 JSON mapping, used with `@GhostProtoSerialization` models.

## What it is about

APIs that follow the proto3 JSON rules differ from plain JSON in small ways: 64-bit integers may be quoted or bare, 32-bit integers may be lenient, and special numbers appear as `"NaN"` or `"Infinity"`. This converter handles bodies of that kind in Ktor. It is part of `ghost-ktor` and mirrors [[GhostContentConverter]] closely.

## How it works

On the read side, it resolves the serializer, then reads the body into a pooled scratch buffer with `GhostKtorBuffers.readToScratch` and parses it with `ghostProtoInternalUseFlatReader`, which supplies a [[GhostProtoJsonFlatReader]] that is lenient in the proto3 ways above. The optional `configurer` can tweak that reader before decoding.

Unlike the JSON converter, it does not special-case an empty body: the reader runs on whatever length was read.

On the write side nothing proto-specific is done by the converter. It calls the shared `encodeKtorContent`, which uses `Ghost.encodeToBytes`, because the generated serializer's own `serialize()` already writes proto3-correct output (lowerCamelCase names, omitted defaults, quoted 64-bit values). The registry can be replaced for tests, with the same restriction as in the JSON converter: generic types always go through `Ghost`.

## The code

```kotlin
return GhostKtorBuffers.readToScratch(
    content = content,
    charset = charset
) { scratch, offset ->
    ghostProtoInternalUseFlatReader(bytes = scratch, length = offset) { reader ->
        configurer?.invoke(reader)
        serializer.deserialize(reader)
    }
}
```

Registration is done with `install(ContentNegotiation) { ghostProto() }`.

## Related

- [[GhostContentConverter]]: the plain JSON sibling.
- [[GhostProtoJsonFlatReader]]: the lenient proto3 reader.
- [[GhostKtorExtensions]]: defines `ghostProto()` that registers the converter.
- [[GhostKtorBuffers]]: the shared body-reading loop.
- [[GhostKtorSerializers]]: the shared lookup and encoding helpers.
- [[GhostProtoSerialization]]: the annotation of the models it handles.
- [[GhostProto]]: the runtime object for proto3 JSON.
