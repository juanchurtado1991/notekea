---
tags: [ghost-serializer, serialization, writer, numbers, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/common/GhostWriterLongDigits.kt
kind: object
---

# GhostWriterLongDigits

A helper object that writes the decimal digits of a `Long` backwards into a scratch buffer.

## What it is about

Writers need decimal text for integers without allocating a `String`. This object contains the shared digit-emission loops for the byte writer, the char writer and the YAML writers, so each one does not carry its own copy. Callers deal with `Long.MIN_VALUE` and with flushing the scratch to their sink.

## How it works

The functions fill the scratch from the end toward the start and return the start index, so the caller can write `scratch[start until end]`. The two main variants peel off two digits at a time using a lookup table of the digit pairs `00..99` (`DOUBLE_DIGIT_LUT` for bytes, `DOUBLE_DIGIT_LUT_CHARS` for chars): while the value is at least 100, `value % 100` selects a pair and the value is divided by 100. The remaining one or two digits are written directly, then an optional `-` is prepended. This halves the number of divisions compared with a digit-per-step loop.

`writePositiveDigitsBytes` is the simple version for YAML: one digit per step, positive values only, with the sign written separately by the caller.

## The code

```kotlin
while (localValue >= HUNDRED_LONG) {
    val rem = (localValue % HUNDRED_LONG).toInt() * 2
    scratch[--pos] = DOUBLE_DIGIT_LUT[rem + 1] // ones
    scratch[--pos] = DOUBLE_DIGIT_LUT[rem]     // tens
    localValue /= HUNDRED_LONG
}

if (localValue < TEN_LONG) {
    scratch[--pos] = (ZERO_INT + localValue.toInt()).toByte()
} else {
    val rem = localValue.toInt() * 2
    scratch[--pos] = DOUBLE_DIGIT_LUT[rem + 1] // ones
    scratch[--pos] = DOUBLE_DIGIT_LUT[rem]     // tens
}
```

`writeDigitsChars` is the same logic over a `CharArray`, and `scratchEnd` defaults to `LONG_SCRATCH_SIZE`.

## Related

- [[GhostJsonWriterHelpers]]: calls it for byte-channel long values
- [[GhostJsonStringWriter]]: calls it for char-channel long values
- [[GhostYamlWriterHelpers]]: uses the positive-digits variant
- [[GhostDoubleFormatter]]: another zero-allocation number formatter
- [[GhostJsonWriterConstants]]: holds the lookup tables and constants
