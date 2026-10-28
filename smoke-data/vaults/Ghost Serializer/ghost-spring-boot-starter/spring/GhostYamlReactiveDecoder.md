---
tags: [ghost-serializer, spring-boot-starter, webflux, yaml]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostYamlReactiveDecoder.kt
kind: class
---

# GhostYamlReactiveDecoder

The WebFlux `Decoder` that reads YAML request bodies into Ghost models.

## What it is about

It is the YAML version of [[GhostReactiveDecoder]] for reactive Spring applications, in `ghost-spring-boot-starter`. It accepts `application/yaml`, `application/x-yaml` and `text/yaml`, and only for types that have a YAML-capable Ghost serializer.

## How it works

`canDecode` checks the MIME type with the superclass and also requires `getYamlSerializer(elementType) != null`, which also covers list, set and map element types. Both `decode` and `decodeToMono` use the same `decodeJoined` path: `DataBufferUtils.join` merges the buffer stream into one buffer, `consumeToByteArray` copies it out and releases it, and `asUtf8From` converts the bytes if the media type declared a different charset.

There is no streaming mode, because YAML has no widely used record-delimited format like NDJSON; a document is always decoded as a whole.

`deserializeBytes` looks up the YAML serializer, casts it to `GhostYamlSerializer<Any>` and decodes with `ghostYamlInternalUseFlatReader`. Any failure, including a missing serializer, is wrapped in a `GhostJsonException` with the message "Failed to decode reactive YAML buffer for" followed by the type. Unlike the JSON decoder, no proto handling is needed.

## The code

```kotlin
private fun decodeJoined(
    inputStream: Publisher<DataBuffer>,
    elementType: ResolvableType,
    charset: Charset?
): Flux<Any> = DataBufferUtils.join(inputStream).flatMapMany { buffer ->
    Flux.just(
        deserializeBytes(
            bytes = buffer.consumeToByteArray().asUtf8From(charset = charset),
            elementType = elementType
        )
    )
}
```

## Related

- [[GhostReactiveDecoder]]: the JSON and NDJSON decoder.
- [[GhostYamlReactiveEncoder]]: the matching YAML encoder.
- [[GhostSpringTypeSerializers]]: provides the YAML serializer lookup.
- [[GhostSpringDataBuffers]]: buffer and charset helpers.
- [[GhostSpringMediaTypes]]: the YAML `MimeType` constants.
- [[GhostWebFluxAutoConfiguration]]: registers it.
- [[GhostYamlSerializer]]: the contract it requires.
- [[GhostJsonException]]: the exception type it wraps failures in.
