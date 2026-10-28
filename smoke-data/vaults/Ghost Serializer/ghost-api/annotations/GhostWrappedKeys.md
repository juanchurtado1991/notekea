---
tags: [ghost-serializer, api, annotations, wrapped-keys]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostWrappedKeys.kt
kind: annotation
---

# GhostWrappedKeys

A property annotation that gathers several sibling JSON keys into one Kotlin property.

## What it is about

Some payloads put related fields side by side, such as `type`, `dth` and others at the same level, while the model prefers to group them in a small class. `@GhostWrappedKeys` does that grouping. It is the inverse of [[GhostWrap]], and one of the structural annotations in `ghost-api`.

## How it works

When reading, the generated code captures each listed key as a zero-copy `RawJson` slice. It then assembles a synthetic wrapper object from them and parses the property type from it. When writing, it unwraps the property back into sibling keys at the same JSON depth, so the wire format stays flat.

Two switches control absence. `omitIfEmpty` sets the property to `null` when every captured key is missing or JSON `null`; the property must then be nullable. `omitIfAbsent` lists keys that, if any of them is missing or `null`, make the property `null`, useful for optional integration blocks.

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class GhostWrappedKeys(
    val keys: Array<String>,
    val omitIfEmpty: Boolean = false,
    val omitIfAbsent: Array<String> = [],
)
```

`keys` names the wire fields that belong to the wrapper. The two optional parameters refine when to give up and use `null`. This feature is JSON only and is rejected with [[GhostYamlSerialization]].

## Related

- [[GhostWrap]]: nests a property under a path; the conceptual inverse.
- [[GhostFlatten]]: another shape annotation, reading nested values flat.
- [[WrappedKeysAnalyzer]]: compiler analysis of the annotation.
- [[WrappedKeysEmitter]]: generates the capture and unwrap code.
- [[WrappedKeysModel]]: model of one wrapped property.
- [[GhostWrappedKeysCapture]]: runtime support that captures the key values.
- [[RawJson]]: the captured slices.
