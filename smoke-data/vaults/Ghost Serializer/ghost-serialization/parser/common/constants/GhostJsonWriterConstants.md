---
tags: [ghost-serialization, serialization, writer, constants, utf8]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/constants/GhostJsonWriterConstants.kt
kind: object
---

# GhostJsonWriterConstants

The constants of Ghost's output side and of its UTF-8 handling: pre-encoded literals, buffer sizes, escape tables and the arithmetic constants of UTF-8 encoding.

## What it is about

Writing JSON fast means never rebuilding `true`, `null` or `":` from characters, and never recomputing which bytes need escaping. This public `object` (aliased `WR` across the code) holds all of that. Despite the name, readers use it as well: its decimal base, UTF-8 sizes and buffer constants are shared by both directions.

## How it works

Main groups:

- Pre-encoded `ByteString`s and arrays (`@PublishedApi internal`): `TRUE_BS`, `FALSE_BS`, `NULL_BS`, the minimum `Int` and `Long`, `DOT_ZERO`, `COLON_QUOTE_BS` and the `type` key, so writers append them with a single copy.
- Buffer sizing: `TIER_SMALL_INT = 1024`, `BUFFER_SCALE_FACTOR = 2`, `INITIAL_WRITE_BUFFER_SIZE` (8 KiB), `STREAMING_BUFFER_SIZE` (8192, one Okio segment, with the reasoning that bigger windows did not help), `PLAIN_ASCII_FAST_PATH_LIMIT = 512`.
- Digit tables: `DOUBLE_DIGIT_LUT` and `DOUBLE_DIGIT_LUT_CHARS`, 200 entries holding the two digits of every number 00 to 99.
- Escape tables: `NEEDS_ESCAPE_MASK_LOW` and `NEEDS_ESCAPE_MASK_HIGH` form `ESCAPE_MASKS`, a two-`Long` bitmap over ASCII 0 to 127 marking control characters, the quote and the backslash. `ESCAPE_REPLACEMENTS` maps those bytes to their two-byte escape such as `\n`.
- UTF-8 arithmetic: limits (`0x80`, `0x800`), lead-byte prefixes (`0xC0`, `0xE0`, `0xF0`), continuation prefix and mask, shifts of 6, 12 and 18, payload masks, byte widths 1 to 4 and the replacement character bytes.

## The code

```kotlin
const val NEEDS_ESCAPE_MASK_LOW = 0x4FFFFFFFFL
const val NEEDS_ESCAPE_MASK_HIGH = 0x10000000L
val ESCAPE_MASKS = longArrayOf(NEEDS_ESCAPE_MASK_LOW, NEEDS_ESCAPE_MASK_HIGH)
```

The low mask has bits 0 to 31 (control characters) plus bit 34 (the quote, code 34); the high mask has bit 28, which is code 92, the backslash. A scanner tests a byte with one shift and one AND.

## Related

- [[GhostSourceCommon]]: uses `ESCAPE_MASKS` in the closing-quote and string scans.
- [[GhostUtf8PositionBridge]]: uses the UTF-8 size constants.
- [[GhostJsonUtf8Input]]: uses the UTF-8 prefixes and shifts to re-encode UTF-16 and UTF-32.
- [[GhostJsonQuotedStringSlow]]: uses them to append decoded code points.
- [[GhostFormatUtils]]: uses `BASE_TEN` and `ASCII_OFFSET`.
- [[FlatByteArrayWriter]]: one of the writers using the literals and sizes.
- [[GhostBufferGrowth]]: uses `BUFFER_SCALE_FACTOR`.
