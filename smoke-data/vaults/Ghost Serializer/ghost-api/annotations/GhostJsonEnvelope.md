---
tags: [ghost-serializer, api, annotations, envelope]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostJsonEnvelope.kt
kind: annotation
---

# GhostJsonEnvelope

A class annotation that declares a data class as a JSON envelope whose payload type is named by a field next to it, not inside it.

## What it is about

Webhooks, server-sent events and event buses often wrap an event as `{"type": "invoice.paid", "data": {...}}`. The shape of `data` depends on `type`, and you usually do not want to parse every possible payload up front. `@GhostJsonEnvelope` tells the KSP plugin to generate routing code for that pattern. It belongs to `ghost-api` and is consumed by the compiler's envelope analysis.

## How it works

At compile time the compiler generates, on the companion serializer, `routePayload(envelope)` and `parsePayload(bytes)`, plus typed variants when a payload field names a `target` model. Routing reads the discriminator, then returns the matching payload as a zero-copy `RawJson` slice of the original buffer, so unused payloads cost nothing to skip.

Two layouts are supported. In the "fat" envelope the class has one nullable `RawJson` property per event type, each tagged with [[GhostEnvelopePayload]]. In the generic envelope, a single shared payload property is named through `dataField`.

## The code

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GhostJsonEnvelope(
    val discriminator: String = "type",
    val timeField: String = "",
    val dataField: String = ""
)
```

- `discriminator` is the JSON field with the wire type name, `"type"` by default.
- `timeField` optionally names a field (for example `"eventTime"`) copied into the metadata of the routed result.
- `dataField`, when not empty, switches to the generic single-payload mode with that field name.

Unknown discriminator values are handled by [[GhostEnvelopeFallback]]. Envelopes cannot be combined with [[GhostProtoSerialization]] or [[GhostYamlSerialization]].

## Related

- [[GhostEnvelopePayload]]: maps one discriminator value to one payload property.
- [[GhostEnvelopeFallback]]: marks the property used for unknown discriminators.
- [[EnvelopeAnalyzer]]: the compiler analysis that reads this annotation.
- [[GhostEnvelopeModel]]: the model the analysis builds from it.
- [[RawJson]]: the type of the zero-copy payload fields.
- [[GhostProtoSerialization]]: incompatible with envelopes.
