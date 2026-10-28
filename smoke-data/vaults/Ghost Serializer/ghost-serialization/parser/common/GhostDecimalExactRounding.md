---
tags: [ghost-serialization, serialization, numbers, rounding]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostDecimalExactRounding.kt
kind: functions
---

# GhostDecimalExactRounding

Exact decimal-to-binary rounding helpers, with a small arbitrary-precision integer class, used where a platform's own `toDouble()` is not correctly rounded.

## What it is about

Kotlin/Native and Kotlin/Wasm parse some hard decimal literals one unit in the last place wrong (for example `2.2250738585072012e-308`). Ghost wants JSON numbers to round-trip exactly, so this file corrects them. The Native actual refines its approximation here; the Wasm actual only borrows the Float helpers. Literals are non-negative magnitudes in JSON syntax; the sign is handled by the wrappers.

## How it works

Public-to-the-module entry points:

- `correctlyRoundedDouble(literal, nearestDoubleOf)` and `correctlyRoundedFloat(...)` are `inline`: they strip a leading minus, call the lambda on the magnitude, and restore the sign.
- `refineToNearestDouble(magnitudeLiteral, approximation)` starts from the approximation and moves it by single raw-bit steps, at most four times. Each step (`refinementStep`) compares the exact decimal to the midpoints toward the next and previous doubles; the result is the double whose rounding interval contains the literal, with ties to even. Literals more than 400 decimal orders from 1, and zero, are returned unrefined.
- `narrowToNearestFloat(magnitudeLiteral, nearestDouble)` narrows to `Float` and fixes the one case where double rounding could be wrong: the double lies exactly halfway between two floats, or sits on the Float overflow threshold.

The exact comparison uses two private classes. `DecimalLiteral` stores `digits * 10^exponent`; its `compareTo(significand, binaryExponent)` builds both sides as big numbers and compares them. `BigNat` is a minimal natural number with little-endian 32-bit limbs and operations `shiftLeft`, `timesPowerOfTen`, `plusSmall`, `timesSmall` and `compareTo`.

## The code

```kotlin
internal fun refineToNearestDouble(
    magnitudeLiteral: String,
    approximation: Double
): Double {
    val decimal = DecimalLiteral.parse(literal = magnitudeLiteral) ?: return approximation
    // …
    var candidate = approximation
    repeat(MAX_REFINEMENT_STEPS) {
        val step = refinementStep(decimal = decimal, candidate = candidate)
        if (step == 0) return candidate
        candidate = Double.fromBits(bits = candidate.toRawBits() + step)
    }
    return candidate
}
```

## Related

- [[GhostDecimalLiteralParsing.native]]: calls `correctlyRoundedDouble`, `correctlyRoundedFloat` and `refineToNearestDouble`.
- [[GhostDecimalLiteralParsing.wasmJs]]: uses `correctlyRoundedFloat` with JavaScript's `Number()`.
- [[GhostDecimalLiteralParsing]]: the `expect` functions these support.
- [[GhostJsonFloatParseHelpers]]: defines `isAmbiguousFloatNarrowing`, imported here.
- [[GhostJsonTokens]]: provides characters and codes for parsing the literal.
- [[GhostNumericParseHelpers]]: provides `isDigit` and `isExponentMarker` used when parsing.
