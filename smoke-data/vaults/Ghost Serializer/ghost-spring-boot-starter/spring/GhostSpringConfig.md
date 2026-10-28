---
tags: [ghost-serializer, spring-boot-starter, thread-local, config]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostSpringConfig.kt
kind: object
---

# GhostSpringConfig

An internal object holding two thread-local flags that carry reader options from the request advice to the converters.

## What it is about

Spring runs `RequestBodyAdvice` and the `HttpMessageConverter` separately, with no shared context object between them. Ghost needs to pass two booleans, "strict" and "coerce", from one to the other for the current request. `GhostSpringConfig` is that channel. It is the smallest file in `ghost-spring-boot-starter`.

## How it works

Each flag is a `ThreadLocal<Boolean>` created with `withInitial { false }`, so a thread that never set a value reads `false`. In the servlet stack a request is handled on one thread from start to finish, so thread-locals are a safe way to share data between the advice (which writes) and the converter (which reads) within the same body read.

The lifecycle is strict. The advice sets the values in `beforeBodyRead` and removes them in `afterBodyRead` or `handleEmptyBody`. Removing rather than setting to false matters, because servlet threads are reused and a stale value would otherwise affect the next request.

This approach works for MVC only. The reactive decoders in this module do not use it and decode with default options.

## The code

```kotlin
internal object GhostSpringConfig {
    val strict = ThreadLocal.withInitial { false }
    val coerce = ThreadLocal.withInitial { false }
}
```

## Related

- [[GhostRequestBodyAdvice]]: sets and clears the flags around each body read.
- [[GhostHttpMessageConverter]]: reads the flags when decoding JSON.
- [[GhostYamlHttpMessageConverter]]: reads them when decoding YAML.
- [[GhostStrict]]: the annotation behind the `strict` flag.
- [[GhostCoerce]]: the annotation behind the `coerce` flag.
