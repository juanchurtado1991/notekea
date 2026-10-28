---
tags: [ghost-serializer, spring-boot-starter, mvc, json]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostHttpMessageConverter.kt
kind: class
---

# GhostHttpMessageConverter

The Spring MVC `HttpMessageConverter` that reads and writes JSON request and response bodies with Ghost.

## What it is about

In Spring MVC (servlet stack), converters turn HTTP bodies into controller arguments and return values. This one replaces Jackson for models that have a Ghost serializer, so controllers get reflection-free JSON handling. It is the central class of `ghost-spring-boot-starter` and is registered by [[GhostWebMvcAutoConfiguration]].

## How it works

It extends `AbstractGenericHttpMessageConverter<Any>` for `application/json` and `*+json`. A `GhostSpringTypeSerializers` resolves the declared Java `Type`, so `List`, `Set` and `Map` bodies work. The converter says it can read or write a type only when a serializer exists, so Spring falls through to other converters for everything else.

- **Read:** the whole body is read into a byte array and converted to UTF-8 when the request declared another charset. `deserialize` reads the strict and coerce flags from `GhostSpringConfig` thread-locals, which [[GhostRequestBodyAdvice]] set from annotations. If `serializer.isProto` is true, it uses the proto3-lenient flat reader; otherwise the normal one. It applies the options to the reader and decodes.
- **Write:** `writeInternal` picks the serializer (declared type first, then the runtime class, then the registry, else `HttpMessageNotWritableException`) and writes through a helper to the output stream, then flushes.

Missing serializers raise `HttpMessageNotReadableException` with Ghost's "not found" text.

## The code

```kotlin
if (serializer.isProto) {
    return ghostProtoInternalUseFlatReader(bytes = bytes) { reader ->
        reader.applyOptions(isStrict = isStrict, isCoerce = isCoerce)
        serializer.deserialize(reader)
    }
}

return ghostInternalUseFlatReader(bytes = bytes) { reader ->
    reader.applyOptions(isStrict = isStrict, isCoerce = isCoerce)
    serializer.deserialize(reader)
}
```

The code notes that the proto annotation has binary retention, so the serializer's `isProto` flag is the runtime signal.

## Related

- [[GhostSpringTypeSerializers]]: resolves types to serializers.
- [[GhostSpringConfig]]: holds the per-request strict and coerce flags.
- [[GhostRequestBodyAdvice]]: sets those flags and only supports this converter and the YAML one.
- [[GhostWebMvcAutoConfiguration]]: registers it first in the converter list.
- [[GhostSpringDataBuffers]]: related buffer helpers (the charset helper `asUtf8From` is defined there).
- [[GhostSpringMediaTypes]]: provides the `*+json` media type.
- [[GhostYamlHttpMessageConverter]]: YAML sibling.
- [[GhostRegistry]]: the injectable lookup.
