---
tags: [ghost-serializer, ktor, content-negotiation, configuration]
source: ghost-ktor/src/commonMain/kotlin/com/ghost/serialization/ktor/GhostKtorExtensions.kt
kind: functions
---

# GhostKtorExtensions

The three extension functions that register Ghost's converters in Ktor's ContentNegotiation.

## What it is about

This is the file a Ktor user touches first. Inside `install(ContentNegotiation) { ... }` you write `ghost()`, `ghostProto()` or `ghostYaml()`, and the matching converter is registered. All three are extensions on `io.ktor.serialization.Configuration`, in the `ghost-ktor` module, and they work on the client and server alike.

## How it works

Each function builds a converter and calls `register(contentType, converter)`. They share three parameters:

- `contentType`: the media type to bind. JSON and proto3 JSON default to `ContentType.Application.Json`, while YAML defaults to `application/yaml` from [[GhostKtorMediaTypes]].
- `configurer`: an optional lambda that receives the freshly created reader for each request, with the reader type matching the format ([[GhostJsonFlatReader]], [[GhostProtoJsonFlatReader]] or [[GhostYamlFlatReader]]). It can switch on strict or coercing options per call.
- `registry`: the lookup used for plain classes, defaulting to the `Ghost` singleton, which tests can replace.

Because JSON and proto3 JSON use the same media type, a given Ktor setup should install only one of them for that type.

## The code

```kotlin
fun Configuration.ghost(
    contentType: ContentType = ContentType.Application.Json,
    configurer: ((GhostJsonFlatReader) -> Unit)? = null,
    registry: GhostRegistry = Ghost
) {
    register(contentType, GhostContentConverter(configurer = configurer, registry = registry))
}
```

`ghostProto` and `ghostYaml` have the same shape with their own converter and reader types.

## Related

- [[GhostContentConverter]]: what `ghost()` registers.
- [[GhostProtoContentConverter]]: what `ghostProto()` registers.
- [[GhostYamlContentConverter]]: what `ghostYaml()` registers.
- [[GhostKtorMediaTypes]]: provides the YAML default content type.
- [[GhostRegistry]]: the type of the `registry` parameter.
- [[Ghost]]: the default registry.
