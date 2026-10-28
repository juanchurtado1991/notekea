---
tags: [ghost-serializer, spring-boot-starter, mvc, yaml]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostYamlHttpMessageConverter.kt
kind: class
---

# GhostYamlHttpMessageConverter

The Spring MVC converter for YAML bodies, for models that have a `GhostYamlSerializer`.

## What it is about

It lets a servlet-stack controller accept and return `application/yaml`, `application/x-yaml` or `text/yaml` with the same Ghost models used for JSON. It is the YAML sibling of [[GhostHttpMessageConverter]] in `ghost-spring-boot-starter`, registered by [[GhostWebMvcAutoConfiguration]].

## How it works

It extends `AbstractGenericHttpMessageConverter<Any>` with three YAML media types and resolves serializers with `GhostSpringTypeSerializers.getYamlSerializer`, which only returns serializers that support YAML (including list, set and map element types).

Two overrides handle content negotiation carefully. `canRead(mediaType)` returns false for a `null` media type, and `canWrite(mediaType)` returns false for a missing or wildcard media type. Without this, a request with `Accept: */*` could be answered with YAML, which would surprise clients expecting JSON. This is why the auto-configuration also puts the JSON converter ahead of it.

- **Read:** reads the body as bytes, converts to UTF-8 if needed, reads the strict and coerce flags from `GhostSpringConfig`, applies them to the reader and decodes with `ghostYamlInternalUseFlatReader`.
- **Write:** picks a serializer (declared type, then runtime class, then registry and only if it is a YAML serializer), encodes with `ghostYamlEncodeToBytes`, and writes the bytes in one call.

## The code

```kotlin
override fun canWrite(mediaType: MediaType?): Boolean {
    val isWildcardMediaType = mediaType == null || mediaType.isWildcardType || mediaType.isWildcardSubtype
    if (isWildcardMediaType) {
        return false
    }
    return super.canWrite(mediaType)
}
```

## Related

- [[GhostHttpMessageConverter]]: the JSON converter this mirrors.
- [[GhostSpringTypeSerializers]]: YAML-only serializer resolution.
- [[GhostSpringMediaTypes]]: defines the three YAML media types.
- [[GhostSpringConfig]]: holds the strict and coerce flags it reads.
- [[GhostWebMvcAutoConfiguration]]: registers it after the JSON converter.
- [[GhostYamlSerializer]]: the contract serializers must implement.
- [[GhostYamlSerialization]]: annotation of the models.
