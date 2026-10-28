---
tags: [ghost-serialization, serialization, parser, constants, swar]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/constants/GhostJsonScanConstants.kt
kind: object
---

# GhostJsonScanConstants

The named constants of Ghost's scanning layer: SWAR masks, sentinel values, unroll offsets, perfect-hash defaults, string-pool sizes and the layout of a packed scan result.

## What it is about

Fast scanners are full of "magic" numbers: `0x0101010101010101`, shifts of 56 and 48, table sizes, sentinel `-1`. Ghost keeps them in one public `object` so the code that uses them reads as words (`SCN.MATCH_END`, `SCN.UNROLL_OFFSET_2`) and every reader channel agrees on the same values. Almost every parser and writer file imports it under the alias `SCN`.

## How it works

The constants fall into groups:

- Sentinels: `MATCH_END = -1` (end of input or no match), `RESET_TOKEN_BYTE = -1`, `MATCH_NONE = -2` (a key that matched no field), `UNKNOWN_LINE_COLUMN`.
- Whitespace: `WHITESPACE_MASK`, a `Long` with bits set for space (32), LF (10), CR (13) and tab (9), tested with `(mask shr byte) and 1`.
- SWAR: `SWAR_ONES`, `SWAR_HIGHS`, `SWAR_QUOTES`, `SWAR_BACKSLASHES`, `SPACE_RUN_LONG`, `LONG_BYTES = 8`, `LONG_CHARS = 4`, byte offsets and shift amounts.
- Unrolling: `UNROLL_STEP = 4` and offsets 1 to 3 for the manual four-way loops.
- Dispatch and hashing: default perfect-hash shift, multiplier (31) and table size (1024), `KEY_HASH_WORD_BYTES = 4`, the string pool size (4096 buckets) and the word-hash constants (golden-ratio multiplier, rotation, finalizer).
- Bitmask depth: `MAX_BITMASK_DEPTH = 64`, used by the comma-validation masks.
- Eight-digit parsing: constants quoted from the fast_float library, said in the source to be checked against all 100,000,000 inputs, plus `HUNDRED_MILLION`.

The `packScanResult(length, hash, is7Bit)` function layers three fields into one `Long`: hash in the low 32 bits, length from bit 32, and a 7-bit flag at bit 63. It is `@PublishedApi internal` so inline callers can use it.

## The code

```kotlin
const val WHITESPACE_MASK = (1L shl 32) or (1L shl 10) or (1L shl 13) or (1L shl 9)

@PublishedApi
internal fun packScanResult(length: Int, hash: Int, is7Bit: Boolean): Long {
    var result = (length.toLong() shl SCAN_LENGTH_SHIFT) or (hash.toLong() and SCAN_HASH_MASK)
    if (is7Bit) result = result or SCAN_7BIT_BIT
    return result
}
```

(The real signature spreads its parameters over several lines.)

## Related

- [[GhostSourceCommon]]: scanning kernels that use the SWAR masks, offsets and packing.
- [[GhostSource]]: its `scanString` returns the packed layout described here.
- [[JsonReaderOptions]]: uses the dispatch defaults and field-prediction constants.
- [[GhostJsonKeySelectHelpers]]: uses the key-hash word size and collision multiplier.
- [[GhostJsonCommaValidationHelpers]]: uses the bitmask depth and `BITMASK_UNIT`.
- [[GhostJsonFloatParseHelpers]]: uses the eight-digit constants.
- [[GhostSwar]]: the platform helpers that read the words these masks operate on.
