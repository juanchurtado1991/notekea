---
tags: [ghost-serializer, compiler, analysis, text-channel]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/TextChannelPlanner.kt
kind: object
---

# TextChannelPlanner

Decides, for every model class of a module, whether its generated serializer also gets the string-based reader and writer overloads (the "text channel").

## What it is about

Ghost can decode from bytes or, on platforms with native strings, directly from a `String`. The string overloads make generated code larger, so they are optional. `@GhostSerialization(textChannel = ...)` lets a model opt out. But if model A has the channel and has a property of type B, A's generated code calls B's string overload, so B must have it too. This object computes the final yes/no per class, respecting both rules.

## How it works

`plan(analyzed, moduleTextChannelOverride)` returns a map from class declaration to a Boolean.

1. The module option `ghost.textChannel` wins: if it is `true` or `false`, every class gets that value.
2. Otherwise a class starts enabled when its annotation's own `textChannel` is true. The default is true, and only `@GhostSerialization` classes have an opinion; a `@GhostProtoSerialization` class starts disabled.
3. Then a breadth-first walk propagates enablement through dependencies: for each enabled class it collects the Ghost classes it refers to (property types, list elements, map values, a value class's inner type, sealed subclasses, inferred subclasses and their properties), and enables those too, until nothing new is added.

So an explicit `textChannel = false` is honored only when no enabled class reaches that model. This is a trade-off between code size and correctness: it is better to emit an unneeded overload than a call that does not compile.

`AnalyzedClass` is a small data holder of a declaration and its properties, so the planner does not have to re-run the analysis.

## The code

The propagation loop:

```kotlin
val pending = ArrayDeque(enabled)
while (pending.isNotEmpty()) {
    val current = pending.removeFirst()
    val properties = byDeclaration[current] ?: emptyList()
    for (dependency in ghostDependencies(classDeclaration = current, properties = properties)) {
        if (enabled.add(dependency)) {
            pending.addLast(dependency)
        }
    }
}
```

and the own preference:

```kotlin
val explicit = annotation.arguments
    .firstOrNull { arg -> arg.name?.asString() == AC.ARG_TEXT_CHANNEL }
    ?.value as? Boolean
return explicit ?: true
```

## Related

- [[GhostSerializationProcessor]]: calls `plan` once per round and passes the result to the generator.
- [[GhostPropertyModel]]: the properties that are followed to find dependencies.
- [[InferredSubclassModel]]: followed for inferred polymorphism.
- [[GhostAnalyzerConstants]]: the `textChannel` argument name.
- [[GhostCommonConstants]]: the annotation name used to find Ghost classes.
- [[GhostCodeGenerator]]: receives the per-class flag.
