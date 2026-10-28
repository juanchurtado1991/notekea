---
tags: [ghost-serializer, serialization, parser, fast-path]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/serializers/GhostFastPrimitiveArrayHelpers.kt
kind: functions
---

# GhostFastPrimitiveArrayHelpers

A set of inline helpers that decode compact primitive arrays in one tight loop and write array elements.

## What it is about

These helpers are the speed trick behind Ghost's primitive array serializers. Large numeric arrays such as `[1,2,3,...]` dominate the cost of many payloads, and the normal path pays for `hasNext()` and comma bookkeeping on every element. The helpers scan a compact run directly over the input bytes, and give up (returning `null` or `false`) the moment the input does not look like the simple, common shape. The same code serves all three input channels because the caller passes the byte access as lambdas.

## How it works

Every `tryFast*` function is `inline` and takes `getByte` and `setPosition` lambdas, so each reader channel gets its own specialised copy without an allocation or a virtual call. The functions are called right after `beginArray()` has confirmed that the array is not empty. They walk from `startPosition`, accumulate values into a primitive list ([[GhostIntList]], [[GhostLongList]], [[GhostBooleanList]]) and stop at `]`.

If anything does not fit (whitespace, a decimal point or exponent in an integer run, too many digits, coerced booleans, a missing comma), the helper resets the position to the start with `setPosition(startPosition)` and returns `null`. The caller then runs its general loop, which stays the single source of truth for overflow and error messages. On success the position is left at the closing `]`, and the caller still calls `endArray()`.

For `Double` and `Float` the file does not reimplement number parsing. `tryFastDecimalArrayCore` only checks that the next byte could start a number (a digit or `-`), then calls a `parseNext` lambda (the reader's own `nextDouble` or `nextFloat`) and checks for a comma or `]` afterwards.

## The code

`matchesLiteral` compares the bytes at a position with an okio `ByteString`, after bounds checking:

```kotlin
    val size = literal.size
    if (pos + size > limit) return false
    for (i in 0 until size) {
        if (getByte(pos + i) != (literal[i].toInt() and TOK.BYTE_MASK)) return false
    }
```

`tryFastBooleanArrayCore` accepts only bare `true` and `false`, so `1`/`0` or quoted strings fall back to the coercing path. `tryFastIntArrayCore` and `tryFastLongArrayCore` parse the digits themselves, giving up when the digit count reaches `INT_SAFE_DIGITS` or `LONG_SAFE_DIGITS`, which avoids having to detect overflow in the fast path:

```kotlin
            if (digitCount >= NUM.INT_SAFE_DIGITS) {
                setPosition(startPosition)
                return null
            }
            value = value * WR.BASE_TEN + (b - TOK.ZERO_INT)
            digitCount++
            pos++
        }
```

`tryFastDecimalArrayCore` fills a caller-supplied `MutableList` and returns a boolean. Finally `writeArrayElements` is the trivial shared write loop: it calls `writeAt(i)` for each index and leaves separator handling to `writer.value(...)`:

```kotlin
internal inline fun writeArrayElements(
    size: Int,
    writeAt: (Int) -> Unit
) {
    for (i in 0 until size) writeAt(i)
}
```

## Related

- [[IntArraySerializer]]: calls `tryFastIntArrayCore` and `writeArrayElements`.
- [[LongArraySerializer]]: calls `tryFastLongArrayCore` and `writeArrayElements`.
- [[DoubleArraySerializer]]: calls `tryFastDecimalArrayCore` with `nextDouble`.
- [[FloatArraySerializer]]: calls `tryFastDecimalArrayCore` with `nextFloat`.
- [[BooleanArraySerializer]]: calls `tryFastBooleanArrayCore`.
- [[GhostIntList]]: the buffer the integer fast path fills.
- [[GhostLongList]]: the buffer the long fast path fills.
- [[GhostBooleanList]]: the buffer the boolean fast path fills.
