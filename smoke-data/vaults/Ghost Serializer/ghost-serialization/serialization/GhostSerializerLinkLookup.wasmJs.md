---
tags: [ghost-serializer, serialization, platform, wasm, registry]
source: ghost-serialization/src/wasmJsMain/kotlin/com/ghost/serialization/GhostSerializerLinkLookup.wasmJs.kt
kind: expect/actual
---

# GhostSerializerLinkLookup.wasmJs

The Kotlin/Wasm actual of `findLinkedSerializer`, which finds a serializer attached to a class at compile time.

## What it is about

On Kotlin/Native and Kotlin/Wasm there is no ServiceLoader and no reflection, so `Ghost` cannot discover generated serializers at runtime. Instead the Ghost compiler plugin links each `@GhostSerialization` class to its serializer, and this function reads that link back. It is the last step of `Ghost`'s serializer lookup.

## How it works

The lookup uses Kotlin's experimental associated-objects API. The plugin attaches a `@GhostSerializerLink` annotation to the class; `findAssociatedObject<GhostSerializerLink>()` returns the serializer object that annotation points to. The result is cast to `GhostSerializer<*>` with `as?`, so a class that has no link, or whose link is of the wrong type, resolves to `null` and `Ghost` reports "serializer not found".

The code is identical to the Kotlin/Native version. It cannot be shared in a common source set because the associated-objects API and the annotation it needs are not available in the shared non-JVM standard library.

## The code

```kotlin
@OptIn(ExperimentalAssociatedObjects::class)
internal actual fun findLinkedSerializer(
    clazz: KClass<*>
): GhostSerializer<*>? = clazz.findAssociatedObject<GhostSerializerLink>() as? GhostSerializer<*>
```

The function is `internal`, so only `Ghost`'s own lookup code can call it. The opt-in is required because `findAssociatedObject` is still experimental.

## Related

- [[GhostSerializerLink.wasmJs]]: the annotation this lookup reads through the associated-objects API
- [[GhostSerializerLinkLookup.native]]: identical Kotlin/Native actual
- [[GhostSerializerLinkLookup.jvmAndroid]]: JVM/Android actual that always returns `null`
- [[GhostSerializerLookup]]: calls `findLinkedSerializer` as the last step of `getSerializerFromRegistries`
- [[Ghost]]: declares the `expect` function
