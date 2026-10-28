---
tags: [ghost-serialization, serialization, numbers, wasm]
source: ghost-serialization/src/wasmJsMain/kotlin/com/ghost/serialization/parser/common/GhostDecimalLiteralParsing.wasmJs.kt
kind: expect/actual
---

# GhostDecimalLiteralParsing.wasmJs

The Wasm-JS `actual` for turning a decimal literal into a correctly rounded `Double` or `Float`, by borrowing the JavaScript engine's `Number()`.

## What it is about

Ghost's number kernels have a slow path for literals that cannot be scaled exactly. That path calls the `expect` functions `parseDecimalLiteralToDouble` and `parseDecimalLiteralToFloat` declared in [[GhostDecimalLiteralParsing]]. Kotlin/Wasm's own `String.toDouble()` is not correctly rounded, so this file hands the work to JavaScript, whose `Number()` is.

## How it works

For `Double`, the literal is passed straight to a private helper `jsNumber`, written with Kotlin's `js(code = ...)` escape hatch as `Number(literal)`. The engine returns a double already rounded to nearest, so nothing else is needed.

For `Float`, rounding the decimal to a `Double` and then narrowing to `Float` can round twice. So the function delegates to `correctlyRoundedFloat` from [[GhostDecimalExactRounding]], passing a lambda that calls `jsNumber` for the magnitude. That helper strips the sign, narrows with an exact tie check and re-applies the sign. The Wasm actual therefore does not need the big-integer refinement used on Native.

## The code

```kotlin
internal actual fun parseDecimalLiteralToDouble(
    literal: String
): Double = jsNumber(literal = literal)

internal actual fun parseDecimalLiteralToFloat(
    literal: String
): Float = correctlyRoundedFloat(literal = literal) { magnitudeLiteral -> jsNumber(literal = magnitudeLiteral) }

@OptIn(ExperimentalWasmJsInterop::class)
private fun jsNumber(
    literal: String
): Double = js(code = "Number(literal)")
```

The `@OptIn(ExperimentalWasmJsInterop::class)` marks the JavaScript interop as experimental. `jsNumber` is the only place the file touches JavaScript. Note that `literal` here is a non-empty decimal in JSON number syntax produced by the reader, so the JavaScript `Number()` quirks with empty strings or hexadecimal text do not come into play.

## Related

- [[GhostDecimalLiteralParsing]]: the `expect` declarations implemented here.
- [[GhostDecimalExactRounding]]: provides `correctlyRoundedFloat` used for the Float case.
- [[GhostDecimalLiteralParsing.native]]: the Native actual, which has no JS engine and refines its own approximation.
- [[GhostDecimalLiteralParsing.jvmAndroid]]: the JVM actual, which just calls the standard library.
- [[GhostJsonFloatParseHelpers]]: the caller that reaches this slow path.
