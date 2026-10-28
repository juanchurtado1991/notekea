---
tags: [ghost-serializer, api, annotations, wrap]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostWrap.kt
kind: annotation
---

# GhostWrap

A property annotation that writes a flat property into a nested JSON object path.

## What it is about

Your Kotlin class may keep a field at the top level, while the external API wants it nested, for example under `meta.owner`. `@GhostWrap("meta.owner")` produces that nesting when serializing, without requiring wrapper classes. It is the inverse of [[GhostFlatten]] and lives in `ghost-api`.

## How it works

The `path` parameter is a dot-separated list of object names. The compiler groups wrapped properties by path and generates a writer that opens the nested objects, writes the field and closes them again, so the output has the right structure while the model stays flat. As with its inverse, no intermediate objects exist at runtime.

A related but different annotation, [[GhostWrappedKeys]], works on the other axis: it groups sibling keys into one property. The two together let a model's shape differ freely from the wire shape in both directions.

The annotation is not supported with proto3 mapping or YAML, where it would be a KSP error.

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.BINARY)
annotation class GhostWrap(val path: String)
```

A single `path` argument. The targets `PROPERTY` and `FIELD` are the same as for [[GhostFlatten]], so the two annotations can be used symmetrically on the same model.

## Related

- [[GhostFlatten]]: the reading-side inverse.
- [[GhostWrappedKeys]]: groups sibling keys into one property.
- [[BaseSerializeEmitter]]: emitter that writes the wrapped structure.
- [[FragmentedSerializeEmitter]]: variant emitter for large models that also handles wrapping.
- [[GhostPropertyModel]]: stores the wrap path.
- [[GhostYamlSerialization]]: cannot be combined with it.
