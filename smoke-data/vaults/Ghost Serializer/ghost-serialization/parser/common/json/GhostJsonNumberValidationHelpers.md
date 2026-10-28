---
tags: [ghost-serialization, serialization, parser, numbers, kernels]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonNumberValidationHelpers.kt
kind: functions
---

# GhostJsonNumberValidationHelpers

Inline kernels that check or skip the syntax of a JSON number without producing a value: the optional quote and minus, leading zeros, and the digits of integer, fraction and exponent.

## What it is about

Reading a number has two halves: validating its shape and computing its value. The value computation differs by type, but validation is the same for every reader and every numeric type, so it lives here. Skipping an unwanted number needs the same grammar, so the skip routine is here too. Reader state comes in through lambdas, as with the other kernels.

## How it works

- `prepareNumericHeaderCore(...)` skips whitespace, throws "Expected number but reached EOF" if nothing remains, and handles an optional quote (only if string coercion is enabled, otherwise "Unexpected string for numeric type (coercion disabled)") and an optional minus (an isolated minus at the end gives "Isolated minus sign"). It returns a bit mask: `NUMERIC_HEADER_QUOTED` (1) and/or `NUMERIC_HEADER_NEGATIVE` (2).
- `consumeNumericCoercionFooterCore` requires the closing quote of a coerced number.
- `validateLeadingZeroCore` throws "Leading zeros are not allowed" when `0` is followed by another digit.
- `skipNumberBodyCore` walks a number: a single `0` or a digit run (error if none), an optional `.` with at least one digit, an optional `e` or `E` with an optional sign and at least one digit, then stores the end position. Streaming keeps its own skip to avoid coupling to the Okio buffer.

Because the validators work on a `getByte` lambda and indices, they are shared by flat, string and streaming readers with no virtual call.

## The code

```kotlin
val hasLeadingZeroFollowedByMore = position < limit && getByte(position) == TOK.ZERO_INT && position + 1 < limit
if (hasLeadingZeroFollowedByMore) {
    val nextDigitByte = getByte(position + 1)
    if (nextDigitByte in TOK.ZERO_INT..TOK.NINE_INT) {
        throwError(EM.ERR_LEADING_ZEROS)
    }
}
```

## Related

- [[GhostJsonIntegerParseHelpers]]: consumes the header result before accumulating digits.
- [[GhostJsonFloatParseHelpers]]: does the same for floating-point values.
- [[GhostNumericParseHelpers]]: provides `isDigit`.
- [[GhostJsonErrorMessages]]: supplies the messages.
- [[GhostJsonNumericLimits]]: defines the header bit values.
- [[GhostJsonScanConstants]]: defines `RESET_TOKEN_BYTE`.
- [[GhostJsonFlatReaderNumbers]]: a reader's number code that calls these kernels.
