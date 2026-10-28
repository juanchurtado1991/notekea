---
tags: [ghost-serializer, api, annotations, envelope]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostEnvelopePayload.kt
kind: annotation
---

# GhostEnvelopePayload

Ties one discriminator value of an envelope to one nullable payload property.

## What it is about

In a "fat" envelope declared with [[GhostJsonEnvelope]], each possible event gets its own property. `@GhostEnvelopePayload("invoice.paid")` on such a property says: when the wire discriminator equals that string, this is where the payload goes. It sits in `ghost-api` and is read only by the compiler.

## How it works

The annotation takes two arguments. `value` is the wire string. `target` is an optional `KClass`; when it names a model annotated with [[GhostSerialization]], the generated `routeTyped` and `parseTyped` methods decode that payload into the model by slicing the original bytes as `RawJson`, with no copy. When `target` is left at its default of `Unit::class`, the router returns the `RawJson` only and leaves decoding to you.

The default of `Unit::class` is a sentinel: Kotlin annotations cannot have nullable class parameters, so "no target" has to be a recognisable class.

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class GhostEnvelopePayload(
    val value: String,
    val target: KClass<*> = Unit::class
)
```

Properties carrying it are expected to be nullable `RawJson` fields, since only the one matching the current discriminator is filled in. For unknown values, see [[GhostEnvelopeFallback]].

## Related

- [[GhostJsonEnvelope]]: the class annotation that these properties belong to.
- [[GhostEnvelopeFallback]]: the catch-all for discriminators with no match.
- [[GhostSerialization]]: the annotation that a typed `target` model carries.
- [[EnvelopePayloadMapping]]: compiler model of one discriminator-to-property mapping.
- [[EnvelopeAnalyzer]]: reads these annotations during compilation.
- [[RawJson]]: the payload representation.
