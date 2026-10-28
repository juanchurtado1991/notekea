---
tags: [ghost-serializer, compiler, constants, analysis]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/internal/GhostAnalyzerConstants.kt
kind: object
---

# GhostAnalyzerConstants

What the analyzers look for and what they say: annotation names, qualified type names, annotation argument names, default-expression whitelists and every diagnostic text.

## What it is about

The analysis stage ([[GhostAnalyzer]], [[WrappedKeysAnalyzer]], [[EnvelopeAnalyzer]], [[CustomCoderAnalyzer]], [[DefaultExpressionExtractor]] and the type helpers) matches KSP declarations against names written as text. All of those names, and all the messages the compiler shows to developers on a mistake, live in this `internal object`.

## How it works

**Names to match.** Qualified names of Kotlin types (`kotlin.Int`, `kotlin.collections.List`, the `IntArray` family and `com.ghost.serialization.types.RawJson`), annotation short names (`GhostIgnore`, `GhostName`, `SerialName`, `GhostResilient`, `GhostDecoder`, `GhostEncoder`, `GhostFlatten`, `GhostWrap`, `GhostWrappedKeys`, `GhostJsonEnvelope`, `GhostSignature`) and their argument names (`provider`, `functionName`, `path`, `keys`, `omitIfEmpty`, `textChannel`, `discriminator`, `dataField`, `target`). The qualified names of the three readers are built from package constants for [[CustomCoderAnalyzer]].

**Diagnostics.** Messages are split into numbered parts (`STR_ERR_DUP_1`...`_4`) that the analyzer joins around a dynamic value, such as a class or property name. They cover unsupported class kinds, private properties, duplicate or empty JSON names, non-string map keys, enums not annotated with `@GhostSerialization`, and all the envelope and wrapped-keys rules.

**Default-expression rules.** Regexes for numeric literals and enum-like references, sets of accepted escapes and empty-collection calls, plus search limits: `DEFAULT_EXPR_MAX_PARAM_SEARCH_CHARS = 8_192` and `DEFAULT_EXPR_MAX_PARAM_BACKTRACK_CHARS = 512`.

**Templates and initial values**, for example `0`, `0L`, `0uL`, `0.0f`, and strings such as `ctx.%s` and `mask%s`.

## The code

Qualified names that mark an annotation or type:

```kotlin
const val GHOST_IGNORE = "GhostIgnore"
const val GHOST_NAME = "GhostName"
const val SERIAL_NAME = "SerialName"
const val LIST_QUALIFIED = "kotlin.collections.List"
const val SET_QUALIFIED = "kotlin.collections.Set"
const val MAP_QUALIFIED = "kotlin.collections.Map"
```

The numeric whitelist used for defaults:

```kotlin
const val REGEX_DEFAULT_EXPR_NUMERIC =
    """^-?(?:""" +
            """(?:0|[1-9]\d*)(?:L|l)?|""" +
            """(?:0|[1-9]\d*)\.\d+(?:[eE][+-]?\d+)?[fFdD]?|""" +
            """(?:0|[1-9]\d*)(?:[eE][+-]?\d+)[fFdD]?|""" +
            """(?:0|[1-9]\d*)[fFdD]""" +
            """)$"""
```

## Related

- [[GhostAnalyzer]]: the main user.
- [[WrappedKeysAnalyzer]]: uses the wrapped-keys names and messages.
- [[EnvelopeAnalyzer]]: uses the envelope names and messages.
- [[DefaultExpressionExtractor]]: uses the regexes and limits.
- [[CustomCoderAnalyzer]]: uses the argument and reader names.
- [[GhostCommonConstants]]: the shared names this object builds on.
- [[GhostEmitterConstants]]: uses a few of the same names for templates.
