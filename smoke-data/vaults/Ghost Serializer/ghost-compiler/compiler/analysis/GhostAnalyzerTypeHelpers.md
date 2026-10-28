---
tags: [ghost-serializer, compiler, analysis, ksp]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/GhostAnalyzerTypeHelpers.kt
kind: functions
---

# GhostAnalyzerTypeHelpers

A file of small stateless functions that inspect KSP types and annotations for the analyzers.

## What it is about

The analysis classes ([[GhostAnalyzer]], [[WrappedKeysAnalyzer]] and, according to the file comment, [[CustomCoderAnalyzer]]) need to ask the same questions: what is the JSON name of this property, is this type an enum, is it a Ghost-annotated class? None of those answers needs a logger or state, so they are plain top-level `internal` functions here instead of being duplicated or inherited.

## How it works

The functions fall into three groups.

**Naming.** `getSerialName` picks the wire name in a fixed order: the argument of `@GhostName`, then the argument of a `@SerialName` (the check accepts any annotation whose name ends in `SerialName`, so kotlinx.serialization's is recognized), and otherwise the declaration's own simple name. `getJsonName` is the property-typed shortcut. `toLowerCamelCase` converts `snake_case` to `lowerCamelCase`; it is used for proto classes, whose JSON mapping uses camel case by default.

**Type classification.** `isEnumType`, `isGhostType` (annotated with `@GhostSerialization` or `@GhostProtoSerialization`), `isSealedClass` and `isValueClass` (`value` or `inline`) inspect a `KSType`'s declaration. `resolveFirstTypeArg` and `resolveSecondTypeArg` return the type arguments of a collection, so the analyzer can reach the element and the map-value types.

**Annotations.** `hasAnnotation(name)` compares by short name only, which keeps the compiler independent of the annotation classes.

## The code

The name resolution is the most important function:

```kotlin
internal fun getSerialName(declaration: KSAnnotated): String {
    val annotations = declaration.annotations.toList()

    // 1. GhostName (Primary)
    val ghostName = annotations.find { it.shortName.asString() == AC.GHOST_NAME }
    if (ghostName != null) {
        val arg = ghostName.arguments.find { it.name?.asString() == AC.NAME }
            ?: ghostName.arguments.firstOrNull()
        return arg?.value?.toString() ?: CC.STR_EMPTY
    }
    // …
```

and the small type predicates:

```kotlin
internal fun isEnumType(type: KSType): Boolean =
    (type.declaration as? KSClassDeclaration)?.classKind == ClassKind.ENUM_CLASS
```

## Related

- [[GhostAnalyzer]]: the main user.
- [[WrappedKeysAnalyzer]]: uses the same name and type helpers.
- [[CustomCoderAnalyzer]]: shares this file's helpers.
- [[GhostAnalyzerConstants]]: the annotation and type names compared here.
- [[GhostYamlEligibility]]: calls `isEnumType` when it checks enums.
- [[GhostName]]: the annotation that `getSerialName` prefers.
