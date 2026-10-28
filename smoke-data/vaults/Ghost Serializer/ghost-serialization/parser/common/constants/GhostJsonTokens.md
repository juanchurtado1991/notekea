---
tags: [ghost-serialization, serialization, parser, constants, tokens]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/constants/GhostJsonTokens.kt
kind: object
---

# GhostJsonTokens

The vocabulary of the parser: named byte and char codes for JSON syntax, case-folded letters, control characters, hex tables, surrogate ranges, BOM markers and generated type names.

## What it is about

The readers compare raw bytes, not characters, so `'{'` appears as `OPEN_OBJ_INT` and a quote as `QUOTE_INT`. This public object (aliased `TOK`) gives every such value a name, in `Int`, `Byte` and `Char` forms as needed, so hot loops read like prose and share one source of truth with generated code.

## How it works

Main groups:

- Syntax: `COMMA_INT`, `COLON_INT`, `QUOTE_INT`, `OPEN_OBJ_INT`, `CLOSE_ARR_INT`, `MINUS_INT`, `DOT_INT`, digits, `BACKSLASH_INT`, plus literal first letters for `true`, `false`, `null`.
- Case folding: `CASE_INSENSITIVE_MASK = 32` and constants such as `FOLD_T = 't'.code or 32`. A byte is compared with `(raw or 32) == FOLD_T` to accept both cases without allocating.
- Control and ASCII: line feed, carriage return, tab, backspace, form feed, `SPACE_INT = 32`, `ASCII_LIMIT = 128`, and letter codes used when matching escapes and literals.
- Hex: `HEX_LUT`, an `IntArray` of 256 mapping ASCII to a hex value or -1, and the lowercase hex digit tables.
- Unicode: surrogate range bounds, `UNICODE_BASE = 0x10000`, `UNICODE_MAX_CODE_POINT`.
- Encoding detection: BOM bytes for UTF-8, UTF-16 and UTF-32, `BOM_PROBE_SIZE = 4`, `NUL_BYTE`, `NO_BOM`.
- Misc: `EMPTY_BYTES`, `DEFAULT_DISCRIMINATOR_KEY = "type"`, the warm-up payload `{"warm":true}`, and `TYPE_NAME_*` strings (such as "Int" or "List<") that the code generator embeds as type names.

## The code

```kotlin
const val CASE_INSENSITIVE_MASK = 32
const val FOLD_T = 't'.code or 32 // "true"
// …
val HEX_LUT = IntArray(size = 256) { -1 }.apply {
    for (i in 0..9) this['0'.code + i] = i
    for (i in 0..5) {
        this['A'.code + i] = 10 + i
        this['a'.code + i] = 10 + i
    }
}
```

## Related

- [[GhostBooleanCoercionHelpers]]: relies on the folded letters and length constants.
- [[GhostJsonStructureHelpers]]: compares tokens against the structure codes.
- [[GhostJsonUtf8Input]]: uses the BOM and NUL constants.
- [[GhostJsonQuotedStringSlow]]: uses escape letters, surrogates and hex length.
- [[GhostDiscriminatorPeeker]]: imports many of the token codes.
- [[GhostJsonScanConstants]]: the scanning-side companion constants.
- [[GhostJsonErrorMessages]]: the text companion for errors.
