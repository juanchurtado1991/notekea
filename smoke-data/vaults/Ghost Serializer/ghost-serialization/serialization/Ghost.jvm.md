---
tags: [ghost-serializer, serialization, platform, jvm, registry]
source: ghost-serialization/src/jvmMain/kotlin/com/ghost/serialization/Ghost.jvm.kt
kind: expect/actual
---

# Ghost.jvm

The plain-JVM actual of `discoverRegistries`, which finds generated registries by class name and then through `ServiceLoader`.

## What it is about

`Ghost` needs to find the `GhostRegistry` objects the compiler plugin generated for each module. On the JVM this happens lazily on the first lookup. This file builds an iterator that checks the well-known generated classes first (cheap) and only then pays for `ServiceLoader`.

## How it works

On the first `hasNext`, the iterator tries two class names, `Ghost.DEFAULT_REGISTRY_NAME` and `Ghost.TEST_REGISTRY_NAME`: `Class.forName(name).getField(INSTANCE).get(null)` cast to `GhostRegistry`, with every failure turned into `null` by `runCatching` and removed by `mapNotNull`. When the fast list is exhausted, `ServiceLoader.load(GhostRegistry::class.java)` is created once and iterated; if loading fails, an empty iterator is used.

Compared with Android, the fast names are `Default` and `Default_Test`; the Android file uses `Default` and the module-named registry.

## The code

```kotlin
val names = listOf(
    Ghost.DEFAULT_REGISTRY_NAME,
    Ghost.TEST_REGISTRY_NAME
)
val registries = names.mapNotNull { name ->
    runCatching {
        Class.forName(name)
            .getField(Ghost.INSTANCE_FIELD)
            .get(null) as GhostRegistry
    }.getOrNull()
}.toMutableList()
```

`next()` returns from the fast iterator first, then from the `ServiceLoader` iterator, and throws `NoSuchElementException` if `hasNext` is false.

## Related

- [[Ghost]]: declares `discoverRegistries` and the registry name constants
- [[Ghost.android]]: Android version with a different set of fast names
- [[GhostRegistry]]: the interface being discovered
- [[GhostSerializerLookup]]: calls `discoverRegistries` when no manual registry matches
