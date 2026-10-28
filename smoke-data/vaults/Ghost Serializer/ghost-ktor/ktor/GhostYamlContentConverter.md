---
tags: [ghost-serializer, ktor, content-negotiation, yaml]
source: ghost-ktor/src/commonMain/kotlin/com/ghost/serialization/ktor/GhostYamlContentConverter.kt
kind: class
---

# GhostYamlContentConverter

The Ktor content converter for YAML bodies, backed by Ghost's YAML serializers.

## What it is about

It lets a Ktor client or server negotiate `application/yaml` and convert it to and from models that have a `GhostYamlSerializer`. It belongs to `ghost-ktor` and follows the same pattern as the JSON and proto converters, with YAML-specific readers and writers. Models need [[GhostYamlSerialization]] for it to work.

## How it works

For **deserialization** it resolves a serializer for the requested type. If none is found, or if the found one is not a `GhostYamlSerializer`, it returns `null`, which tells Ktor this converter cannot handle the type. Otherwise it fills a pooled scratch buffer from the channel, trims it to the exact length (copying only if the buffer is not already full), and runs `ghostYamlInternalUseFlatReader` over the bytes. A user `configurer` can adjust the [[GhostYamlFlatReader]] first.

For **serialization** the converter returns `null` for a null value, resolves the serializer using the value's runtime class as fallback, checks it is a YAML serializer, encodes with `ghostYamlEncodeToBytes`, and wraps the UTF-8 bytes through `ghostKtorBytesContent`, which re-encodes to the negotiated charset when it is not UTF-8.

## The code

```kotlin
if (serializer !is GhostYamlSerializer<*>) {
    return null
}

val yamlSerializer = serializer as GhostYamlSerializer<Any>
return GhostKtorBuffers.readToScratch(
    content = content,
    charset = charset
) { scratch, offset ->
    val bytesToParse = if (offset == scratch.size) scratch else scratch.copyOf(offset)
    ghostYamlInternalUseFlatReader(bytes = bytesToParse) { reader ->
        configurer?.invoke(reader)
        yamlSerializer.deserialize(reader)
    }
}
```

The copy exists because the YAML reader takes a byte array without a length parameter in this call. Registration is `install(ContentNegotiation) { ghostYaml() }`.

## Related

- [[GhostKtorExtensions]]: provides `ghostYaml()`, the registration function.
- [[GhostKtorMediaTypes]]: defines the `application/yaml` content type.
- [[GhostYamlSerializer]]: the serializer contract it requires.
- [[GhostYamlFlatReader]]: the reader used to parse.
- [[GhostKtorBuffers]]: body reading into a scratch buffer.
- [[GhostKtorSerializers]]: lookup and bytes-to-content helpers.
- [[GhostContentConverter]]: the JSON counterpart.
