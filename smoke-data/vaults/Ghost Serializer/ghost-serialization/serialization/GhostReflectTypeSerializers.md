---
tags: [ghost-serializer, serialization, jvm, http, reflection]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/GhostReflectTypeSerializers.kt
kind: class
---

# GhostReflectTypeSerializers

A helper class that turns a `java.lang.reflect.Type` into a `GhostSerializer`, used by the Retrofit and Spring integrations.

## What it is about

HTTP libraries on the JVM describe the body type with Java reflection types (`Class`, `ParameterizedType`, `WildcardType`), while Ghost's registry is keyed by `KClass`. This class bridges the two. It resolves plain classes through a registry and builds serializers for top-level `List`, `Set` and `Map<String, V>` by wrapping the element serializer. Ktor does not use it, because Ktor resolves through `KType`.

## How it works

`get(type)` checks a `ConcurrentHashMap` cache, then calls `resolve`. `resolve` handles three cases: a `Class` goes to the registry (`resolveClass`); a wildcard such as `List<? extends Animal>` (which Kotlin emits for non-final element types, including `suspend` return types) is replaced by its lower or upper bound; and a `ParameterizedType` whose raw type is `List`, `Set` or `Map` resolves its element or value type recursively and wraps it. Maps are supported only with `String` keys.

In `yamlOnly` mode a serializer qualifies only if it implements `GhostYamlSerializer`, and the YAML collection wrappers are used instead. The optional `isExcludedTopLevel` predicate makes some classes resolve to `null` at the top level while remaining valid as elements. The cache only stores top-level results, using `putIfAbsent` so concurrent callers agree.

## The code

```kotlin
@InternalGhostApi
class GhostReflectTypeSerializers(
    private val registry: GhostRegistry,
    private val yamlOnly: Boolean = false,
    private val isExcludedTopLevel: (Class<*>) -> Boolean = { false }
) {
    private val cache = ConcurrentHashMap<Type, GhostSerializer<Any>>()

    fun get(type: Type): GhostSerializer<Any>? {
        cache[type]?.let { return it }
        val isExcluded = type is Class<*> && isExcludedTopLevel(type)
        val resolved = (if (isExcluded) null else resolve(type = type)) ?: return null
        return cache.putIfAbsent(type, resolved) ?: resolved
    }
```

The `wrapList`, `wrapSet` and `wrapMap` helpers pick `ListSerializer`, `SetSerializer`, `MapSerializer` or their YAML counterparts, and return `null` when a YAML wrapper is requested for an element that is not YAML capable.

## Related

- [[GhostRegistry]]: the registry used to resolve plain classes
- [[GhostConverterFactory]]: Retrofit converter that resolves body types with this class
- [[GhostSpringTypeSerializers]]: Spring integration that uses it
- [[GhostProtoConverterFactory]]: Retrofit protobuf converter that uses it
- [[ListSerializer]]: wrapper chosen for `List` types
- [[MapSerializer]]: wrapper chosen for `Map<String, V>` types
- [[GhostYamlListSerializer]]: YAML wrapper chosen when `yamlOnly` is on
