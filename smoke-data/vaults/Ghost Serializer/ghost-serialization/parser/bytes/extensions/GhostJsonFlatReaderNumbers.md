---
tags: [ghost-serializer, serialization, parser, numbers]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/bytes/extensions/GhostJsonFlatReaderNumbers.kt
kind: functions
---

# GhostJsonFlatReaderNumbers

The number readers for the byte channel: `Int`, `Long`, `ULong`, `Float`, `Double` and `skipNumber`.

## What it is about

[[GhostJsonFlatReader]] declares `nextInt`, `nextLong`, `nextFloat` and `nextDouble` as open methods that delegate here, to the `...Extension` functions. Keeping the bodies in extensions keeps the class file small, and lets the proto reader override the public methods while still reusing these implementations.

## How it works

Every reader follows one shape. `prepareNumericHeader` handles the leading sign and, when string coercion is enabled, the opening quote of a number written as a string; it returns a small bit set (`NUMERIC_HEADER_QUOTED`, `NUMERIC_HEADER_NEGATIVE`). `validateLeadingZero` rejects forms such as `01`. The digits are parsed by shared "Core" functions that take the reader's state as lambdas, so the same logic serves all channels; for integers they accumulate and report overflow. If the value was quoted, `consumeNumericCoercionFooter` eats the closing quote and following whitespace.

Floating-point values go through `parseJsonFloatingBodyCore`, which produces a mantissa, decimal exponent and a flag for a truncated literal, then `finalizeParsedDouble` or `finalizeParsedFloat` scales them to the final value; a literal with too many digits is re-parsed from its text, and an infinite or NaN result raises an overflow error. The byte channel passes `allowBulkDigitRead = true`, which lets it read several digits per step; the streaming reader cannot.

`nextULongExtension` is special: a quoted value goes through `toULongOrNull`, a bare one parses an exact magnitude over the full range and rejects negative non-zero values.

## The code

The shape of an integer read:

```kotlin
fun GhostJsonFlatReader.nextIntExtension(): Int {
    val header = prepareNumericHeader()
    val isQuoted = (header and NUM.NUMERIC_HEADER_QUOTED) != 0
    val isNegativeValue = (header and NUM.NUMERIC_HEADER_NEGATIVE) != 0

    validateLeadingZero()
    val absoluteValue = parseIntDigits(isNegative = isNegativeValue)
    // …
    if (isQuoted) {
        consumeNumericCoercionFooter()
    }
    return finalIntResult
}
```

`nextLongExtension` has the extra case that the absolute value `Long.MIN_VALUE` is returned unchanged rather than negated.

## Related

- [[GhostJsonFlatReader]]: the reader and its open number methods.
- [[GhostJsonNumberValidationHelpers]]: leading-zero, header and footer checks.
- [[GhostJsonIntegerParseHelpers]]: digit accumulation and overflow.
- [[GhostJsonFloatParseHelpers]]: mantissa, exponent and finalization.
- [[GhostDecimalLiteralParsing]]: the exact parse used for literals with more digits than the mantissa holds.
- [[ProtoJsonFlatReaderNumbers]]: proto3 wrappers around these extensions.
- [[GhostJsonStringReaderNumbers]]: the `String` reader's counterpart.
