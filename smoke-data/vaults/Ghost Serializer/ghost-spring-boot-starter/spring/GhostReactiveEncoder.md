---
tags: [ghost-serializer, spring-boot-starter, webflux, json, ndjson]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostReactiveEncoder.kt
kind: class
---

# GhostReactiveEncoder

The WebFlux `Encoder` that writes Ghost models as JSON or NDJSON response bodies.

## What it is about

When a reactive controller returns a `Mono` or `Flux` of a Ghost model, Spring needs an encoder to turn each element into `DataBuffer`s. This class is that encoder in `ghost-spring-boot-starter`, the output counterpart of [[GhostReactiveDecoder]].

## How it works

It extends `AbstractEncoder<Any>` for `application/json` and `application/x-ndjson`. `canEncode` requires a Ghost serializer for the element type, found through `GhostSpringTypeSerializers`, so declared generic types are respected.

`encode` resolves the declared serializer once for the whole stream, then maps every element with `encodeValue`:

1. Take the declared serializer; if absent, look up the element's runtime class in the registry; if still absent, throw an `IllegalArgumentException` with Ghost's "not found" text.
2. Encode to bytes with `Ghost.encodeToBytes`.
3. For plain JSON, wrap the bytes with the `DataBufferFactory` unchanged. For NDJSON, copy them into an array one byte longer and append the `\n` record separator, then wrap.

The comment in the class explains the choice of the declared serializer: for collection bodies it uses the declared generic serializer and not `value::class`, which would be the runtime collection class.

## The code

```kotlin
if (!isNdJson) return bufferFactory.wrap(encoded)

val bytes = ByteArray(encoded.size + 1)
encoded.copyInto(bytes)
bytes[encoded.size] = GhostSpringMediaTypes.NDJSON_NEWLINE
return bufferFactory.wrap(bytes)
```

## Related

- [[GhostReactiveDecoder]]: the matching decoder.
- [[GhostSpringTypeSerializers]]: resolves the serializer from the `ResolvableType`.
- [[GhostSpringMediaTypes]]: provides NDJSON detection and the newline byte.
- [[GhostWebFluxAutoConfiguration]]: registers it.
- [[GhostYamlReactiveEncoder]]: the YAML sibling.
- [[Ghost]]: provides `encodeToBytes`.
