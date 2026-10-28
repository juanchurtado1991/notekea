---
tags: [ghost-serializer, spring-boot-starter, serializer-lookup]
source: ghost-spring-boot-starter/src/main/kotlin/com/ghost/serialization/spring/GhostSpringTypeSerializers.kt
kind: class
---

# GhostSpringTypeSerializers

An internal class that resolves Ghost serializers from Java and Spring types for the Spring converters.

## What it is about

Spring tells a converter which type it needs as a `java.lang.reflect.Type` or a `ResolvableType`, often generic, such as `List<Order>`. Ghost's registry only knows classes. This class adapts one to the other for all the MVC converters and the reactive codecs of `ghost-spring-boot-starter`.

## How it works

It wraps two instances of the shared `GhostReflectTypeSerializers`, the same type traversal Retrofit uses: one for JSON and one with `yamlOnly = true`. Both unwrap top-level `List`, `Set` and `Map` and find serializers of the element types.

A custom exclusion rule, `isExcludedTopLevelType`, keeps Ghost away from bodies that Spring already handles well: top-level `String`, `ByteArray`, primitives and anything in `java.lang.`. They remain valid as element types, for example `List<String>`, but at top level the converter reports "not supported" so Spring's default converters read them. The class takes an injectable `registry` that defaults to the global `Ghost`.

Four public functions cover the combinations: `getJsonSerializer` and `getYamlSerializer`, each taking a `Type` or a `ResolvableType` (the latter simply forwards its `.type`).

## The code

```kotlin
private fun isExcludedTopLevelType(clazz: Class<*>): Boolean {
    return clazz == String::class.java ||
        clazz == ByteArray::class.java ||
        clazz.isPrimitive ||
        clazz.name.startsWith("java.lang.")
}
```

## Related

- [[GhostReflectTypeSerializers]]: the shared resolver it wraps.
- [[GhostHttpMessageConverter]]: uses the JSON lookups in MVC.
- [[GhostYamlHttpMessageConverter]]: uses the YAML lookups in MVC.
- [[GhostReactiveDecoder]]: uses the lookup with `ResolvableType` in WebFlux.
- [[GhostReactiveEncoder]]: same for encoding.
- [[GhostRegistry]]: the lookup contract passed in.
