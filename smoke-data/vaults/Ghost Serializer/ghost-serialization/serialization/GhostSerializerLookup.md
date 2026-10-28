---
tags: [ghost-serializer, serialization, registry, lookup]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/GhostSerializerLookup.kt
kind: functions
---

# GhostSerializerLookup

The lookup tables behind `Ghost.getSerializer`, split out of `Ghost.kt` into extension functions.

## What it is about

`Ghost.getSerializer` first tries cheap, fixed answers and only then searches registries. This file holds those steps: built-in primitive serializers, YAML primitive-array serializers, the protobuf well-known types, and the registry search that ends with the compiler plugin's link. None of them are called by name from outside `Ghost`, so they live in their own file.

## How it works

`getPrimitiveSerializer` is a `when` over `KClass` that returns the singleton serializer for `String`, the numeric types, the unsigned types, `Char`, `Boolean`, the primitive arrays and `RawJson`. `getYamlPrimitiveSerializer` is deliberately separate, so JSON resolution keeps returning the JSON array serializers while YAML gets its own.

`getWktSerializer` reads from a private `Map<KClass<*>, GhostSerializer<*>>`, so adding a protobuf well-known type means adding one entry. `ProtoStruct` has no entry, because it is a typealias for `Map<String, ProtoValue>` and erases to `Map`. `getSerializerFromRegistries` checks the manually added registries, then the lazily discovered ones (calling `discoverRegistries()` once and remembering the result), and finally `findLinkedSerializer`.

## The code

```kotlin
internal fun <T : Any> Ghost.getSerializerFromRegistries(clazz: KClass<T>): GhostSerializer<T>? {
    for (registry in mutableRegistries) {
        registry.getSerializer(clazz = clazz)?.let { return it }
    }

    val disc = discoveredRegistries ?: discoverRegistries().also { discoveredRegistries = it }

    for (registry in disc) {
        registry.getSerializer(clazz = clazz)?.let { return it }
    }

    @Suppress("UNCHECKED_CAST")
    return findLinkedSerializer(clazz = clazz) as GhostSerializer<T>?
}
```

## Related

- [[Ghost]]: owns the registries and caches these extensions read
- [[GhostRegistry]]: the interface searched in the registry loops
- [[GhostSerializerLinkLookup.native]]: supplies the compile-time link on Native
- [[GhostSerializerLinkLookup.jvmAndroid]]: JVM and Android version that returns `null`
- [[IntSerializer]]: example of a primitive serializer returned by the fast path
- [[RawJsonSerializer]]: serializer returned for `RawJson`
- [[ProtoTimestampSerializer]]: example of a well-known-type serializer in the table
