---
tags: [ghost-serializer, api, annotations, envelope]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostEnvelopeFallback.kt
kind: annotation
---

# GhostEnvelopeFallback

Marks the one payload property of an envelope that receives data whose discriminator is not recognised.

## What it is about

In an envelope class built with [[GhostJsonEnvelope]], each known event type has its own payload property. When a new event type shows up that the class does not list, routing has nowhere to put it. `@GhostEnvelopeFallback` designates a property as that catch-all, so the raw payload is preserved instead of dropped. It is the envelope counterpart of [[GhostFallback]], which does the same for sealed classes.

## How it works

The compiler analysis looks for at most one property with this annotation. In the generated router, if the discriminator matches none of the [[GhostEnvelopePayload]] values, the payload is stored in the fallback property. The property must be a nullable `RawJson`, or a `ByteArray`, so that any JSON can be stored without parsing it. If no fallback is declared, unknown discriminators simply route to `null`.

Keeping the payload as raw bytes means logging, forwarding or retrying unknown events is cheap, and the code can parse them later once a model exists.

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class GhostEnvelopeFallback
```

There are no parameters. The constraints (nullable `RawJson` or `ByteArray`, at most one per class) are enforced by the analyzer rather than by the type system.

## Related

- [[GhostJsonEnvelope]]: the class annotation that makes the envelope.
- [[GhostEnvelopePayload]]: maps the known discriminator values to properties.
- [[GhostFallback]]: the sealed-class equivalent.
- [[RawJson]]: the usual type of the fallback property.
- [[GhostAnalyzerConstants]]: holds the annotation name the analyzer matches.
