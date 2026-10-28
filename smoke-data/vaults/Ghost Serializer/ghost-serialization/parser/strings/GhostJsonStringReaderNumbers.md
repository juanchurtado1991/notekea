---
tags: [ghost-serializer, serialization, parser, numbers, string-channel]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/strings/GhostJsonStringReaderNumbers.kt
kind: functions
---

# GhostJsonStringReaderNumbers

Number reading for the `String` reader: `nextInt`, `nextLong`, `nextULong`, `nextFloat`, `nextDouble` and `skipNumber`.

## What it is about

[[GhostJsonStringReader]] keeps its input as a `CharArray`, so unlike the byte reader its number methods are plain extension functions, not open members. The logic is the same as in [[GhostJsonFlatReaderNumbers]]: the heavy lifting is in shared "Core" functions that take the reader's state as lambdas, so each channel supplies only how to read a unit and move the cursor.

## How it works

Each function runs the same steps. `prepareNumericHeader` reads an optional minus sign and, if `coerceStringsToNumbers` is on, an opening quote, and returns the result as flag bits. `validateLeadingZero` rejects numbers such as `007`. For `Int` and `Long`, `parseIntDigitsCore` and `parseLongDigitsCore` accumulate the digits and detect overflow; a quoted value then calls `consumeNumericCoercionFooter` for the closing quote. The sign is applied at the end.

Floating-point values use `parseJsonFloatingBodyCore` followed by `finalizeParsedDouble` or `finalizeParsedFloat`, which also throw on overflow to infinity. This channel enables `allowBulkDigitRead` as the byte channel does. `nextULong` handles a quoted value through `toULongOrNull`, and reads a bare value as an exact magnitude, rejecting negative non-zero input. `skipNumber` validates the shape (exponent and dot format) without producing a value.

Because the characters are UTF-16 and a number is always ASCII, the lambdas simply use `getByte(index)`, which returns `rawChars[index].code`.

## The code

The integer path:

```kotlin
fun GhostJsonStringReader.nextInt(): Int {
    val header = prepareNumericHeader()
    val isQuoted = (header and NUM.NUMERIC_HEADER_QUOTED) != 0
    val isNegativeValue = (header and NUM.NUMERIC_HEADER_NEGATIVE) != 0

    validateLeadingZero()

    val accumulatedValue = parseIntDigits(isNegative = isNegativeValue)
    // …
    nextTokenByte = SCN.RESET_TOKEN_BYTE

    return if (isNegativeValue) -accumulatedValue else accumulatedValue
}
```

## Related

- [[GhostJsonStringReader]]: the reader extended.
- [[GhostJsonFlatReaderNumbers]]: the same logic for the byte reader.
- [[GhostJsonNumberValidationHelpers]]: header, leading-zero and footer cores.
- [[GhostJsonIntegerParseHelpers]]: the integer digit cores.
- [[GhostJsonFloatParseHelpers]]: floating-point parsing and finalization.
- [[GhostJsonNumericLimits]]: the constants (`NUM`) used for flags and limits.
