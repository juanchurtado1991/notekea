---
tags: [ghost-serializer, compiler, model, envelope]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/model/EnvelopePayloadMapping.kt
kind: class
---

# EnvelopePayloadMapping

One routable payload slot of an envelope: a discriminator value, the field that holds the payload and, optionally, the type to decode it into.

## What it is about

For an envelope class (see [[GhostEnvelopeModel]]), the generated router needs to know, for each discriminator value, which property to return and whether to decode it. This record is that row of the routing table. It lives in the model layer and is created by [[EnvelopeAnalyzer]] from `@GhostEnvelopePayload` annotations and from the `@GhostEnvelopeFallback` property.

## How it works

The analyzer creates two kinds of rows:

1. A normal payload row: `discriminatorValue` is the string from `@GhostEnvelopePayload(value = ...)`, `kotlinName` is the property name, and `targetType` is the `target` class from the annotation. The annotation's placeholder defaults, `Unit` and `Nothing`, are treated as "no target", so `targetType` is null.
2. A fallback row: `discriminatorValue` is the empty string, and there is no target.

`isRawJson` records whether the property type is `RawJson`. The analyzer reports an error unless payload properties are nullable `RawJson`, so in valid code it is true.

[[EnvelopeRouterEmitter]] filters the rows that have a `targetType` ("typed mappings") to know which classes to decode and cache a serializer for. It then emits two route functions per envelope: one returns the raw payload, and one decodes to the target type when the mapping has one.

## The code

```kotlin
internal data class EnvelopePayloadMapping(
    val discriminatorValue: String,
    val kotlinName: String,
    val isRawJson: Boolean,
    val targetType: TypeName?
)
```

The target is a KotlinPoet `TypeName`, so the emitter can drop it straight into generated code, for example `target.decode(...)`, without more resolution.

## Related

- [[GhostEnvelopeModel]]: contains the list of mappings and the fallback.
- [[EnvelopeAnalyzer]]: creates them from annotation arguments.
- [[EnvelopeRouterEmitter]]: turns them into `when` branches.
- [[GhostEnvelopePayload]]: the annotation that tags a payload property.
- [[GhostEnvelopeFallback]]: the annotation that marks the fallback property.
- [[RawJson]]: the type every payload property must have.
