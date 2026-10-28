---
tags: [ghost-serializer, spring-boot-starter, auto-configuration, mvc]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostWebMvcAutoConfiguration.kt
kind: class
---

# GhostWebMvcAutoConfiguration

The auto-configuration that plugs Ghost's converters into a Spring MVC (servlet) application.

## What it is about

For a classic servlet-based Spring Boot app, this class is what makes Ghost work without any setup. It adds the JSON and YAML message converters and registers the advice that reads `@GhostStrict` and `@GhostCoerce`. It belongs to `ghost-spring-boot-starter` and is listed in the Boot auto-configuration imports file.

## How it works

Three conditions guard it: it is an `@AutoConfiguration`, it needs both `Ghost` and `WebMvcConfigurer` on the classpath (`@ConditionalOnClass`), and the application must be a servlet web app (`@ConditionalOnWebApplication(type = SERVLET)`). `proxyBeanMethods` is false, which avoids creating a CGLIB proxy because the beans do not call each other.

The class implements `WebMvcConfigurer`. In `extendMessageConverters` it inserts the converters at the front of Spring's list: first the YAML converter at index 0, then the JSON converter at index 0, so the final order is JSON then YAML, ahead of the defaults. The comment explains why: the YAML converter must not win default `*/*` negotiation. Ghost's converters decline any type they have no serializer for, so Jackson still serves other types.

It also exposes a `@Bean` for [[GhostRequestBodyAdvice]].

## The code

```kotlin
override fun extendMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
    // JSON first — YAML converter must not win default */* negotiation.
    converters.add(0, GhostYamlHttpMessageConverter())
    converters.add(0, GhostHttpMessageConverter())
}
```

## Related

- [[GhostHttpMessageConverter]]: the JSON converter it registers first.
- [[GhostYamlHttpMessageConverter]]: the YAML converter it registers second.
- [[GhostRequestBodyAdvice]]: the bean it declares.
- [[GhostAutoConfiguration]]: the umbrella marker listed alongside it.
- [[GhostWebFluxAutoConfiguration]]: the reactive equivalent.
- [[Ghost]]: the class its condition checks for.
