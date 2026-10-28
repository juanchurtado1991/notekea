---
tags: [ghost-serializer, serialization, writer, numbers, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/common/GhostDoubleFormatter.kt
kind: object
---

# GhostDoubleFormatter

A zero-allocation formatter that writes `Double` and `Float` values as ASCII bytes, with a safe fallback.

## What it is about

Calling `Double.toString()` for every number creates garbage and is slow on hot encode paths. `GhostDoubleFormatter` writes common values straight into a byte buffer. It only handles the range where it can be exact; for everything else it tells the caller to fall back to the platform `toString()`.

## How it works

`writeDoubleDirect` and `writeFloatDirect` return the number of bytes written, or `FALLBACK_REQUIRED` (`-1`). The steps: reject non-finite values; write a leading `-` for negative values; send small whole numbers (up to 1e9) to `writeLongDirect` with a trailing `.0`; give up for numbers above 1e9 or below 1e-9; split into integer and fraction, scale the fraction to 9 decimals (7 for `Float`), and give up if it rounds to 0 or a whole number.

A fixed decimal count is not automatically round-trip safe (`0.1 + 0.2` would print as `0.3` and read back as a different double). So after trimming trailing zeros, `doubleRoundTrips` (or `floatRoundTrips`) rebuilds the value with the parser's own `finalizeParsedDouble` or `finalizeParsedFloat` and compares raw bits. Any mismatch, or a digit count the parser would truncate, falls back. Digits are then written two at a time from a lookup table.

## The code

```kotlin
var fracInt = (fracPart * PRECISION_MULTIPLIER).roundToInt()
val isFractionLostAtThisPrecision = fracInt == 0 || fracInt >= FRAC_LIMIT
if (isFractionLostAtThisPrecision) {
    return FALLBACK_REQUIRED
}
// …
if (!doubleRoundTrips(
    intPart = intPart,
    fracInt = fracInt,
    decimalsToPrint = decimalsToPrint,
    expected = localValue
)) {
    return FALLBACK_REQUIRED
}
```

`writeLongDirect` writes the integer part backward into a 32-byte scratch zone using digit tables, then moves it into place with one `copyInto`, optionally appending `.0`. The callers (`GhostJsonWriterHelpers` and `GhostJsonStringWriter`) throw for NaN and infinity and use `toString()` for every other fallback.

## Related

- [[GhostJsonWriterHelpers]]: calls it from the shared double and float kernels
- [[GhostJsonStringWriter]]: uses it on the char channel, widening bytes to chars
- [[GhostJsonFloatParseHelpers]]: provides `finalizeParsedDouble` and `finalizeParsedFloat` used for the round-trip check
- [[GhostWriterLongDigits]]: sibling helper for integer digits
- [[GhostFormatUtils]]: supplies the digit tables
