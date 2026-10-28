---
tags: [ghost-serialization, serialization, writer, formatting]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostFormatUtils.kt
kind: object
---

# GhostFormatUtils

An internal object holding two precomputed 100-entry tables used to print numbers two digits at a time.

## What it is about

Writing an integer or double as ASCII is a hot operation in a serializer. A classic speed-up is to split the number into pairs of decimal digits and look each pair up in a table rather than dividing by ten once per digit. This file builds the two lookup tables: one for the tens digit and one for the ones digit of every value from 0 to 99.

## How it works

`DIGIT_TENS` and `DIGIT_ONES` are `ByteArray`s of size 100 (`DIGIT_PAIR_TABLE_SIZE`). An `init` block fills them once, when the object is first used:

- `DIGIT_TENS[i] = i / 10 + 48`
- `DIGIT_ONES[i] = i % 10 + 48`

The constants come from `GhostJsonWriterConstants`: `BASE_TEN` (10) and `ASCII_OFFSET` (48, the code of `'0'`). So `DIGIT_TENS[47]` is the byte for `'4'` and `DIGIT_ONES[47]` is the byte for `'7'`. A formatter that has reduced a value to a remainder below 100 can emit two output bytes with two array reads and no division.

The tables are built from loops, not literals, so the object stays short and obviously correct, and the cost is paid once per process.

## The code

```kotlin
internal object GhostFormatUtils {
    private const val DIGIT_PAIR_TABLE_SIZE = 100

    val DIGIT_TENS = ByteArray(DIGIT_PAIR_TABLE_SIZE)
    val DIGIT_ONES = ByteArray(DIGIT_PAIR_TABLE_SIZE)

    init {
        for (i in 0 until DIGIT_PAIR_TABLE_SIZE) {
            DIGIT_TENS[i] = ((i / GhostJsonWriterConstants.BASE_TEN) + GhostJsonWriterConstants.ASCII_OFFSET).toByte()
            DIGIT_ONES[i] = ((i % GhostJsonWriterConstants.BASE_TEN) + GhostJsonWriterConstants.ASCII_OFFSET).toByte()
        }
    }
}
```

`GhostJsonWriterConstants` also exposes a combined `DOUBLE_DIGIT_LUT` (200 bytes, tens then ones interleaved); these two arrays are the split form used by the double formatter.

## Related

- [[GhostDoubleFormatter]]: the consumer of these tables.
- [[GhostJsonWriterConstants]]: provides `BASE_TEN` and `ASCII_OFFSET`, and the interleaved lookup table.
- [[GhostWriterLongDigits]]: another digit-writing helper in the writer path.
- [[FlatByteArrayWriter]]: a writer that produces the numeric output.
