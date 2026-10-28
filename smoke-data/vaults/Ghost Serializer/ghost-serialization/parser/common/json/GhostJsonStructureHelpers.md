---
tags: [ghost-serialization, serialization, parser, kernels]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonStructureHelpers.kt
kind: functions
---

# GhostJsonStructureHelpers

Shared inline kernels for the structural tokens of JSON: opening and closing objects and arrays, the `:` separator, and skipping a whole value.

## What it is about

Every reader has to match `{`, `}`, `[`, `]` and `:`, keep track of nesting depth, and be able to skip a value it does not want. The logic is identical across the streaming and string readers, differing only in where their state lives. This file contains it once as `internal inline` functions. Each reader passes its own accessors as lambdas, and after inlining the call site is specific to that reader.

## How it works

- `beginObjectCore` and `beginArrayCore` check that the next non-whitespace token is `{` or `[` (else throw "Expected '{'" or "Expected '['"), then call a private `enterCollectionDepthCore`. `beginObjectCore` also resets the predicted field index to the start for in-order field prediction.
- `enterCollectionDepthCore` increments the depth, throws "Reached maximum recursion depth" past `maxDepth`, and, when the depth is below 64, clears that depth's bit in the two comma masks used by strict-mode comma validation.
- `endObjectCore` and `endArrayCore` check the closing token and decrement the depth if it is above zero.
- `consumeKeySeparatorCore` requires a `:`.
- `skipValueCore` is the skip kernel. It peeks the next token and handles objects (loop over keys: expect a quote, skip the key, consume the colon, recurse for the value), arrays (loop over elements), strings, the three literals (validated against the pre-encoded `true`, `false`, `null` byte strings) and finally numbers. Recursion goes through the supplied `skipValue` lambda, since an inline function cannot call itself.

All errors go through a `throwError: (String) -> Nothing` lambda so each reader adds its own position and path.

## The code

```kotlin
internal inline fun endObjectCore(
    nextNonWhitespace: () -> Int,
    getDepth: () -> Int,
    setDepth: (Int) -> Unit,
    throwError: (String) -> Nothing,
) {
    if (nextNonWhitespace() != TOK.CLOSE_OBJ_INT) {
        throwError(EM.ERR_EXPECTED_END_OBJ)
    }
    val depth = getDepth()
    if (depth > 0) {
        setDepth(depth - 1)
    }
}
```

## Related

- [[GhostJsonReaderSubsystem]]: streaming reader code calling these kernels.
- [[GhostJsonStringReaderSubsystem]]: string reader code calling them.
- [[GhostJsonFlatReader]]: the byte-array reader that also uses some of these kernels.
- [[GhostJsonCommaValidationHelpers]]: uses the same comma masks that `enterCollectionDepthCore` clears.
- [[GhostJsonErrorMessages]]: supplies the messages.
- [[GhostJsonNumericLimits]]: defines `MAX_DEPTH`, the usual `maxDepth`.
- [[GhostJsonScanConstants]]: defines the bitmask depth and field-prediction start.
