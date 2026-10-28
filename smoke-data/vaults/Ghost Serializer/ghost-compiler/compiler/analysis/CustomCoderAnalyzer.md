---
tags: [ghost-serializer, compiler, analysis, custom-coder]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/CustomCoderAnalyzer.kt
kind: class
---

# CustomCoderAnalyzer

Turns a `@GhostDecoder` or `@GhostEncoder` annotation on a property into a [[CustomCoderModel]], including which reader types the user's function accepts.

## What it is about

A developer can replace Ghost's generated handling of a single property with a function of their own. The annotation gives a `provider` class and a `functionName`. The compiler needs more than those two names: since Ghost has three readers, it must know which of them the function can take. This analyzer finds out by looking at the function's overloads. It was split out of [[GhostAnalyzer]] because it changes for its own reason, custom-coder resolution.

## How it works

`resolveCustomCoder(prop, annotationName)` finds the annotation on the property by short name and reads two arguments: `provider` (a KSP type) and `functionName` (a string). If either is missing it returns null. Otherwise it builds the model with the provider converted to a KotlinPoet type name and the reader kinds worked out below.

`resolveCustomCoderReaderKinds` lists every function in the provider class (including inherited ones) with the given name. For each overload it looks at the qualified name of the first parameter:

- the string reader gives `STRING`,
- the flat reader gives `FLAT`,
- the streaming reader gives `BYTES`,
- anything else is ignored.

If no overload matches, or the provider is not a class, the set is just `BYTES`. This is a conservative default: the generated code can always build a bytes reader as a bridge (see [[CustomCoderReaderKind]]).

`warnIfCustomCoder` writes an info-level log line that names the property and both coders. Despite its name, it does not raise a warning.

## The code

```kotlin
val kinds = declaration.getAllFunctions()
    .filter { it.simpleName.asString() == functionName }
    .mapNotNull { fn ->
        when (fn.parameters.firstOrNull()?.type?.resolve()?.declaration?.qualifiedName?.asString()) {
            AC.STR_GHOST_JSON_STRING_READER_QUALIFIED -> CustomCoderReaderKind.STRING
            AC.STR_GHOST_JSON_FLAT_READER_QUALIFIED -> CustomCoderReaderKind.FLAT
            AC.STR_GHOST_JSON_READER_QUALIFIED -> CustomCoderReaderKind.BYTES
            else -> null
        }
    }
    .toSet()
return kinds.ifEmpty { setOf(CustomCoderReaderKind.BYTES) }
```

The qualified reader names come from [[GhostAnalyzerConstants]], so that a package move of a reader only needs one change.

## Related

- [[GhostAnalyzer]]: owns an instance and calls it for each property.
- [[CustomCoderModel]]: what is built.
- [[CustomCoderReaderKind]]: the labels used for the overloads.
- [[GhostAnalyzerConstants]]: annotation arguments, qualified reader names and the log text.
- [[GhostDecoder]]: the annotation for custom decoders.
- [[GhostEncoder]]: the annotation for custom encoders.
