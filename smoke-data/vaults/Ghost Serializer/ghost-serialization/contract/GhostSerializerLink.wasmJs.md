---
tags: [ghost-serializer, serialization, platform, wasm, registry]
source: ghost-serialization/src/wasmJsMain/kotlin/com/ghost/serialization/contract/GhostSerializerLink.wasmJs.kt
kind: annotation
---

# GhostSerializerLink.wasmJs

The Kotlin/Wasm declaration of the `@GhostSerializerLink` annotation that ties a class to its generated serializer.

## What it is about

This annotation is the compile-time link used instead of runtime discovery on platforms without ServiceLoader. The Ghost compiler plugin, applied through the Gradle plugin, places it on every `@GhostSerialization` class. Nobody writes it by hand. It is declared separately for Native and Wasm because the associated-objects API is not part of the shared non-JVM standard library.

## How it works

`@AssociatedObjectKey` marks the annotation as a key for Kotlin's associated-objects feature: an annotation whose argument is a `KClass` of an object, which the runtime can look up from the annotated class. At runtime, `findLinkedSerializer` calls `findAssociatedObject<GhostSerializerLink>()` and gets the serializer object back without any reflection tables.

The annotation is `@PublishedApi internal` so the plugin-generated code in user modules can still reference it, `BINARY` retention keeps it in the compiled output only, and the target is restricted to classes.

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

The single parameter, `serializer`, names the generated serializer class for the annotated type.

## Related

- [[GhostSerializerLink.native]]: the same annotation, declared for Kotlin/Native
- [[GhostSerializerLinkLookup.wasmJs]]: reads this annotation at runtime
- [[GhostSerializer]]: the contract the linked class must implement
- [[GhostSerializerLinkExtension]]: compiler-side code that attaches the link
