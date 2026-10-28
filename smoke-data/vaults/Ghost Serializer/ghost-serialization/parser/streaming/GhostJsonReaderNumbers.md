---
tags: [ghost-serializer, serialization, parser, numbers, streaming]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/streaming/GhostJsonReaderNumbers.kt
kind: functions
---

# GhostJsonReaderNumbers

Number reading for the general reader: `nextInt`, `nextLong`, `nextULong`, `nextFloat`, `nextDouble` and `skipNumber`.

## What it is about

These are extension functions on [[GhostJsonReader]]. They follow the same recipe as the flat reader's numbers ([[GhostJsonFlatReaderNumbers]]) with two differences that come from supporting streams: how a byte is read, and the bookkeeping for error paths.

## How it works

Digits are read through `numberByteAt`, not `getByte`. On a stream the document length is unknown, so reading one byte past a number at the very end would normally fail; `numberByteAt` returns a NUL byte at end of input, which simply ends the number. The shared Core parsers (`parseIntDigitsCore`, `parseLongDigitsCore`, `parseJsonFloatingBodyCore` and the others) are given that accessor as a lambda.

The floating-point readers pass `allowBulkDigitRead = false`. The byte channel can read several digits at once; the streaming reader cannot assume a contiguous array, so it reads one at a time, and the doc comment calls the loop "zero-allocation, register-based".

After every value the function calls `pathTracker.finishScalarValue()`, which updates the JSONPath breadcrumbs used in error messages. Quoted numbers (when string coercion is on) finish with `consumeNumericCoercionFooter`. `nextULong` has its own beginning: it skips whitespace, and if the next byte is a quote it reads the string and calls `toULongOrNull`; otherwise it parses an exact magnitude and rejects a negative non-zero value. `skipNumber` is marked internal API and validates without returning a value.

## The code

```kotlin
fun GhostJsonReader.nextInt(): Int {
    val header = prepareNumericHeader()
    val isQuoted = (header and NUM.NUMERIC_HEADER_QUOTED) != 0
    val isNegativeValue = (header and NUM.NUMERIC_HEADER_NEGATIVE) != 0

    validateLeadingZero()
    val absoluteValue = parseIntDigits(isNegative = isNegativeValue)
    // …
    if (isQuoted) {
        consumeNumericCoercionFooter()
    }
    pathTracker.finishScalarValue()
    return finalIntResult
}
```

## Related

- [[GhostJsonReader]]: the reader and its `numberByteAt`.
- [[GhostJsonFlatReaderNumbers]]: the byte-reader version, with bulk digit reads.
- [[GhostJsonNumberValidationHelpers]]: header, footer and leading-zero cores.
- [[GhostJsonIntegerParseHelpers]]: the integer cores.
- [[GhostJsonFloatParseHelpers]]: floating-point finalization.
- [[GhostJsonPathTracker]]: updated after each scalar.
