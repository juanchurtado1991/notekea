---
tags: [ghost-serialization, serialization, parser, kernels]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonValueHelpers.kt
kind: functions
---

# GhostJsonValueHelpers

Two shared inline kernels for reading a boolean and for the "null or value" pattern, written once for the streaming and string readers.

## What it is about

Ghost has several reader implementations whose state lives in different fields. Rather than copy the logic for `nextBoolean` and every `nextXOrNull`, the logic is extracted here as inline functions that take the reader-specific operations as lambdas. After inlining, each reader gets code specialized to itself, which keeps call sites monomorphic and allocation-free.

## How it works

`nextBooleanCore(...)`: peeks the next token and then

1. if it is `t`, skips and validates the literal `true` and returns `true`;
2. if it is `f`, does the same for `false`;
3. if coercion is enabled, accepts a bare `1` or `0` (consuming one byte), or a quote, in which case it delegates to `matchCoerceBooleanBytes` so quoted `"yes"` or `"off"` work;
4. otherwise calls `throwError("Expected boolean but found ")`; the lambda returns `Nothing`.

`nextOrNullCore(peekNextToken, consumeNull, readValue)` is a generic `inline fun <T>`: if the next token is `n` it calls `consumeNull()` and returns `null`, otherwise it returns `readValue()`. Every nullable read method builds on it.

Parameters such as `skipAndValidateLiteral` take the pre-encoded `TRUE_BS` or `FALSE_BS` byte strings from the writer constants, so the literal check compares bytes, not strings.

## The code

```kotlin
internal inline fun <T> nextOrNullCore(
    peekNextToken: () -> Int,
    consumeNull: () -> Unit,
    readValue: () -> T,
): T? {
    if (peekNextToken() == TOK.NULL_CHAR_INT) {
        consumeNull()
        return null
    }
    return readValue()
}
```

## Related

- [[GhostBooleanCoercionHelpers]]: provides `matchCoerceBooleanBytes`, which `nextBooleanCore` receives through an adapter.
- [[GhostJsonReaderSubsystem]]: streaming reader code that calls these kernels.
- [[GhostJsonStringReaderSubsystem]]: string reader code that calls them.
- [[GhostJsonErrorMessages]]: provides the "Expected boolean" message.
- [[GhostJsonWriterConstants]]: provides `TRUE_BS` and `FALSE_BS`.
- [[GhostJsonTokens]]: defines the token codes.
