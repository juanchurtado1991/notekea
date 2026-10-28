---
tags: [ghost-serializer, api, annotations, yaml]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostYamlSerialization.kt
kind: annotation
---

# GhostYamlSerialization

An opt-in marker that adds a YAML serializer to a model that already has a JSON or proto one.

## What it is about

YAML support in Ghost is not on by default: a class has to ask for it. Place `@GhostYamlSerialization` on a class that is annotated with [[GhostSerialization]] or [[GhostProtoSerialization]], and the compiler adds a `GhostYamlSerializer` companion, as long as the shape of the model can be expressed in YAML. It is a marker in `ghost-api` with no arguments.

## How it works

During analysis the compiler runs a YAML eligibility scan over the class and its nested Ghost types. If the shape is compatible, it generates the YAML serializer next to the JSON one. If not, it reports a KSP error instead of silently generating something wrong. The error list is long, and it is the practical documentation of what YAML does not support: [[GhostResilient]], [[GhostJsonEnvelope]], [[GhostFlatten]], [[GhostWrap]], [[GhostWrappedKeys]], sealed or `inferred` polymorphism, [[GhostDecoder]], [[GhostEncoder]], contextual serializers, `RawJson`, non-proto `ByteArray`, and nested types that fail the scan.

Keeping YAML opt-in avoids bloating generated code for models that never use it.

## The code

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GhostYamlSerialization
```

The marker has no state; the rules live in the compiler.

## Related

- [[GhostSerialization]]: one of the two annotations it requires.
- [[GhostProtoSerialization]]: the other base annotation it can extend.
- [[GhostYamlEligibility]]: the compiler check that decides whether a model fits YAML.
- [[GhostYamlSerializer]]: the runtime contract of the generated companion.
- [[GhostYamlContentConverter]]: Ktor converter that uses the YAML serializers.
- [[GhostResilient]]: one of the annotations that is rejected alongside it.
