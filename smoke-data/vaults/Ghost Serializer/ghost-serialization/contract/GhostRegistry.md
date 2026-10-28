---
tags: [ghost-serializer, serialization, contract, registry]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/contract/GhostRegistry.kt
kind: interface
---

# GhostRegistry

The interface for a module's collection of serializers, generated per module by the compiler plugin.

## What it is about

A registry maps classes to serializers without reflection. The Ghost compiler generates one per module (named `GhostModuleRegistry_...`), `Ghost` discovers or is given the registries, and `Ghost` itself also implements the interface to present an aggregate view. HTTP integrations depend on the interface rather than on the singleton, which makes them testable with a fake.

## How it works

Four functions, all abstract on purpose, so each implementation states its behavior explicitly: `getSerializer(clazz)` returns the serializer or `null` if this module does not know the class; `getAllSerializers()` returns the whole map, for eager loading; `prewarm()` eagerly initializes entries to avoid first-use warm-up; and `registeredCount()` reports how many serializers there are. Anyone who wants the common defaults extends `AbstractGhostRegistry`.

## The code

```kotlin
interface GhostRegistry {
    fun getAllSerializers(): Map<KClass<*>, GhostSerializer<*>>

    fun <T : Any> getSerializer(clazz: KClass<T>): GhostSerializer<T>?

    fun prewarm()

    fun registeredCount(): Int
}
```

## Related

- [[AbstractGhostRegistry]]: base class with defaults for three of the functions
- [[Ghost]]: implements this interface and searches every registry
- [[GhostSerializer]]: the type each registry returns
- [[GhostModuleRegistryGenerator]]: compiler component that generates registries
- [[GhostReflectTypeSerializers]]: HTTP helper that resolves types through a registry
