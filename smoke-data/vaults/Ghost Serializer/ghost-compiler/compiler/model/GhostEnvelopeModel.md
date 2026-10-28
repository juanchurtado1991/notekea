---
tags: [ghost-serializer, compiler, model, envelope]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/model/GhostEnvelopeModel.kt
kind: class
---

# GhostEnvelopeModel

The analyzed form of a class annotated with `@GhostJsonEnvelope`.

## What it is about

An envelope is a JSON message whose payload type depends on a discriminator, such as `{"type": "order", "ts": 17, "data": {...}}`. Ghost does not decode the payload straight away. It keeps it as an opaque `RawJson` slice, and a generated router later picks the right payload or decodes it into a chosen target type. This record is what [[EnvelopeAnalyzer]] produces after validating the annotation, and what [[EnvelopeRouterEmitter]] consumes to write the router functions.

## How it works

An envelope has two modes, and the model records which one applies with `isGenericMode`.

- **Fat envelope**: each payload has its own nullable `RawJson` property, tagged `@GhostEnvelopePayload("value")`. `payloadMappings` has one entry per tagged property.
- **Generic mode**: the annotation names a single `dataField`; `genericDataKotlinName` holds that property. Payload mappings, if any, only add discriminator values and typed targets that route that same field.

In both modes there are the discriminator property (Kotlin and JSON names are kept separately, because the router reads the Kotlin property while the wire name matters for parsing), an optional time property, and an optional `fallbackMapping` for values that match nothing. Without a fallback, an unknown value routes to null (or, in generic mode, to the data field).

The class is data only, all fields are immutable, and a null return from the analyzer means the annotation was invalid.

## The code

```kotlin
internal data class GhostEnvelopeModel(
    val discriminatorKotlinName: String,
    val discriminatorJsonName: String,
    val timeKotlinName: String?,
    val isGenericMode: Boolean,
    val genericDataKotlinName: String?,
    val payloadMappings: List<EnvelopePayloadMapping>,
    val fallbackMapping: EnvelopePayloadMapping?
)
```

Besides the router, the model has two other users: the processor passes it to [[GhostYamlEligibility]], which rejects any envelope on YAML, and [[GhostSerializerContext]] keeps it so emitters know the class is an envelope.

## Related

- [[EnvelopeAnalyzer]]: builds and validates it.
- [[EnvelopePayloadMapping]]: the element type of `payloadMappings` and of the fallback.
- [[EnvelopeRouterEmitter]]: generates the routing functions from it.
- [[GhostYamlEligibility]]: refuses YAML when an envelope is present.
- [[GhostSerializerContext]]: carries it into code generation.
- [[GhostJsonEnvelope]]: the annotation that this model represents.
