---
tags: [ghost-serialization, serialization, parser, coercion]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/GhostBooleanCoercionHelpers.kt
kind: functions
---

# GhostBooleanCoercionHelpers

A reader-independent matcher that turns lenient text such as `yes`, `Off` or `1` into a Boolean without creating a String.

## What it is about

When boolean coercion is enabled, Ghost accepts more than the JSON literals `true` and `false`. A quoted value like `"Yes"`, `"ON"`, `"n"` or `"0"` is also a valid boolean. `matchCoerceBooleanBytes` implements that matching once, so the JSON byte readers, the char-based string reader and the YAML traversal can all share it.

## How it works

The function is `internal inline`. It receives the `start` offset and `length` of the candidate text, a `getByte` adapter that reads one unit at a given index, and an `onError` lambda that must throw. Because it returns `Nothing` on failure, every `when` branch type-checks as a Boolean.

It switches on the length first, a cheap gate:

- length 1: `y`/`n` (case-folded) or `1`/`0`;
- length 2: `on` or `no`;
- length 3: `yes` or `off`;
- length 4: only `true`;
- length 5: only `false`;
- anything else: error.

Case insensitivity uses a bit trick: ORing a letter with `CASE_INSENSITIVE_MASK` (32) turns upper case into lower case, so `(byte or 32) == FOLD_T` matches both `t` and `T`. Constants `FOLD_Y`, `FOLD_N`, `FOLD_O` and so on come from `GhostJsonTokens`. No substring is allocated and no string comparison is called, so the check is allocation-free.

## The code

```kotlin
TOK.BOOL_STR_LEN_3 -> {
    val b0 = getByte(start)
    val b1 = getByte(start + 1)
    val b2 = getByte(start + 2)
    val isYes = (b0 or TOK.CASE_INSENSITIVE_MASK) == TOK.FOLD_Y &&
        (b1 or TOK.CASE_INSENSITIVE_MASK) == TOK.FOLD_E &&
        (b2 or TOK.CASE_INSENSITIVE_MASK) == TOK.FOLD_S
    // … isOff is built the same way
    when {
        isYes -> true
        isOff -> false
        else -> onError()
    }
}
```

The truthy set is `true`, `yes`, `on`, `y`, `1`; the falsy set is `false`, `no`, `off`, `n`, `0`.

## Related

- [[GhostJsonValueHelpers]]: `nextBooleanCore` calls it through an adapter when the token is a quote.
- [[GhostJsonTokens]]: defines the folded letter constants and the length constants.
- [[GhostJsonFlatReader]]: byte-array reader using it.
- [[GhostJsonReaderSubsystem]]: streaming reader's helper code using it.
- [[GhostJsonStringReaderSubsystem]]: string reader's helper code using it.
- [[GhostYamlCursorTraversal]]: YAML scalar reading that reuses it.
