---
tags: [ghost-serialization, serialization, parser, numbers, kernels]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonIntegerParseHelpers.kt
kind: functions
---

# GhostJsonIntegerParseHelpers

Inline kernels that read `Int` and `Long` values from a run of digits, with a precise slow path for exponent forms like `1e2`.

## What it is about

Integers dominate real JSON, so their parsing is the hottest numeric code in Ghost. The flat, streaming and string readers all need the same logic, supplied here as `internal inline` functions parameterized by lambdas (`getByte`, `getPosition`, `setPosition`, `throwError`). Following kotlinx.serialization, an exponent is applied exactly (`1e2` is 100, `100e-2` is 1) while a fraction, a non-integral result or an out-of-range value throws rather than truncating.

## How it works

The fast path is `parseIntDigitsCore` (and `parseLongDigitsCore`). It loops over bytes, and for each digit:

- while fewer than 9 (`Int`) or 18 (`Long`) digits have been seen, it simply computes `value * 10 + digit`, since overflow is impossible;
- afterwards it calls the overflow-checked accumulators from [[GhostNumericParseHelpers]].

If it meets `.`, `e` or `E` (`isNumericSeparator`), or the `Int` accumulator overflows, it re-reads the whole literal through the cold path. If no digit is found, it throws "Expected integer part of number".

The cold path starts with `parseIntegralMagnitudeCore`, which reads the digits again as a `ULong` magnitude (overflow checked against the `ULong` limit), rejects a `.` with "Expected an integer but found a fractional number", and if an exponent marker follows, parses it with `parseExponentValueCore` and applies `scaleMagnitudeByPowerOfTen`. That function multiplies while checking against `ULong.MAX_VALUE / 10`, or divides while requiring that no nonzero digit is dropped (loops are bounded to about 20 steps). `parseIntegralLiteralAsIntCore` and `parseIntegralLiteralCore` then check the magnitude against the sign-dependent maximum (`Int.MIN_VALUE` and `Long.MIN_VALUE` magnitudes are allowed for negatives).

## The code

```kotlin
accumulatedValue = if (digitCount < NUM.INT_SAFE_DIGITS) {
    accumulatedValue * WR.BASE_TEN + digit
} else {
    accumulateIntWithOverflowCheck(
        current = accumulatedValue,
        digitValue = digit,
        isNegative = isNegative
    ) {
        return parseIntegralLiteralAsIntCore(/* … */)
    }
}
```

## Related

- [[GhostNumericParseHelpers]]: supplies the overflow-checked accumulators and `isDigit` predicates.
- [[GhostJsonFloatParseHelpers]]: provides `parseExponentValueCore`, reused for exponents.
- [[GhostJsonNumericLimits]]: defines safe-digit counts and magnitudes.
- [[GhostJsonErrorMessages]]: supplies overflow and fraction messages.
- [[GhostJsonFlatReaderNumbers]]: flat reader entry points.
- [[GhostJsonReaderNumbers]]: streaming reader entry points.
- [[GhostJsonStringReaderNumbers]]: string reader entry points.
