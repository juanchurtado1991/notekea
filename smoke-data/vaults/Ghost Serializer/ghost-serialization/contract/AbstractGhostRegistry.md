---
tags: [ghost-serializer, serialization, contract, registry]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/contract/AbstractGhostRegistry.kt
kind: class
---

# AbstractGhostRegistry

A skeletal `GhostRegistry` that supplies defaults for everything except `getSerializer`.

## What it is about

Most registries only need to implement the lookup. This base class removes the boilerplate for the other three functions, so a simple or hand-written registry is a few lines long.

## How it works

`getAllSerializers()` returns an empty map, `prewarm()` does nothing, and `registeredCount()` is computed from `getAllSerializers().size`. A registry that does hold serializers should override `getAllSerializers()`, and `registeredCount()` then becomes correct automatically.

Using a derived `registeredCount()` means the count can never disagree with the map returned by `getAllSerializers()`, which is the one place where an implementation lists its serializers.

## The code

```kotlin
abstract class AbstractGhostRegistry : GhostRegistry {

    override fun getAllSerializers(): Map<KClass<*>, GhostSerializer<*>> = emptyMap()

    override fun prewarm() {}

    override fun registeredCount(): Int = getAllSerializers().size
}
```

## Related

- [[GhostRegistry]]: the interface it partly implements
- [[GhostSerializer]]: the type in the map
- [[Ghost]]: a consumer of registries
