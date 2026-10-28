---
tags: [ghost-serialization, serialization, parser, errors, constants]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/constants/GhostJsonErrorMessages.kt
kind: object
---

# GhostJsonErrorMessages

One `object` that holds the text of every JSON parse and decode error, shared by all reader channels.

## What it is about

Ghost has several readers (flat bytes, streaming, string) and many shared kernels. For a user to see the same message for the same mistake, the strings are defined once. The code imports the object as `EM`. Messages are plain `const val`s, so they cost nothing at runtime until an error is actually thrown.

## How it works

Constants are grouped by what went wrong:

- Strings and escapes: "Unterminated string", "Invalid escape sequence", "Lone high surrogate", "Unescaped control character in string".
- Structure: "Expected '{'", "Expected ':'", "Trailing comma", "Unexpected comma", "Expected ',' or ']'", "Reached maximum recursion depth".
- Fields and polymorphism: "Required field '…' missing in JSON" (split into a prefix and a suffix constant so the field name can be inserted), "Missing discriminator field for sealed class", "Unknown type discriminator: ", and the strict-mode "Unknown field in strict mode: ".
- Numbers: "Integer overflow" and the variants for each integer type, "Leading zeros are not allowed", "Expected digits after decimal point", "Isolated minus sign", "Numeric overflow or NaN is not allowed in JSON", "Expected an integer but found a fractional number".
- Encoding and capacity: "Invalid JSON text encoding (RFC 8259 requires UTF-8, UTF-16, or UTF-32)" and "FlatByteArrayWriter capacity overflow: ".

Several messages are intentionally built from a prefix, optional middle and suffix constants (for example the index-out-of-bounds family) so callers concatenate values without format strings. `ERR_AT_POSITION_PREFIX = " at position "` is glued on by the exception type. `ERR_UNKNOWN_ENUM` is also reused by the Protobuf enum decode.

## The code

```kotlin
const val ERR_REQUIRED_FIELD_PREFIX = "Required field '"
const val ERR_REQUIRED_FIELD_SUFFIX = "' missing in JSON"
// …
/** Suffix glued onto parser error messages: `"$message$ERR_AT_POSITION_PREFIX$position"`. */
const val ERR_AT_POSITION_PREFIX = " at position "
```

## Related

- [[GhostJsonException]]: the exception that carries these messages with a position and path.
- [[GhostJsonStructureHelpers]]: throws the structural errors.
- [[GhostJsonNumberValidationHelpers]]: throws the number-syntax errors.
- [[GhostNumericParseHelpers]]: throws the narrowing overflow errors.
- [[GhostJsonQuotedStringSlow]]: throws the string and escape errors.
- [[GhostJsonHintMessages]]: companion text that adds hints to errors.
- [[GhostYamlErrorMessages]]: the YAML counterpart.
