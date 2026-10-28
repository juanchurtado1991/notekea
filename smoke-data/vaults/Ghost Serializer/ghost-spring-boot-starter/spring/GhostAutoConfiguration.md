---
tags: [ghost-serializer, spring-boot-starter, auto-configuration]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostAutoConfiguration.kt
kind: class
---

# GhostAutoConfiguration

A marker auto-configuration class that announces Ghost's presence to Spring Boot.

## What it is about

Spring Boot starters work by auto-configuration: classes listed in a resource file are loaded automatically when their conditions hold. `GhostAutoConfiguration` is the umbrella entry of Ghost's starter. It has no beans and no logic; it only exists as the base entry, while the real wiring lives in [[GhostWebMvcAutoConfiguration]] and [[GhostWebFluxAutoConfiguration]].

## How it works

The class is annotated with `@AutoConfiguration` and `@ConditionalOnClass(Ghost::class)`, so it is active only when the Ghost runtime is on the classpath. Boot finds it through the file `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, which lists three classes: this one, the MVC configuration and the WebFlux configuration.

Because each web configuration declares its own conditions (servlet or reactive application, and the presence of the matching Spring web classes), an application only activates the part that fits its stack. Adding the starter dependency is therefore enough, with no `@Import` and no manual bean definitions.

The KDoc of the class points to the two web configurations as the stack wiring.

Being a marker also gives users a stable name to exclude if they ever need to switch Ghost off for an application, using Spring Boot's regular `exclude` mechanism on the auto-configuration list, without touching the individual web configurations.

## The code

```kotlin
@AutoConfiguration
@ConditionalOnClass(Ghost::class)
class GhostAutoConfiguration
```

The empty body is intentional.

## Related

- [[GhostWebMvcAutoConfiguration]]: registers the servlet converters.
- [[GhostWebFluxAutoConfiguration]]: registers the reactive codecs.
- [[Ghost]]: the class whose presence triggers the condition.
- [[GhostHttpMessageConverter]]: one of the components wired by the dependent configurations.
