---
tags: [ghost-serialization, serialization, parser, numbers, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonFloatParseHelpers.kt
kind: functions
---

# GhostJsonFloatParseHelpers

The shared kernels that read a JSON floating-point number: collect the digits into a mantissa and exponent, then scale them to a correctly rounded `Double` or `Float`.

## What it is about

Converting text such as `0.30000000000000004` to the right binary value is both performance-critical and easy to get subtly wrong. This file holds the logic common to the flat, streaming and string readers. It follows a staged strategy: do the cheapest exact thing when possible, use the Eisel-Lemire algorithm for most remaining cases, and only fall back to the platform parser for what neither can decide.

## How it works

`parseJsonFloatingBodyCore(...)` walks the integer digits, an optional `.fraction` and an optional exponent. It accumulates up to `precisionLimit` digits (17 for `Double`, 9 for `Float`) into a `Long` mantissa; extra digits increase the exponent instead, and if any dropped digit was nonzero it builds a `truncatedLiteral` string of the original text. Leading zeros do not consume precision slots. When bulk reads are allowed, it grabs eight digits at once with `tryParseEightDigitsAt`, a SWAR routine quoted from `fast_float`; the streaming reader disables this because it could read past the retained window.

`scaleToDouble(mantissa, exponent)`:

1. If the mantissa is at most 2^53 and the exponent is within +-22, multiply or divide by an exact entry of the `POWERS_OF_TEN` table. Large exponents can be folded into the mantissa while it stays exact.
2. Otherwise call `eiselLemireToDoubleBits`.
3. If that returns `NO_RESULT`, parse the text `"${mantissa}e$exponent"` with the platform parser.

`finalizeParsedDouble` and `finalizeParsedFloat` apply the sign and throw "Numeric overflow or NaN is not allowed in JSON" for infinities. For `Float`, `isAmbiguousFloatNarrowing` detects when the rounded `Double` sits exactly halfway between two floats, where narrowing would round twice; only then is the literal parsed directly as a `Float`. `parseExponentValueCore` reads the sign and digits of an exponent, clamping at 1000 to prevent overflow.

## The code

```kotlin
if (mantissa <= NUM.MAX_EXACT_DOUBLE_MANTISSA) {
    if (exponent in 0..NUM.MAX_EXACT_DOUBLE_POWER_OF_TEN) {
        return mantissa.toDouble() * NUM.POWERS_OF_TEN[exponent]
    }
    // …
}
val eiselLemireBits = eiselLemireToDoubleBits(mantissa = mantissa, exponent = exponent)
```

## Related

- [[GhostEiselLemire]]: the middle stage of the strategy.
- [[GhostDecimalLiteralParsing]]: the platform fallback for undecided inputs.
- [[GhostDecimalExactRounding]]: uses `isAmbiguousFloatNarrowing` for Float narrowing.
- [[GhostJsonNumericLimits]]: supplies the power table, precision limits and exact bounds.
- [[GhostJsonNumberValidationHelpers]]: validates the number's syntax before this runs.
- [[GhostJsonFlatReaderNumbers]]: flat reader code calling these kernels.
- [[GhostJsonReaderNumbers]]: streaming reader code calling them.
- [[GhostJsonStringReaderNumbers]]: string reader code calling them.
