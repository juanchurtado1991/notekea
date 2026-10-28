---
tags: [ghost-serialization, serialization, numbers, lookup-table]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostEiselLemirePowers.kt
kind: object
---

# GhostEiselLemirePowers

The large lookup table of 128-bit approximations of powers of ten that the Eisel-Lemire float-parsing algorithm multiplies against.

## What it is about

To turn `mantissa x 10^exponent` into the nearest `Double`, the Eisel-Lemire algorithm (Lemire, "Number Parsing at a Gigabyte per Second", 2021) needs `10^exponent` to about 128 bits of precision. Computing that on the fly would be slow, so the values are precomputed and stored. This file is that table, and nothing else: most of its ~365 lines are numeric literals. It is `internal` and only read by [[GhostEiselLemire]].

## How it works

Two `LongArray`s hold the entries, one per half of the 128-bit value:

- `HIGH[i]`: the upper 64 bits, normalized so the top bit is always set;
- `LOW[i]`: the lower 64 bits.

Entry `i` corresponds to `10^(i + MIN_EXPONENT)`, where `MIN_EXPONENT = -348` and `MAX_EXPONENT = 347`, so each array covers 696 exponents. The source says the values were generated with exact rational arithmetic and then truncated to 128 bits. Because the numbers are unsigned 64-bit words stored in signed `Long`s, many literals print as negative; the algorithm treats them as unsigned.

The lookup is a single array read: `tableIndex = exponent - MIN_EXPONENT`, then `HIGH[tableIndex]` and, only in the rare wide case, `LOW[tableIndex]`. Using static arrays means no computation at startup beyond class loading, and no allocation per parse.

## The code

```kotlin
internal object GhostEiselLemirePowers {
    const val MAX_EXPONENT = 347
    const val MIN_EXPONENT = -348

    val HIGH: LongArray = longArrayOf(
        -391859759250406776L, -7162441377172586091L, -4341365703038344710L, -815021110370542984L,
        // …
    )

    val LOW: LongArray = longArrayOf(
        // …
    )
}
```

Exponents outside the range are not handled by the table; the algorithm returns "no result" for them and the caller falls back to the platform parser.

## Related

- [[GhostEiselLemire]]: the only consumer; imports this object as `POW`.
- [[GhostJsonFloatParseHelpers]]: reaches the table indirectly, through `scaleToDouble`.
- [[GhostJsonNumericLimits]]: the exact-arithmetic bounds that apply before this table is used.
