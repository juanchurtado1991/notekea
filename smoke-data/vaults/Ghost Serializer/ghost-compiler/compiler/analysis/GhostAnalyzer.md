---
tags: [ghost-serializer, compiler, analysis, ksp]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/GhostAnalyzer.kt
kind: class
---

# GhostAnalyzer

The first stage of code generation: it validates an annotated class against Ghost's rules and turns its properties into [[GhostPropertyModel]]s.

## What it is about

KSP gives the processor raw declarations (classes, properties, annotations and types). Emitters should not have to deal with those, so `GhostAnalyzer` reads them once and produces a flat, typed description. It also does most of the user-facing validation: anything wrong with a model class is reported here, as a compile error pointing at the declaration, before any code is generated. Two helper classes handle narrower topics: [[WrappedKeysAnalyzer]] and [[CustomCoderAnalyzer]].

## How it works

`analyze(classDeclaration)` follows these steps:

1. Objects return an empty list. Other classes must be `data`, `sealed`, `value` or `enum`, or an error is logged.
2. It collects the properties: all properties minus overridden ones, minus those with `@GhostIgnore`, keeping those with a backing field or that are constructor parameters. A `private` property is an error.
3. Enums get a single synthetic property called `name` that carries the map of entry names to serial names. Other classes get one model per property, built by `buildPropertyModel`.
4. For a sealed class, every sealed subclass is analyzed recursively and attached as `inferredSubclasses` to each property, with a placeholder property when the parent has none.
5. Final validation: no empty JSON name, no duplicate JSON names, then the wrapped-keys rules.

`buildPropertyModel` classifies the type (list, set, map with `String` keys, primitive array, value class, enum, sealed, Ghost type or contextual third-party type), resolves the JSON name (`@GhostName`, then `@SerialName`; for proto classes the name is turned into lowerCamelCase unless explicitly named), reads `@GhostFlatten`/`@GhostWrap` paths, the custom coders and the default expression. The default goes through [[DefaultExpressionExtractor]] and then [[DefaultExpressionQualifier]].

A default value that another default reads (`val doubled: Int = base * 2`) marks `base` as a dependency source. More than six such sources is an error, since each combination needs its own constructor call.

## The code

Which properties take part in serialization:

```kotlin
val properties = allProps
    .filterNot { it in overriddenProps }
    .filterNot { it.hasAnnotation(name = AC.GHOST_IGNORE) }
    .filter { it.hasBackingField || it.simpleName.asString() in constructorParameterNames }
    .toList()
```

How a type is judged contextual, meaning Ghost has no built-in handling for it:

```kotlin
if (isBuiltIn) {
    return when (qualifiedName) {
        AC.K_STRING, AC.K_INT, AC.K_LONG, AC.K_ULONG, AC.K_UINT, AC.K_USHORT, AC.K_UBYTE,
        AC.K_DOUBLE, AC.K_FLOAT,
        AC.K_BOOLEAN, AC.K_BYTE, AC.K_SHORT, AC.K_CHAR, AC.K_UNIT, AC.K_ANY,
        AC.K_BYTE_ARRAY -> false

        else -> true
    }
}
```

Other `kotlin.` and `java.` types, and types without a Ghost annotation, are treated as contextual, so another serializer must supply them.

## Related

- [[GhostSerializationProcessor]]: calls `analyze` for every annotated class.
- [[GhostPropertyModel]]: the result type.
- [[InferredSubclassModel]]: attached for sealed classes.
- [[WrappedKeysAnalyzer]]: validates and resolves `@GhostWrappedKeys`.
- [[CustomCoderAnalyzer]]: resolves `@GhostDecoder` and `@GhostEncoder`.
- [[GhostAnalyzerTypeHelpers]]: the stateless helpers for names and types.
- [[GhostAnalyzerConstants]]: annotation names and error messages.
