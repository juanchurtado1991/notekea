---
tags: [ghost-serializer, api, opt-in, annotation]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/InternalGhostApi.kt
kind: annotation
---

# InternalGhostApi

An opt-in marker annotation that flags declarations as internal to Ghost and not meant for application code.

## What it is about

Ghost's runtime has to expose some helpers publicly so that code produced by the KSP compiler can call them, but those helpers are not part of the supported API. `InternalGhostApi` is the Kotlin way to say "you may call this, but you have to acknowledge it". It lives in the root `serialization` package of the `ghost-api` module, next to the annotations that users do apply.

## How it works

The annotation is declared with `@RequiresOptIn` at `WARNING` level. Any call site that touches a declaration marked with it must either opt in explicitly (`@OptIn(InternalGhostApi::class)`) or propagate the marker, otherwise the compiler prints a warning carrying the message "This is an internal Ghost API. Do not use it directly."

Using a warning level instead of an error is a trade-off: generated code and Ghost's own modules can opt in without friction, while application authors get a visible nudge rather than a hard build break. The retention is `BINARY`, so the marker survives in compiled classes and is honoured when a library consumes Ghost as a dependency.

## The code

The whole file is the declaration plus its metadata:

```kotlin
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "This is an internal Ghost API. Do not use it directly."
)
@Retention(AnnotationRetention.BINARY)
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.CONSTRUCTOR
)
annotation class InternalGhostApi
```

The `@Target` list covers classes, functions, properties, type aliases and constructors, which is every kind of declaration Ghost needs to hide: serializer base classes, writer and source implementations, pooling helpers and typealiases.

## Related

- [[AbstractGhostSerializer]]: a base class for generated serializers that carries the marker.
- [[AbstractGhostSource]]: a runtime source type that is internal and marked with it.
- [[ByteArrayGhostSource]]: another source implementation guarded by the opt-in.
- [[FlatByteArrayWriter]]: an internal writer used by generated code.
- [[IntSerializer]]: one of the many built-in serializers marked as internal.
- [[GhostSerialization]]: the public annotation users apply instead, which triggers generation of code that opts in.
