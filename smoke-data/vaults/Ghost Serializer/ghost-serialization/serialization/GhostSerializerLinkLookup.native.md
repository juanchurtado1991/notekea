---
tags: [ghost-serializer, serialization, platform, native, registry]
source: ghost-serialization/src/nativeMain/kotlin/com/ghost/serialization/GhostSerializerLinkLookup.native.kt
kind: expect/actual
---

# GhostSerializerLinkLookup.native

The Kotlin/Native actual of `findLinkedSerializer`, which resolves a serializer linked to a class by the compiler plugin.

## What it is about

Kotlin/Native has no ServiceLoader and no reflection, so Ghost relies on the compiler plugin linking each serialized class to its serializer. This function is where `Ghost` turns that link into a usable `GhostSerializer`. It runs only after primitives, well-known types and registered registries have failed to match.

## How it works

It asks Kotlin for the associated object of `clazz` under the key `GhostSerializerLink`. If one exists and is a `GhostSerializer`, it is returned; otherwise the `as?` cast yields `null`, which makes `Ghost` throw its "No Ghost serializer found" error, extended with the platform's registration hint.

The body is the same as the Wasm one. The two exist side by side because the associated-objects annotation is declared separately for each target.

## The code

```kotlin
@OptIn(ExperimentalAssociatedObjects::class)
internal actual fun findLinkedSerializer(
    clazz: KClass<*>
): GhostSerializer<*>? = clazz.findAssociatedObject<GhostSerializerLink>() as? GhostSerializer<*>
```

## Related

- [[GhostSerializerLink.native]]: the annotation read by this lookup
- [[GhostSerializerLinkLookup.wasmJs]]: identical Wasm actual
- [[GhostSerializerLinkLookup.jvmAndroid]]: JVM/Android actual that returns `null`
- [[GhostSerializerLookup]]: calls `findLinkedSerializer` last in `getSerializerFromRegistries`
- [[Ghost.nonJvm]]: provides the native registration hint shown when this lookup fails
