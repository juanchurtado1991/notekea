---
tags: [ghost-serializer, serialization, parser, swar, string-channel]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/strings/GhostCharSwar.kt
kind: functions
---

# GhostCharSwar

Two tiny helpers that pack four UTF-16 characters into one `Long`, the character-array version of the SWAR trick used on bytes.

## What it is about

The `String` reader ([[GhostJsonStringReader]]) works on a `CharArray`, not on bytes. To match field names quickly it needs the same "compare several units in one go" idea as [[GhostSwar]], but with 16-bit lanes. This file provides the packing function and its masks. Unlike the byte version, it needs no `expect`/`actual`, because the layout is chosen in plain Kotlin rather than taken from the platform.

## How it works

`packChars4` puts `chars[index]` in the low 16 bits, the next char in bits 16 to 31, and so on up to four chars (`LONG_CHARS`). Two packed words can then be compared with one `==`. For keys shorter than four chars, `ghostCharSwarLengthMasks[n]` clears the unused high lanes: entry `n` is `(1 shl (16 * n)) - 1`, and entry four is `-1` (all bits set), which avoids a shift by 64.

The selector in [[GhostJsonStringReaderSubsystem]] uses at most two words (eight chars): one for `word0` and one for `word1`, with precomputed expected values in the field options. Longer names fall back to an unrolled char loop.

## The code

Packing four chars:

```kotlin
internal inline fun packChars4(
    chars: CharArray,
    index: Int
): Long = chars[index].code.toLong() or
    (chars[index + CHAR_OFFSET_1].code.toLong() shl SCN.SHIFT_16) or
    (chars[index + CHAR_OFFSET_2].code.toLong() shl SCN.SHIFT_32) or
    (chars[index + CHAR_OFFSET_3].code.toLong() shl SCN.SHIFT_48)
```

The mask table:

```kotlin
internal val ghostCharSwarLengthMasks: LongArray = LongArray(SCN.LONG_CHARS + 1) { n ->
    if (n >= SCN.LONG_CHARS) -1L else (1L shl (SCN.SHIFT_16 * n)) - 1L
}
```

The caller must guarantee that `index + 4 <= chars.size`.

## Related

- [[GhostSwar]]: the byte-channel counterpart, which does need platform actuals.
- [[GhostJsonStringReaderSubsystem]]: the field selector that packs chars and applies the masks.
- [[GhostJsonStringReader]]: the reader whose `rawChars` array is packed.
- [[GhostJsonScanConstants]]: defines `LONG_CHARS` and the shift constants.
- [[JsonReaderOptions]]: stores the precomputed expected words for each field name.
