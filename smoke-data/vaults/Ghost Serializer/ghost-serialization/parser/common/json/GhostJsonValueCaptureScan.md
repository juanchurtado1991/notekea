---
tags: [ghost-serialization, serialization, parser, rawjson, kernels]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonValueCaptureScan.kt
kind: functions
---

# GhostJsonValueCaptureScan

One iterative scanner that finds the end of a complete JSON value while validating its grammar, used to capture a value as `RawJson` without decoding it.

## What it is about

Sometimes a field should be kept as raw JSON, for example an `@GhostWrappedKeys` slot or a `RawJson` property. The reader then needs to know where the value ends, and the captured text must be guaranteed to be well-formed JSON. `captureJsonValueScan` does both in one pass over bytes or chars. It serves the flat and streaming readers (UTF-8 bytes) and the string reader (UTF-16 code units) through a `getUnit` lambda.

## How it works

`captureJsonValueScan(startPosition, limit, getUnit, onError)` returns the position just after the value. Every read goes through a helper that yields `MATCH_END` at or past `limit`, so truncated input produces the reader's own error rather than an out-of-bounds read.

Because an inline function cannot recurse, the scan is a loop with an explicit `depth` and a bitset (`LongArray` sized for `MAX_DEPTH` = 255 levels) recording whether each open level is an object or an array. The loop alternates two states:

- Expecting a value: skip whitespace; on `{` or `[` push a level (error "Reached maximum recursion depth" beyond the limit), handle empty containers, and for objects scan the first key; otherwise scan a scalar.
- After a value: at depth 0 return the position; else expect a comma (then the next key for objects) or the matching closer; any other token yields "Expected ',' or '}'" or "Expected ',' or ']'".

Scalars are checked without decoding: strings (escape syntax and four hex digits after `\u`, with surrogate pairing left to later decoding), the literals `true`, `false`, `null`, and numbers (optional minus, no leading zeros, digits after `.` and in exponents). Errors are reported with the position of the offending unit through `onError`.

## The code

```kotlin
val isContainerStart = unit == TOK.OPEN_OBJ_INT || unit == TOK.OPEN_ARR_INT
if (isContainerStart) {
    if (depth >= NUM.MAX_DEPTH) {
        onError(EM.ERR_DEPTH_EXCEEDED, position)
    }
    val levels = objectLevels ?: LongArray(size = CONTAINER_LEVEL_WORDS).also { objectLevels = it }
    // …
}
```

The bitset is allocated lazily, so scalar captures never allocate it.

## Related

- [[GhostJsonFlatReaderCapture]]: flat reader capture code that calls this scan.
- [[GhostJsonReaderCapture]]: the streaming reader's capture code.
- [[GhostJsonStringReaderCapture]]: the string reader's capture code.
- [[RawJson]]: the type that holds the captured slice.
- [[GhostWrappedKeysCapture]]: stores several captured values and reassembles them.
- [[GhostJsonNumericLimits]]: defines `MAX_DEPTH`.
- [[GhostJsonErrorMessages]]: supplies the error messages.
