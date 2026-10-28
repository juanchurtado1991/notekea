---
tags: [ghost-serializer, compiler, analysis, envelope]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/EnvelopeAnalyzer.kt
kind: class
---

# EnvelopeAnalyzer

Resolves the `@GhostJsonEnvelope` annotation of a class into a [[GhostEnvelopeModel]] and checks the conventions that the router code relies on.

## What it is about

An envelope is a message with a type tag and an opaque payload that is routed later. The annotation says which property holds the tag (`discriminator`, default `"type"`), optionally which holds a timestamp (`timeField`) and optionally one property that carries the data for every type (`dataField`). Before the router is generated, the compiler must make sure all those names exist and the types are right. This class does that and reports errors on the class.

## How it works

`analyze(classDeclaration, properties)` returns null when the class has no envelope annotation, or when the discriminator or the data field cannot be found. Otherwise:

1. It finds the discriminator property by JSON or Kotlin name, and errors if it is not a `String`. A missing time property is only a warning.
2. It handles at most one `@GhostEnvelopeFallback` property (a second one is an error), which becomes the fallback mapping with an empty discriminator value.
3. **Generic mode** (a `dataField` is given): the data property must be a nullable `RawJson`. Payload mappings come from properties that carry `@GhostEnvelopePayload`.
4. **Fat-envelope mode**: every property that is not the discriminator, time or fallback and that has `@GhostEnvelopePayload` becomes a mapping and must be nullable `RawJson`. A nullable `RawJson` without the tag only gets a warning, because it would never be routed. No payloads at all is an error.
5. Two mappings with the same discriminator value are an error.

`buildPayloadMapping` reads the annotation's `value` (required, non-empty) and `target`. The default placeholders `Unit` and `Nothing` mean "no typed target".

## The code

The payload convention is enforced by one small check:

```kotlin
private fun validateOpaquePayload(
    prop: GhostPropertyModel,
    classDeclaration: KSClassDeclaration
) {
    if (!prop.isNullable || !prop.type.isRawJson()) {
        logger.error(
            "${AC.STR_ERR_ENVELOPE_PAYLOAD_TYPE_1}${prop.kotlinName}${AC.STR_ERR_ENVELOPE_PAYLOAD_TYPE_2}",
            classDeclaration
        )
    }
}
```

Duplicate values are found by grouping:

```kotlin
val duplicateValues =
    payloadMappings.groupBy { it.discriminatorValue }.filter { it.value.size > 1 }
```

The model that is returned carries both the Kotlin and the JSON name of the discriminator, the time property, the mode flag, the data field, the mappings and the fallback.

## Related

- [[GhostSerializationProcessor]]: calls `analyze` while generating each class, after [[GhostAnalyzer]] has produced the properties.
- [[GhostEnvelopeModel]]: the result.
- [[EnvelopePayloadMapping]]: the element type of the mappings.
- [[GhostPropertyModel]]: the input; its type information drives validation.
- [[GhostAnalyzerConstants]]: annotation and argument names, and all the messages.
- [[GhostJsonEnvelope]]: the annotation that it reads.
- [[EnvelopeRouterEmitter]]: the emitter that consumes the model.
