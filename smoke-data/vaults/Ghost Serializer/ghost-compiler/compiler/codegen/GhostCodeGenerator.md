---
tags: [ghost-serializer, compiler, codegen, ksp]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/GhostCodeGenerator.kt
kind: class
---

# GhostCodeGenerator

The class that turns the analysed model of one `@GhostSerialization` class into a complete KotlinPoet `FileSpec` holding its generated serializer object.

## What it is about

`GhostCodeGenerator` is the orchestrator of code generation in the KSP compiler. The annotation processor ([[GhostSerializationProcessor]]) analyses a class into [[GhostPropertyModel]]s and hands them over; this class decides what the generated `object <Name>Serializer` contains and in which order. It does very little emitting itself: imports, companion constants, deserialize bodies and serialize bodies are each delegated to a specialised collaborator.

## How it works

On construction it builds a [[GhostSerializerContext]] (names, flags such as sealed / value / enum / proto / resilient, reader and writer class names) and creates an import resolver and a setup emitter from it. `createSpec()` then creates the file with an `@OptIn(InternalGhostApi)` annotation, lets [[SerializerImportResolver]] add the conditional imports, adds extra imports when an envelope maps payloads to target types, and appends the serializer object.

`buildSerializerObject()` assembles the object in a fixed order: superclass and optional YAML interface, the `isProto` and type-name overrides, the perfect-hash `OPTIONS`, cached byte-string headers and enum options (only when the shape needs them), nested options for flattened properties, then one deserialize function per channel. The channels are streaming (always), flat (always), string (only with a text channel) and YAML flat (only when YAML is present, with resilience turned off). Contextual serializers are injected, an envelope router is added when there is an envelope, and finally one serialize function per writer (streaming, string, YAML) plus the `warmUp` method. The text and YAML variants are optional, so a JSON-only class produces a smaller file.

## The code

The constructor shows the flags that shape the output:

```kotlin
internal class GhostCodeGenerator(
    properties: List<GhostPropertyModel>,
    classDeclaration: KSClassDeclaration,
    textChannel: Boolean = false,
    envelopeModel: GhostEnvelopeModel? = null,
    hasYaml: Boolean = false,
)
```

`createSpec()` only adds the file-level annotation and imports, then delegates the body:

```kotlin
importResolver.applyTo(fileBuilder = fileBuilder)
// …
.addType(buildSerializerObject())
```

`deserializeEmitterFor(...)` is a small factory that builds a [[DeserializeCodeEmitter]] for a given reader class. The YAML variant passes `isResilientClass = false` and `supportsResilience = false`, so the YAML path never emits resilient decoding. Serialization is different: a single [[SerializeCodeEmitter]] is reused and `build(...)` is called once per writer class.

Setup work goes through [[SerializerSetupEmitter]] (`addPerfectHashOptions`, `addCachedHeaderProperties`, `addEnumOptions`, `buildWarmUpMethod`) and [[FlattenOptionsGenerator]] for `@GhostFlatten` structures. Envelopes are handled by [[EnvelopeRouterEmitter]].

## Related

- [[GhostSerializationProcessor]]: creates this generator for each annotated class and writes its output.
- [[GhostSerializerContext]]: the derived facts about the class that every emitter reads.
- [[GhostPropertyModel]]: the per-property input.
- [[SerializerImportResolver]]: plans the imports of the generated file.
- [[SerializerSetupEmitter]]: emits OPTIONS, header constants and `warmUp`.
- [[FlattenOptionsGenerator]]: emits nested options for flattened properties.
- [[DeserializeCodeEmitter]]: builds the deserialize functions.
- [[SerializeCodeEmitter]]: builds the serialize functions.
