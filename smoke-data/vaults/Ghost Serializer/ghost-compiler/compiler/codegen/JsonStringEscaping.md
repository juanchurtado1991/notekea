---
tags: [ghost-serializer, compiler, codegen, json, escaping]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/JsonStringEscaping.kt
kind: object
---

# JsonStringEscaping

An object that escapes a JSON field name at compile time, using the same rules the runtime writer applies at run time.

## What it is about

Generated serializers precompute the text `"fieldName":` as a constant. If a field name contains a quote, a backslash or a control character, pasting it raw would break the JSON string (a name such as `a"b` would end the string early). `JsonStringEscaping.escape` makes the precomputed text valid, and because it follows the runtime writer's behavior, a precomputed header equals what `writer.name(...)` would have produced.

## How it works

The function walks the characters of the name. A lookup map covers the characters with a short escape: `"`, `\`, backspace, form feed, newline, carriage return and tab. Any other character below `0x20` becomes `\u00xx` with lowercase hex digits. Everything else is appended unchanged, including non-ASCII text and characters like `/`. The result is built in a `StringBuilder` sized to the input, avoiding repeated reallocation.

## The code

The mapping and the main loop:

```kotlin
private val SHORT_ESCAPES = mapOf(
    '"' to "\\\"",
    '\\' to "\\\\",
    '\b' to "\\b",
    '\u000C' to "\\f",
    '\n' to "\\n",
    '\r' to "\\r",
    '\t' to "\\t"
)
```

```kotlin
for (char in text) {
    val shortEscape = SHORT_ESCAPES[char]
    when {
        shortEscape != null -> append(shortEscape)
        char.code < CONTROL_CHAR_LIMIT -> appendUnicodeEscape(char = char)
        else -> append(char)
    }
}
```

`appendUnicodeEscape` emits `\u` then four hex digits, extracting each nibble from the highest to the lowest with a shift and a mask of `0xF`, and looking it up in the string `0123456789abcdef`.

## Related

- [[SerializerSetupEmitter]]: escapes names for the `H_*` headers and the warm-up document.
- [[WrappedKeysEmitter]]: escapes names for wrapped-key output.
- [[HeaderConstantNames]]: the sibling that makes the identifier for the same name.
- [[GhostJsonWriter]]: the runtime writer whose escaping this mirrors.
