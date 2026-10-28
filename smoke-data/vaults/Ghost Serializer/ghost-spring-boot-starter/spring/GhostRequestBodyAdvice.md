---
tags: [ghost-serializer, spring-boot-starter, mvc, annotations]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostRequestBodyAdvice.kt
kind: class
---

# GhostRequestBodyAdvice

A Spring `@ControllerAdvice` that turns the `@GhostStrict` and `@GhostCoerce` annotations into per-request reader settings.

## What it is about

The Ghost converters decide how to read a body, but the annotations live on controller methods or parameters, where a converter cannot see them. This advice bridges that gap: just before Spring reads a request body, it checks the annotations and records the result for the converter. It belongs to the servlet (MVC) part of `ghost-spring-boot-starter`.

## How it works

`RequestBodyAdvice` hooks run around body reading, and this class uses three of them.

1. `supports` limits the advice to the Ghost converters: it returns true only when the converter class is [[GhostHttpMessageConverter]] or [[GhostYamlHttpMessageConverter]], so bodies handled by Jackson or others are untouched.
2. `beforeBodyRead` sets two thread-local flags in [[GhostSpringConfig]]: strict if `@GhostStrict` is found, and coerce if `@GhostCoerce` is found. The search covers the parameter itself, its method and the declaring controller class (`isAnnotatedAnywhere`).
3. `afterBodyRead` and `handleEmptyBody` remove both thread-locals, so a flag can never leak into the next request handled on a pooled thread.

The converter reads those flags while it deserializes, between the before and after hooks.

## The code

```kotlin
override fun beforeBodyRead(
    inputMessage: HttpInputMessage,
    parameter: MethodParameter,
    targetType: Type,
    converterType: Class<out HttpMessageConverter<*>>
): HttpInputMessage {
    GhostSpringConfig.strict.set(parameter.isAnnotatedAnywhere(annotation = GhostStrict::class.java))
    GhostSpringConfig.coerce.set(parameter.isAnnotatedAnywhere(annotation = GhostCoerce::class.java))

    return inputMessage
}
```

## Related

- [[GhostSpringConfig]]: the thread-locals it sets and clears.
- [[GhostStrict]]: annotation that switches strict parsing on.
- [[GhostCoerce]]: annotation that switches loose coercion on.
- [[GhostHttpMessageConverter]]: reads the flags during JSON decoding.
- [[GhostYamlHttpMessageConverter]]: reads them for YAML.
- [[GhostWebMvcAutoConfiguration]]: declares this advice as a bean.
