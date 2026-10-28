---
tags: [ghost-serialization, serialization, yaml, writer, constants]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/GhostYamlWriterConstants.kt
kind: object
---

# GhostYamlWriterConstants

A holder of the numeric and string constants that only the YAML writer needs.

## What it is about

The YAML writer deals with indentation, hexadecimal escapes and the fast path that decides whether a short string needs any escaping. Rather than scatter literals through the writer code, Ghost gathers them in this internal object (marked `@PublishedApi`, so inline code from public functions may still use it). It is the writer-side counterpart of [[GhostYamlScanConstants]], which serves the reader.

## How it works

Nothing runs here: the object only contains `const val`s, which the compiler inlines at each use. The values fall in three groups: hex formatting, indentation and context types, and the offsets used to cut a field name out of a pre-encoded header.

## The code

Hex formatting is used when a control or non-ASCII character must be written as an escape. The lookup table is turned into a byte array once:

```kotlin
const val STR_HEX_CHARS = "0123456789abcdef"
const val STR_MIN_LONG_ABS = "9223372036854775808"
val HEX_CHARS_ARR = STR_HEX_CHARS.encodeToByteArray()
```

`STR_MIN_LONG_ABS` is the absolute value of `Long.MIN_VALUE`, which cannot be negated inside a `Long`, so the writer needs its text form.

Indentation and context:

```kotlin
const val SPACES_PER_LEVEL = 2
const val TYPE_OBJECT = 1
const val TYPE_ARRAY = 2
const val PLAIN_ASCII_LIMIT = 64
```

Each nesting level adds two spaces. `TYPE_OBJECT` and `TYPE_ARRAY` tag the entries of the writer's context stack. `PLAIN_ASCII_LIMIT` (64) is the longest string the writer pre-checks for printable ASCII before writing it between quotes without escaping, and `ASCII_LIMIT` (128) separates single-byte characters from those that need UTF-8 encoding. The `SHIFT_*` and `HEX_MASK` values extract hex digits from a char.

The header constants (`HEADER_MIN_SIZE`, `HEADER_QUOTE_*`, `HEADER_COLON_OFFSET_SUB`, `SUBSTRING_START_OFFSET`) describe the layout of a pre-encoded JSON-style header such as `"name":`, so the writer can pull the bare name out of it.

## Related

- [[GhostYamlWriter]]: the main user of these constants.
- [[GhostYamlWriterHelpers]]: the escaping and number helpers that also read them.
- [[GhostYamlScanConstants]]: the reader's equivalent.
- [[GhostYamlTokens]]: the shared byte-level syntax tokens.
