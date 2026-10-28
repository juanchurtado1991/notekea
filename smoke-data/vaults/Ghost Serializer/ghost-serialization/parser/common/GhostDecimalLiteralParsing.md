---
tags: [ghost-serialization, serialization, numbers, expect-actual]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostDecimalLiteralParsing.kt
kind: expect/actual
---

# GhostDecimalLiteralParsing

Two `expect` functions that convert a decimal number literal into a correctly rounded `Double` or `Float`.

## What it is about

Ghost parses JSON numbers by collecting a mantissa and exponent and scaling them, which is exact or handled by the Eisel-Lemire algorithm in nearly all cases. For the few it cannot decide, such as halfway ties or literals with more digits than the mantissa holds, it needs a safe slow path. These declarations are that path. The literal has the shape `[-]digits[.digits][e[+-]digits]`.

## How it works

The common file declares only the contract:

- `parseDecimalLiteralToDouble(literal): Double`
- `parseDecimalLiteralToFloat(literal): Float`

Both are `internal expect`, so each target supplies an implementation. The KDoc records why: Kotlin/Wasm and Kotlin/Native `String.toDouble()` are not correctly rounded; the examples `"64.90e-5"` and `"2.2250738585072012e-308"` come out one unit in the last place off. The JVM and Android actual just calls the standard library; the Wasm actual uses the JavaScript engine's `Number()`; the Native actual refines its approximate result with `refineToNearestDouble`. Floats are produced by narrowing with `narrowToNearestFloat` so that double rounding is avoided.

## The code

```kotlin
internal expect fun parseDecimalLiteralToDouble(
    literal: String
): Double

internal expect fun parseDecimalLiteralToFloat(
    literal: String
): Float
```

The callers are `finalizeParsedDouble` and `finalizeParsedFloat` in the JSON float helpers, and the YAML number decoding. They pass either the original text (when digits were dropped) or a synthesized literal `"${mantissa}e$exponent"`.

## Related

- [[GhostDecimalLiteralParsing.jvmAndroid]]: calls `toDouble()` and `toFloat()`.
- [[GhostDecimalLiteralParsing.native]]: refines Native's approximation exactly.
- [[GhostDecimalLiteralParsing.wasmJs]]: delegates to JavaScript `Number()`.
- [[GhostDecimalExactRounding]]: the exact-rounding helpers used by the Native and Wasm actuals.
- [[GhostJsonFloatParseHelpers]]: the JSON kernels that call these functions.
- [[GhostYamlNumberDecoding]]: the YAML reader's number decoder, another caller.
