---
tags: [ghost-serializer, api, annotations, naming]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostIgnore.kt
kind: annotation
---

# GhostIgnore

A property annotation that removes a field from the wire in both directions.

## What it is about

Some properties of a model are derived, cached or internal and should never be read from or written to JSON. Annotating them with `@GhostIgnore` keeps them out of the generated serializer entirely. It is one of the smallest annotations in `ghost-api`, the sibling of [[GhostName]], which renames a key instead of dropping it.

## How it works

The retention is `SOURCE`: the annotation exists only while the KSP processor runs. The analyzer skips such properties when it builds the list of fields, so the generated reader never looks for the key and the generated writer never emits it. The exclusion applies to every generated path: plain JSON, proto3 JSON for classes marked [[GhostProtoSerialization]], and YAML for classes marked [[GhostYamlSerialization]].

Because the property is not read, it normally needs a default value in the constructor, otherwise the generated code would have nothing to give the parameter. Nothing is added at runtime, so ignoring a field has no cost.

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class GhostIgnore
```

A marker with no parameters.

## Related

- [[GhostName]]: renames a key rather than excluding it.
- [[GhostAnalyzerConstants]]: contains the annotation name matched by the analyzer.
- [[GhostSerialization]]: the class annotation whose generated code skips ignored properties.
- [[GhostProtoSerialization]]: one of the paths where the exclusion applies.
- [[GhostYamlSerialization]]: the other path with the same behaviour.
