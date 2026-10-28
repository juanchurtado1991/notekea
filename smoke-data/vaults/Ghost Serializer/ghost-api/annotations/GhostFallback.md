---
tags: [ghost-serializer, api, annotations, polymorphism]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostFallback.kt
kind: annotation
---

# GhostFallback

A marker annotation for the sealed subclass that catches every unknown discriminator value.

## What it is about

Polymorphic payloads evolve: a server can add a new event type that an older client has never heard of. Without a fallback, deserialization fails on the unknown discriminator. Annotating one subclass with `@GhostFallback` tells Ghost to create that subclass instead, so old clients keep working. It is part of the polymorphism annotations in `ghost-api`, used together with [[GhostSerialization]] on the sealed parent.

## How it works

At compile time the processor inspects the subclasses of a sealed class. When the generated deserializer reads the discriminator field (named by the `discriminator` parameter of [[GhostSerialization]]), it dispatches to the matching subclass; if nothing matches, it uses the subclass carrying this annotation. The rule is one fallback per hierarchy: only a single subclass may carry the annotation.

The annotation has no parameters and no runtime behaviour of its own, only the information the compiler needs.

## The code

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GhostFallback
```

It targets classes only, since the fallback is a subclass, and keeps `BINARY` retention so the choice survives in compiled artifacts. Envelope payloads have their own, similarly named annotation, [[GhostEnvelopeFallback]], which applies to a property rather than a class.

## Related

- [[GhostSerialization]]: the annotation on the sealed parent whose discriminator this fallback complements.
- [[GhostEnvelopeFallback]]: the envelope equivalent for unknown discriminators.
- [[GhostEmitterConstants]]: constants the code emitters use when generating the dispatch, including the fallback path.
- [[GhostJsonHintMessages]]: error hints that mention the fallback when an unknown type is met.
- [[GhostSignature]]: the other way to pick subclasses, by key presence.
