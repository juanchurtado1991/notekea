---
tags: [ghost-serialization, serialization, parser, swar, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostSourceCommon.kt
kind: functions
---

# GhostSourceCommon

The shared scanning kernels behind every `GhostSource`: skip whitespace, find a closing quote, hash and scan a string, and compare bytes to a string, all tuned for speed.

## What it is about

Whether a document sits in a `ByteArray` or arrives through Okio, the parser does the same low-level jobs thousands of times per document. This file holds those loops once, as `inline` functions that take a `getByte` lambda, plus a few functions that work directly on an array with SWAR (SIMD within a register) tricks. The byte-array and streaming sources call these kernels instead of repeating them.

## How it works

Generic kernels (`internal inline`, driven by a `getByte` lambda so each source supplies its own access):

- `findNextNonWhitespaceImpl` and `findClosingQuoteImpl` unroll four bytes per iteration, then finish with a one-byte loop. Whitespace and "special byte" tests use bitmasks: a `Long` mask for space/tab/LF/CR, and a two-`Long` table for bytes needing escape.
- `scanStringImpl` does the closing-quote scan while accumulating a rolling hash (`hash * 31 + byte`, written as a shift minus hash) and tracking whether every byte was below 128. It packs length, hash and the 7-bit flag into one `Long`, or returns -1 on an escape or control byte.
- `contentEqualsStringImpl` compares a slice to a `String`, unrolled by four.

Array-specific functions:

- `scanStringSwarNoHash` reads eight bytes at a time with `ghostReadLong8`, detects a quote, backslash or control byte with the McIlroy zero-byte test (`swarHasZeroByte`) and checks the high bits for non-ASCII; only the word containing a boundary is re-scanned bytewise. It skips the hash, since long values are rarely pooled.
- `rollingHashImpl` recomputes the same hash over a short span, sharing `accumulateHash` with `scanStringImpl` so the two stay identical.
- `wordPoolHashImpl` hashes eight bytes per step with a multiply-and-rotate mix and a final avalanche, masking the tail word.

## The code

```kotlin
internal inline fun swarHasZeroByte(
    v: Long
): Long = (v - SCN.SWAR_ONES) and v.inv() and SCN.SWAR_HIGHS
```

XOR-ing a word with a broadcast quote (`0x2222…`) turns quote bytes into zero bytes, which this test then finds without branching.

## Related

- [[ByteArrayGhostSource]]: uses these kernels for the in-memory source.
- [[StreamingGhostSource]]: shares the predicates `isNonWhitespace` and `isEscapeOrControlByte`.
- [[GhostSource]]: the interface whose methods these kernels implement.
- [[GhostJsonScanConstants]]: SWAR masks, unroll offsets and scan-result packing.
- [[GhostJsonWriterConstants]]: provides `ESCAPE_MASKS`.
- [[GhostJsonFlatReaderStrings]]: uses the pool hash and the SWAR scan for strings.
- [[GhostSwar]]: platform SWAR helpers such as reading a long.
