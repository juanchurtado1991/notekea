---
tags: [ghost-serializer, spring-boot-starter, auto-configuration, webflux]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostWebFluxAutoConfiguration.kt
kind: class
---

# GhostWebFluxAutoConfiguration

The auto-configuration that registers Ghost's reactive encoders and decoders in a Spring WebFlux application.

## What it is about

WebFlux does not use `HttpMessageConverter`; it uses codecs that work on streams of `DataBuffer`. This class installs Ghost's four codecs for reactive Boot applications, so annotated models are converted without Jackson. It is part of `ghost-spring-boot-starter` and appears in the auto-configuration imports file.

## How it works

The conditions are an `@AutoConfiguration` that needs both `Ghost` and `WebFluxConfigurer` on the classpath, applying only to a `REACTIVE` web application, with `proxyBeanMethods = false`. It implements `WebFluxConfigurer`, and in `configureHttpMessageCodecs` it takes the server codec configurer's `customCodecs()` registry and registers:

1. [[GhostReactiveDecoder]] for JSON and NDJSON input,
2. [[GhostReactiveEncoder]] for JSON and NDJSON output,
3. [[GhostYamlReactiveDecoder]] for YAML input,
4. [[GhostYamlReactiveEncoder]] for YAML output.

Custom codecs are consulted before the defaults, but each codec only accepts a type when a Ghost serializer exists for it, so other types still go to Jackson. Unlike the MVC configuration, there is no advice bean, so strict and coerce annotations are not applied here.

Registering encoder and decoder as a pair for each format keeps the reactive stack symmetric with the servlet one: whatever the application can read in a format, it can also write. The classes are instantiated with their default registry, the global `Ghost`, so no extra bean wiring is required.

## The code

```kotlin
override fun configureHttpMessageCodecs(configurer: ServerCodecConfigurer) {
    with(configurer.customCodecs()) {
        register(GhostReactiveDecoder())
        register(GhostReactiveEncoder())
        register(GhostYamlReactiveDecoder())
        register(GhostYamlReactiveEncoder())
    }
}
```

## Related

- [[GhostReactiveDecoder]]: registered JSON decoder.
- [[GhostReactiveEncoder]]: registered JSON encoder.
- [[GhostYamlReactiveDecoder]]: registered YAML decoder.
- [[GhostYamlReactiveEncoder]]: registered YAML encoder.
- [[GhostWebMvcAutoConfiguration]]: the servlet-stack counterpart.
- [[GhostAutoConfiguration]]: the umbrella marker.
