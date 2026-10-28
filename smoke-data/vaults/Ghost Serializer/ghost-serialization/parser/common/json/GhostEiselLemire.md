---
tags: [ghost-serialization, serialization, numbers, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostEiselLemire.kt
kind: functions
---

# GhostEiselLemire

An allocation-free implementation of the Eisel-Lemire algorithm that converts a decimal mantissa and exponent into the raw bits of the nearest `Double`.

## What it is about

Parsing `3.141592653589793` correctly requires rounding a decimal to binary. The exact-power-of-ten shortcut works only up to 10^22, and falling back to string parsing is slow. The Eisel-Lemire algorithm (the one behind `fast_float`, Go's `strconv` and Rust's `core`) handles most of the remaining inputs with a single 64x64 to 128-bit multiplication. The source describes this as a port of Go's `strconv.eiselLemire64`, with bit tricks kept line for line so it can be checked against that code.

## How it works

`eiselLemireToDoubleBits(mantissa, exponent)` returns a `Long`:

1. Zero mantissa gives `0L`; an exponent outside the table range gives `NO_RESULT`.
2. Normalize the mantissa so its top bit is set (`countLeadingZeroBits`), and estimate the binary exponent as `exponent * log2(10)`, using a fixed-point constant of 217706 (Q16).
3. Multiply by `HIGH[index]` from [[GhostEiselLemirePowers]] using `unsignedMultiplyHigh`. If the low bits show the product might be inexact, multiply again by `LOW[index]` to widen it, and bail out if it is still ambiguous.
4. Shift to 54 bits, detect an exact halfway tie, round to even, handle mantissa overflow, and give up on subnormal or infinite exponents.
5. Assemble the exponent and 52-bit fraction.

`NO_RESULT = -1L` is safe as a sentinel because a valid positive double has the sign bit clear. The caller treats it as "use the platform parser" (ties, subnormals, overflow, out-of-table). `unsignedMultiplyHigh` is Hacker's Delight `mulhu`, portable to every Kotlin target since Kotlin lacks a common `multiplyHigh`; `isUnsignedLess` compares unsigned values by flipping the sign bit.

## The code

```kotlin
internal inline fun unsignedMultiplyHigh(
    left: Long,
    right: Long
): Long {
    val leftLow = left and LOW_32_MASK
    val leftHigh = left ushr SHIFT_32
    // …
    return leftHigh * rightHigh + (partial ushr SHIFT_32) + (middle ushr SHIFT_32)
}
```

## Related

- [[GhostEiselLemirePowers]]: the 128-bit powers-of-ten table.
- [[GhostJsonFloatParseHelpers]]: `scaleToDouble` calls `eiselLemireToDoubleBits` and falls back when it returns `NO_RESULT`.
- [[GhostDecimalLiteralParsing]]: the fallback path for undecided inputs.
- [[GhostJsonNumericLimits]]: describes the exact range handled before this algorithm.
