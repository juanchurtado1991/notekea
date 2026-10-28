---
tags: [ghost-serializer, spring-boot-starter, webflux, yaml]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostYamlReactiveEncoder.kt
kind: class
---

# GhostYamlReactiveEncoder

The WebFlux `Encoder` that writes Ghost models as YAML response bodies.

## What it is about

When a reactive endpoint produces YAML (`application/yaml`, `application/x-yaml` or `text/yaml`), this encoder converts each element of the returned `Mono` or `Flux` using Ghost's YAML serializer. It is the YAML sibling of [[GhostReactiveEncoder]] and the counterpart of [[GhostYamlReactiveDecoder]], inside `ghost-spring-boot-starter`.

## How it works

`canEncode` requires both the media type check of the base class and a YAML-capable serializer for the declared element type. `encode` looks up that declared serializer once, then maps every element with `encodeValue`.

`encodeValue` follows a fixed fallback chain. It uses the declared serializer if there is one, otherwise the registry's serializer for the element's runtime class, and if neither exists it throws an `IllegalArgumentException` with Ghost's "not found" message. Then it verifies that the serializer implements `GhostYamlSerializer`, with a descriptive error otherwise, because a model can have only a JSON serializer. Finally `ghostYamlEncodeToBytes` produces the bytes and the `DataBufferFactory` wraps them.

Each element becomes its own buffer, with no separator logic: the streaming concerns of NDJSON in the JSON encoder have no YAML equivalent here.

## The code

```kotlin
if (serializer !is GhostYamlSerializer<*>) {
    throw IllegalArgumentException(
        "Serializer for ${value::class.simpleName} does not implement GhostYamlSerializer"
    )
}

@Suppress("UNCHECKED_CAST")
val yamlSerializer = serializer as GhostYamlSerializer<Any>
val encoded = ghostYamlEncodeToBytes(
    serializer = yamlSerializer,
    value = value
)
return bufferFactory.wrap(encoded)
```

## Related

- [[GhostYamlReactiveDecoder]]: the matching decoder.
- [[GhostReactiveEncoder]]: the JSON and NDJSON encoder.
- [[GhostSpringTypeSerializers]]: resolves YAML serializers from the element type.
- [[GhostSpringMediaTypes]]: the YAML `MimeType` constants.
- [[GhostWebFluxAutoConfiguration]]: registers it.
- [[GhostYamlSerializer]]: the contract checked before encoding.
- [[GhostYamlSerialization]]: annotation of the models.
