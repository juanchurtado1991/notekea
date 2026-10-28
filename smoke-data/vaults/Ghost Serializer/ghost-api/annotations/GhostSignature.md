---
tags: [ghost-serializer, api, annotations, polymorphism]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostSignature.kt
kind: annotation
---

# GhostSignature

Marks a property as a key whose presence identifies a subclass in inferred polymorphism.

## What it is about

Normally a sealed class carries an explicit discriminator field. Some APIs have none: the shape of the object, meaning which keys are present, is the only clue. With `@GhostSerialization(inferred = true)` on the sealed parent, Ghost picks the subclass from the keys it sees. `@GhostSignature` lets you say which keys count. It is a marker in `ghost-api`.

## How it works

By default the compiler treats non-nullable fields that are unique to one subclass as that subclass's signatures. This works when the hierarchy is naturally distinguishable. When it is not, for example because the distinguishing field is shared by several subclasses, you can mark properties as signatures yourself. The rule is that the combination of signatures must remain unique across subclasses, so two subclasses cannot end up with identical signature sets.

At runtime this means the generated reader looks at the keys as it scans the object and decides on the concrete type from them. The marker itself has no runtime presence beyond the compiled annotation.

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class GhostSignature
```

No parameters, property targets only.

## Related

- [[GhostSerialization]]: its `inferred` option enables this kind of polymorphism.
- [[GhostFallback]]: names the default subclass in the explicit-discriminator case.
- [[GhostAnalyzerConstants]]: contains the annotation name matched in analysis.
- [[InferredSubclassModel]]: compiler model of a subclass in an inferred hierarchy.
- [[GhostPropertyModel]]: records which properties act as signatures.
