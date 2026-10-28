---
tags: [ghost-serializer, serialization, writer, kernels, performance]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/writer/bytes/GhostJsonWriterHelpers.kt
kind: object
---

# GhostJsonWriterHelpers

Inline kernels shared by the JSON writers for structure, numbers and string handling.

## What it is about

The byte writer (`GhostJsonWriter`, over either Okio or a flat buffer) and the char writer (`GhostJsonStringWriter`) need identical logic for opening and closing containers, for writing integers and for choosing a number path. This object holds that logic once. Each function is `inline` and receives the real sink operations as lambdas, so the writers keep their own monomorphic, fully inlined writes. It plays the same role as `GhostYamlWriterHelpers` does for YAML.

## How it works

The kernels cover: `beginArrayCore` and `beginObjectCore` (check depth against the maximum, append the comma, write the bracket, increase depth); `endArrayCore` and `endObjectCore`; `writeIntValueRawCore` and `writeLongValueRawCore`, which keep the original branch order (single digit positive, single digit negative, minimum value, general) because those are the hottest cases; `writeLongValueRawInternalCore`, which writes digits with `GhostWriterLongDigits`; `writeDoubleValueRawCore` and `writeFloatValueRawCore`; `writeStringValueRawSlowCore`; and `writeULongValueRawCore`.

For doubles, a whole number inside the safe-integer range (and not negative zero) is written as a long followed by `.0`. Otherwise `GhostDoubleFormatter` is tried, and on `FALLBACK_REQUIRED` the code throws for non-finite values or writes `toString()`. For strings, if no plain prefix exists and the string fits, it is escaped into the scratch buffer in one pass; otherwise the quote, ASCII prefix and escaped remainder are written separately.

## The code

```kotlin
inline fun beginArrayCore(
    depth: Int,
    maxDepth: Int,
    appendSeparator: () -> Unit,
    writeByte: (Int) -> Unit,
    setDepth: (Int) -> Unit,
    throwDepthError: () -> Nothing,
) {
    if (depth >= maxDepth) throwDepthError()
    appendSeparator()
    writeByte(TOK.OPEN_ARR_INT)
    setDepth(depth + 1)
}
```

`writeULongValueRawCore` writes values up to `Long.MAX_VALUE` as a long and otherwise passes the unsigned `toString()` to a `writeAsciiDigits` lambda.

## Related

- [[GhostJsonWriter]]: byte writer that calls these kernels
- [[GhostJsonStringWriter]]: char writer that reuses the structural and integer kernels
- [[GhostDoubleFormatter]]: used for non-integral doubles and floats
- [[GhostWriterLongDigits]]: used for the digit emission
- [[GhostJsonNumericLimits]]: constants such as the safe-integer range
- [[GhostYamlWriterHelpers]]: the YAML counterpart
