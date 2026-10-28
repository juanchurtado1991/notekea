---
tags: [ghost-serializer, api, annotations, codegen]
source: ghost-api/src/commonMain/kotlin/com/ghost/serialization/annotations/GhostSerialization.kt
kind: annotation
---

# GhostSerialization

The main entry-point annotation: put it on a class and the Ghost KSP plugin generates a reflection-free serializer for it.

## What it is about

Every model you want Ghost to read or write starts here. The annotation is the switch for the compiler: classes without it are ignored, classes with it get a generated serializer that is registered so that `Ghost` can look it up at runtime. It sits in `ghost-api`, the small module that applications and the compiler both depend on.

## How it works

The annotation is read at compile time by the processor, which turns its four parameters into code generation decisions. The retention is `BINARY`, so the information is also available to tooling that inspects compiled classes.

- `name` is an optional label used to avoid collisions in the serializer registry when two classes share a simple name.
- `discriminator` names the JSON field that tells the concrete type of a sealed class; it defaults to `"type"` and has no effect on non-sealed classes.
- `inferred` switches sealed hierarchies to inferred polymorphism, where key presence selects the subclass (see [[GhostSignature]]).
- `textChannel` controls whether string-based reader overloads are generated. When true, decoding a `String` skips UTF-8 re-encoding; turning it off shrinks generated code for byte or stream only types, and the module-wide property `ghost.textChannel=false` forces it for all models.

YAML needs a second annotation, [[GhostYamlSerialization]], on the same class.

## The code

```kotlin
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class GhostSerialization(
    val name: String = "",
    val discriminator: String = "type",
    val inferred: Boolean = false,
    val textChannel: Boolean = true,
)
```

All parameters have defaults, so the common case is just `@GhostSerialization` with nothing else.

## Related

- [[GhostSerializationProcessor]]: the KSP processor that finds annotated classes.
- [[GhostAnalyzerConstants]]: holds the annotation name and parameter names the analyzer matches on.
- [[GhostPropertyModel]]: model built for each property of an annotated class.
- [[GhostYamlSerialization]]: opt-in companion for YAML output.
- [[GhostProtoSerialization]]: alternative annotation for proto3 JSON.
- [[GhostFallback]]: marks the default subclass of a sealed hierarchy.
- [[GhostSignature]]: used with `inferred = true`.
