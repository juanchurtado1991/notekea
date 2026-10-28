---
tags: [ghost-serialization, serialization, yaml, parser, constants]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/GhostYamlScanConstants.kt
kind: object
---

# GhostYamlScanConstants

The numeric constants used by the YAML reader: digit bounds, limits, bit shifts, UTF-8 encoding and escape codes.

## What it is about

Parsing YAML bytes needs many small numbers: where the digits begin and end, how many bits a hex digit uses, which byte prefixes mark a multi-byte UTF-8 sequence, what code an escape such as `\e` stands for. This internal object (`@PublishedApi`) names them all. It is the reader's counterpart of [[GhostYamlWriterConstants]], and it builds on the byte tokens of [[GhostYamlTokens]].

## How it works

The object holds only `const val`s, so there is no runtime cost: the values are inlined where used. The groups are:

- **Hot-path masks:** the digit range and the mask that lowercases an ASCII letter (`0x20`).
- **Indentation and limits:** `INDENT_UNSET = -1`, `SEQUENCE_ENTRY_INDENT = 2` (the dash and its space) and `MAX_DEPTH = 64`, the maximum nesting.
- **Number bases:** shifts of 4, 3 and 1 bits for hex, octal and binary digit accumulation.
- **UTF-8 and escapes:** range limits, lead-byte prefixes, continuation masks, the surrogate ranges and the code points that double-quoted escapes (`\a`, `\b`, `\t`, `\e`, `\N`, `\_`, `\L`, `\P`) produce.
- **Sizes:** default map capacity, scratch buffer size (256) and the lengths of `\x`, `\u` and `\U` escapes (2, 4 and 8 hex digits).

## The code

The digit bounds reuse the token constants so there is a single definition of the byte values:

```kotlin
const val DIGIT_LOWER_BOUND: Byte = ZERO_BYTE   // 0x30
const val DIGIT_UPPER_BOUND: Byte = NINE_BYTE   // 0x39
```

Depth and indentation limits:

```kotlin
const val INDENT_UNSET: Int = -1
const val SEQUENCE_ENTRY_INDENT: Int = 2
const val MAX_DEPTH: Int = 64
```

The escape code constants include `CODE_ESC = 27`, `CODE_NEXT_LINE = 133`, `CODE_NBSP = 160`, `CODE_LINE_SEP = 8232` and `CODE_PARA_SEP = 8233`, matching the YAML escape table.

## Related

- [[GhostYamlTokens]]: provides `ZERO_BYTE` and `NINE_BYTE` used here.
- [[GhostYamlWriterConstants]]: constants for the writer side.
- [[GhostYamlQuotedScalarDecoding]]: uses the UTF-8 and escape constants.
- [[GhostYamlNumberDecoding]]: uses the radix and shift constants.
- [[GhostYamlFlatReader]]: uses depth and scratch-size limits.
- [[GhostYamlDocumentReading]]: reads documents using these limits.
