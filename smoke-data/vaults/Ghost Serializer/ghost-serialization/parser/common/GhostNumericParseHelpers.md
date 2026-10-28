---
tags: [ghost-serialization, serialization, parser, numbers]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostNumericParseHelpers.kt
kind: functions
---

# GhostNumericParseHelpers

Small inline building blocks for parsing integers: overflow-checked digit accumulation, safe narrowing to smaller integer types, and three byte predicates.

## What it is about

The JSON and YAML readers parse numbers byte by byte. The pieces that every integer parser needs, and that do not depend on which reader is running, are collected here. All functions are `internal inline`, so each call site compiles down to the same machine code as hand-written arithmetic.

## How it works

Accumulators:

- `accumulateIntWithOverflowCheck(current, digitValue, isNegative, onOverflow)` multiplies by ten and adds a digit, but first checks whether `current` is already past `INT_OVERFLOW_LIMIT` (214748364), or equal to it with a last digit above 7 (or 8 for negatives, so `Int.MIN_VALUE` is allowed). On overflow it calls the lambda, which must throw.
- `accumulateLongWithOverflowCheck` is the same for `Long`, with a special case: when a negative number reaches `922337203685477580` and the digit is 8, it returns `Long.MIN_VALUE` instead of failing.

Narrowing helpers take an already-parsed `Int` (or `Long` for `UInt`) and return `Byte`, `Short`, `UByte`, `UShort` or `UInt`. Out-of-range values call `throwError` with messages such as "Byte overflow", and unsigned types reject negatives with a dedicated message, instead of silently wrapping.

Predicates:

- `isDigit(byteCode)` uses `(byteCode xor '0') < 10`, one comparison for the whole digit range.
- `isExponentMarker` ORs with the case mask to match `e` and `E`.
- `isNumericSeparator` is true for `.`, `e` or `E`.

## The code

```kotlin
internal inline fun isDigit(
    byteCode: Int
): Boolean {
    return (byteCode xor TOK.ZERO_INT) < WR.BASE_TEN
}

internal inline fun narrowToByte(
    value: Int,
    throwError: (String) -> Nothing,
): Byte {
    val isOutOfRange = value < Byte.MIN_VALUE || value > Byte.MAX_VALUE
    if (isOutOfRange) {
        throwError(EM.ERR_BYTE_OVERFLOW)
    }
    return value.toByte()
}
```

The `isDigit` trick works because the digit codes 48..57 differ from 48 only in their low four bits, so XOR with 48 gives 0..9 for digits and a larger number for anything else (for non-negative inputs).

## Related

- [[GhostJsonIntegerParseHelpers]]: uses the accumulators and the separator predicates in its digit loops.
- [[GhostJsonNumberValidationHelpers]]: uses `isDigit` while validating number syntax.
- [[GhostJsonErrorMessages]]: provides the overflow messages.
- [[GhostJsonNumericLimits]]: provides the overflow limits.
- [[GhostJsonFlatReader]]: calls the narrowing helpers when reading small integer types.
- [[UIntSerializer]]: an unsigned serializer that relies on the narrowing helpers.
