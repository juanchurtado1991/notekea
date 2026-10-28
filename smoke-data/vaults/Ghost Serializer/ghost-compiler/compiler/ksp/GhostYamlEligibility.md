---
tags: [ghost-serializer, compiler, ksp, yaml, validation]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/ksp/GhostYamlEligibility.kt
kind: class
---

# GhostYamlEligibility

Decides whether a class gets YAML serializer code, and reports a build error when `@GhostYamlSerialization` is used in a way that cannot work.

## What it is about

YAML support is an opt-in second channel next to JSON. Not every JSON feature carries over to it: sealed hierarchies, envelopes, custom coders and structural rewrites are JSON-only. Rather than silently producing broken YAML code, the compiler checks the rules up front and fails with an explicit message. These rules change whenever YAML's supported surface changes, so they live here and not inside [[GhostSerializationProcessor]].

## How it works

There are two entry points.

`reportOrphanGhostYamlSerialization` scans every class annotated `@GhostYamlSerialization` and logs an error if it carries neither `@GhostSerialization` nor `@GhostProtoSerialization`, because YAML is generated on top of those.

`shouldGenerateYaml` runs per class and returns false as soon as a rule fails. In order: the class has the YAML annotation; the YAML runtime (`GhostYamlSerializer`) is on the classpath, otherwise it quietly returns false; the class also has a base annotation; it is not `@GhostResilient`, sealed, an envelope, or `inferred = true`. Finally every property is checked. A property blocks YAML if it uses a custom coder or a contextual type, a structural mapping (wrapped keys, flatten, wrap), a sealed type, a nested Ghost object, an enum that lacks the YAML annotation, a type that `containsYamlIncompatibleType`, or a value class that wraps such a type. For each offending property an error with a specific reason is logged (for instance a non-proto `ByteArray` or `RawJson`), and generation is skipped.

## The code

A property is checked with one predicate, built from early returns:

```kotlin
val hasCustomCoder = prop.isContextual || prop.customDecoder != null || prop.customEncoder != null
if (hasCustomCoder) {
    return true
}
val hasStructuralMapping = prop.wrappedKeys != null || prop.flattenPath != null || prop.wrapPath != null
if (hasStructuralMapping) {
    return true
}
```

A second function, `yamlIncompatibilityReason`, picks the message for the log. Enums used from a YAML model must also be YAML-annotated:

```kotlin
private fun GhostPropertyModel.referencesEnumWithoutYaml(): Boolean =
    listOfNotNull(type, listInnerType, mapValueType).any { candidate ->
        val hasYamlAnnotation = candidate.declaration.annotations.any {
            it.shortName.asString() == CC.ANNOTATION_GHOST_YAML_SERIALIZATION
        }
        isEnumType(type = candidate) && !hasYamlAnnotation
    }
```

The error texts all begin with `GhostYamlSerialization:` and are defined in [[GhostProcessorConstants]].

## Related

- [[GhostSerializationProcessor]]: calls both methods.
- [[GhostPropertyModel]]: the per-property facts the rules read.
- [[GhostEnvelopeModel]]: a non-null envelope model disables YAML.
- [[GhostProcessorConstants]]: the error messages and the YAML serializer's qualified name.
- [[TypeHelpers]]: provides the type checks `containsYamlIncompatibleType`, `isRawJson` and `isByteArray`.
- [[GhostAnalyzerTypeHelpers]]: provides `isEnumType`.
- [[GhostYamlSerializer]]: the runtime contract whose presence is checked.
