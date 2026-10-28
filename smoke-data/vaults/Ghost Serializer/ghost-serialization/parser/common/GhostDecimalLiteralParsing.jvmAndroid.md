---
tags: [ghost-serialization, serialization, numbers, jvm]
source: ghost-serialization/src/jvmAndroidMain/kotlin/com/ghost/serialization/parser/common/GhostDecimalLiteralParsing.jvmAndroid.kt
kind: expect/actual
---

# GhostDecimalLiteralParsing.jvmAndroid

The JVM and Android `actual` for decimal-literal parsing: the two functions simply call `toDouble()` and `toFloat()`.

## What it is about

Ghost needs a correctly rounded conversion from a decimal literal to a floating-point value for the cases where its fast scaling cannot be exact. Common code declares the contract as `expect` functions in [[GhostDecimalLiteralParsing]]. On the JVM (and on Android, which shares the `jvmAndroidMain` source set) the standard library's `String.toDouble()` and `toFloat()` already round correctly, so the actual is a one-line forwarder.

## How it works

`parseDecimalLiteralToDouble(literal)` returns `literal.toDouble()`, and `parseDecimalLiteralToFloat(literal)` returns `literal.toFloat()`. Java's `Double.parseDouble` and `Float.parseFloat` perform correctly rounded decimal-to-binary conversion for the inputs Ghost passes, which are non-empty JSON-style numbers (digits, optional fraction, optional exponent), sometimes with a sign.

That is why this file is tiny while the Native and Wasm siblings are not: Native would need the exact refinement of [[GhostDecimalExactRounding]], and Wasm borrows JavaScript's `Number()`. The shared source set means Android devices use the same code path as the JVM, with the Android runtime's implementation of the same methods.

## The code

```kotlin
internal actual fun parseDecimalLiteralToDouble(
    literal: String
): Double = literal.toDouble()

internal actual fun parseDecimalLiteralToFloat(
    literal: String
): Float = literal.toFloat()
```

Both functions are `internal`, so they are an implementation detail of the module. They are called from [[GhostJsonFloatParseHelpers]] (through `finalizeParsedDouble` and `finalizeParsedFloat`) and from the YAML number decoder.

Parsing a `Float` directly with `toFloat()` is deliberate: it avoids the double-rounding problem that narrowing an already-rounded `Double` can produce.

## Related

- [[GhostDecimalLiteralParsing]]: the `expect` declarations implemented here.
- [[GhostDecimalLiteralParsing.native]]: the Native actual with exact refinement.
- [[GhostDecimalLiteralParsing.wasmJs]]: the Wasm actual using JavaScript's `Number()`.
- [[GhostJsonFloatParseHelpers]]: the JSON number kernel that calls it.
- [[GhostYamlNumberDecoding]]: the YAML decoder that also calls it.
