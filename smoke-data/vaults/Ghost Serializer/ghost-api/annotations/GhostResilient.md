---
tags: [ghost-serializer, api, annotations, resilience]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostResilient.kt
kind: annotation
---

# GhostResilient

A marker that makes a property, or every property of a class, survive bad data instead of failing the whole parse.

## What it is about

When a server sends a wrong type or an enum value your app does not know, the default behaviour is to throw. `@GhostResilient` changes that for the annotated property or class: Ghost assigns `null` when the property is nullable and carries on reading. It is an annotation of `ghost-api` and works only on the JSON readers.

## How it works

The KSP analyzer sees the annotation on a property or on its owning class and records it in the property model. The generated reader then wraps the decoding of that field so that a type mismatch or unknown enum constant yields `null` and the reader skips the value, keeping its position consistent. Resilience is therefore a compile-time decision: there is no runtime flag to flip.

YAML is explicitly not supported. Combining `@GhostResilient` with [[GhostYamlSerialization]] on the same class is reported as a KSP error, rather than silently ignored.

## The code

```kotlin
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GhostResilient
```

It has no parameters. Targeting both `PROPERTY` and `CLASS` lets you protect a single flaky field or declare a whole model tolerant. The trade-off is that errors become `null` values, which can hide a real contract break, so it is best used on fields that are truly optional.

## Related

- [[GhostAnalyzer]]: the compiler analysis that detects the annotation.
- [[GhostPropertyModel]]: records whether a property is resilient.
- [[GhostAnalyzerConstants]]: holds the annotation name the analyzer matches.
- [[GhostJsonHintMessages]]: error hints that may suggest this annotation.
- [[GhostYamlSerialization]]: cannot be combined with it.
- [[GhostCoerce]]: a different kind of leniency, applied on web endpoints.
