---
tags: [ghost-serializer, serialization, platform, native, registry]
source: ghost-serialization/src/nativeMain/kotlin/com/ghost/serialization/contract/GhostSerializerLink.native.kt
kind: annotation
---

# GhostSerializerLink.native

The Kotlin/Native declaration of the `@GhostSerializerLink` annotation.

## What it is about

It links a `@GhostSerialization` class to its generated serializer as a Kotlin associated object. The runtime resolves the serializer with `findAssociatedObject` instead of requiring `Ghost.addRegistry`. The Ghost compiler plugin attaches it; it is never written by hand.

## How it works

An annotation marked `@AssociatedObjectKey` lets the compiler record, for each annotated class, a pointer to an object. At runtime Ghost asks for that object by key, which is a direct lookup with no reflection. The annotation is declared once for Native and once for Wasm, because the associated-objects API is not in the shared non-JVM stdlib, so it cannot live in `nonJvmMain`.

Visibility is `internal` with `@PublishedApi`, so inline code generated in other modules can still name it. Retention is `BINARY`, so it is not visible through source-level reflection.

## The code

```kotlin
@AssociatedObjectKey
@OptIn(ExperimentalAssociatedObjects::class)
@PublishedApi
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
internal annotation class GhostSerializerLink(
    val serializer: KClass<out GhostSerializer<*>>
)
```

## Related

- [[GhostSerializerLink.wasmJs]]: identical declaration for Wasm
- [[GhostSerializerLinkLookup.native]]: reads the link at runtime on Native
- [[GhostSerializer]]: the type of serializer the annotation points to
- [[GhostSerializerLinkExtension]]: compiler-side extension that creates the link
