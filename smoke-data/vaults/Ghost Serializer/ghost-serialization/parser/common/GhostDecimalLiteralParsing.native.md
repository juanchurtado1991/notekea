---
tags: [ghost-serialization, serialization, numbers, native]
source: ghost-serialization/src/nativeMain/kotlin/com/ghost/serialization/parser/common/GhostDecimalLiteralParsing.native.kt
kind: expect/actual
---

# GhostDecimalLiteralParsing.native

The Kotlin/Native `actual` for converting a decimal literal to `Double` or `Float`: it takes the platform's approximate `toDouble()` and refines it to the correctly rounded result.

## What it is about

When the number kernels cannot scale a parsed mantissa exactly, they call `parseDecimalLiteralToDouble` or `parseDecimalLiteralToFloat` (declared `expect` in [[GhostDecimalLiteralParsing]]). Kotlin/Native's `String.toDouble()` is only an approximation: hard literals can land one unit in the last place away from the true nearest value. This file makes Native correct by layering the exact-rounding helpers of [[GhostDecimalExactRounding]] on top of that approximation.

## How it works

For `Double`, the work is delegated to `correctlyRoundedDouble(literal)`. That helper removes a leading minus, calls the supplied lambda for the non-negative magnitude, and restores the sign. The lambda calls `refineToNearestDouble(magnitudeLiteral, approximation = magnitudeLiteral.toDouble())`. The refinement compares the literal with the candidate and its neighbours using exact big-integer arithmetic and moves the candidate by at most a few units in the last place until the literal lies between the midpoints to its neighbours.

For `Float`, `correctlyRoundedFloat` does the same sign handling, obtains the refined `Double`, and then narrows it with `narrowToNearestFloat`, which resolves the one case where narrowing rounds twice (a double exactly halfway between two floats).

## The code

```kotlin
internal actual fun parseDecimalLiteralToDouble(
    literal: String
): Double = correctlyRoundedDouble(literal = literal) { magnitudeLiteral ->
    refineToNearestDouble(
        magnitudeLiteral = magnitudeLiteral,
        approximation = magnitudeLiteral.toDouble()
    )
}
```

The Float version differs only in calling `correctlyRoundedFloat`. Both lambdas are inlined because the helpers are `inline` functions, so there is no closure allocation. This slow path is rare: most numbers are handled by the exact power-of-ten scaling or by the Eisel-Lemire routine before ever reaching it.

## Related

- [[GhostDecimalLiteralParsing]]: the `expect` declarations implemented here.
- [[GhostDecimalExactRounding]]: provides `correctlyRoundedDouble`, `correctlyRoundedFloat` and `refineToNearestDouble`.
- [[GhostDecimalLiteralParsing.wasmJs]]: the Wasm actual, which uses the JS engine's `Number()` instead.
- [[GhostDecimalLiteralParsing.jvmAndroid]]: the JVM actual, which trusts the standard library.
- [[GhostJsonFloatParseHelpers]]: the kernel that calls into this slow path.
