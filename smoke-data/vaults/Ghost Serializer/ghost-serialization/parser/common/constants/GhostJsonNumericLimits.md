---
tags: [ghost-serialization, serialization, parser, constants, numbers]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/constants/GhostJsonNumericLimits.kt
kind: object
---

# GhostJsonNumericLimits

The numeric thresholds of Ghost's parser and formatter: overflow bounds for integers, precision limits for floating point, depth limit and a table of powers of ten.

## What it is about

Parsing a number safely means knowing exactly when the next digit would overflow, how many digits a `Double` can carry, and how deep nested JSON may go. Instead of scattering those numbers through the readers, they are declared here with names (the code imports it as `NUM`). The object is public so generated code and every reader channel can share it.

## How it works

Groups of constants:

- Overflow checks: `INT_OVERFLOW_LIMIT = 214748364` and `LONG_OVERFLOW_LIMIT = 922337203685477580` (the value just before the last digit may overflow), with last-digit limits for the positive (7) and negative (8) cases. `*_MAX_MAGNITUDE` and `*_MIN_MAGNITUDE` are `ULong` bounds used when an exponent form is read as an integer; `ULONG_OVERFLOW_LIMIT` and `ULONG_MAX_LAST_DIGIT` do the same for `ULong`.
- Safe digit counts: `INT_SAFE_DIGITS = 9` and `LONG_SAFE_DIGITS = 18`, below which accumulation cannot overflow so no check is made.
- Floating point: `DOUBLE_PRECISION_LIMIT = 17` and `FLOAT_PRECISION_LIMIT = 9` digits; `MAX_EXACT_DOUBLE_POWER_OF_TEN = 22` (the largest `n` for which `10^n` is exactly a `Double`); `MAX_EXACT_DOUBLE_MANTISSA = 2^53`; `EXPONENT_CLAMP_THRESHOLD = 1000` to stop exponent digits from overflowing.
- `MAX_DEPTH = 255`: the maximum nesting of arrays and objects.
- Literal strings for the minimum `Int` and `Long`.
- `POWERS_OF_TEN`: a `DoubleArray` of 309 entries filled at class initialization with `10.0.pow(i)`, so scaling needs no call to `pow`.

## The code

```kotlin
const val MAX_EXACT_DOUBLE_POWER_OF_TEN = 22

val POWERS_OF_TEN = DoubleArray(size = 309).apply {
    for (i in indices) this[i] = 10.0.pow(x = i.toDouble())
}
```

The KDoc explains that beyond 10^22 the table entries are rounded themselves, so multiplying by them stops being correctly rounded, and the parser must switch to Eisel-Lemire or a platform parse.

## Related

- [[GhostNumericParseHelpers]]: uses the overflow limits in the digit accumulators.
- [[GhostJsonFloatParseHelpers]]: uses the power-of-ten table, precision limits and exact-mantissa bound.
- [[GhostJsonIntegerParseHelpers]]: uses the safe-digit counts and magnitudes.
- [[GhostEiselLemire]]: takes over where the exact bounds end.
- [[GhostJsonValueCaptureScan]]: uses `MAX_DEPTH` to size its container bitset.
- [[GhostDoubleFormatter]]: uses the formatting precision limits when writing.
