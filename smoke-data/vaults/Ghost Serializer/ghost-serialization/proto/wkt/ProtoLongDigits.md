---
tags: [ghost-serializer, serialization, proto, formatting, wkt]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoLongDigits.kt
kind: functions
---

# ProtoLongDigits

Allocation-light decimal formatting of `Long` values, used for quoted int64 JSON strings and the seconds part of a Duration.

## What it is about

Proto3 JSON writes 64-bit integers as quoted strings, and `Duration` renders its seconds as text. This file provides the two helpers for that: `formatLong`, which returns a string, and `writeLongToBytes`, which writes the digits into a caller-supplied byte array and returns the new position. They are internal to the proto module.

## How it works

The digit loop works in negative space: a positive value is negated and digits are extracted from a negative number. The reason is documented in the source: negating `Long.MIN_VALUE` overflows back to itself, which once corrupted the output to `-0`, whereas negative space can represent every value.

The algorithm first counts the digits by dividing by ten until the value fits, then builds a power-of-ten divisor, and finally emits digits from most to least significant. `writeLongToBytes` is `inline` so that `formatLong` and `formatDuration` each get their own copy of the loop, preserving the performance of the earlier version where the code was duplicated by hand.

## The code

```kotlin
internal fun formatLong(
    value: Long
): String {
    val outputBuffer = ByteArray(size = PC.LONG_BUFFER_SIZE)
    val endOffset = writeLongToBytes(
        buffer = outputBuffer,
        startOffset = 0,
        value = value
    )
    return outputBuffer.decodeToString(
        startIndex = 0,
        endIndex = endOffset
```

The sign-handling and counting loop in the inline writer:

```kotlin
    var position = startOffset
    val isNegative = value < 0
    if (isNegative) {
        buffer[position++] = TOK.CHAR_HYPHEN.code.toByte()
    }
    var tempValue = if (isNegative) value else -value
    var digitCount = 1
    while (tempValue <= -WR.BASE_TEN) {
        digitCount++
        tempValue /= WR.BASE_TEN
```

## Related

- [[ProtoInt64ValueSerializer]]: calls `formatLong` to write int64 as a string
- [[ProtoDurationCodec]]: calls `writeLongToBytes` for the seconds field
- [[GhostProtoConstants]]: supplies `LONG_BUFFER_SIZE`
- [[GhostWriterLongDigits]]: the general JSON writer's own long-digit routine
- [[GhostJsonTokens]]: provides the hyphen and zero character codes
