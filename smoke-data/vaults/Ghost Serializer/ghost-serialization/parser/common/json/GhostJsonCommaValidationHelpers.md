---
tags: [ghost-serialization, serialization, parser, strict-mode, kernels]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonCommaValidationHelpers.kt
kind: functions
---

# GhostJsonCommaValidationHelpers

Four inline kernels that enforce comma rules (no missing, extra or trailing commas) for arrays and objects.

## What it is about

JSON requires exactly one comma between members and none after the last. Ghost can be lenient or strict; in strict mode it must reject `[1 2]`, `[1,,2]` and `{"a":1,}`. The checks are needed in several places (iterating arrays, selecting an object key, reading the next key, consuming an array separator) and in several readers. These functions hold that logic for the streaming and string readers.

## How it works

Strict mode uses a bit per nesting depth, in two `Long` masks held by the reader: `needsCommaMask` (a comma will be required before the next member) and `commaConsumedMask` (a comma was already consumed for this position). That caps strict tracking at 64 levels (`MAX_BITMASK_DEPTH`); deeper levels fall back to the lenient branch.

The kernels:

- `hasNextCore`: peeks a token; `]`, `}` or end of input means no more items. In strict mode a comma when none is needed is "Unexpected comma", a missing comma is "Expected comma", and a comma followed by a closer is "Trailing comma".
- `nextKeyCommaPreambleCore`: the same checks before reading an object key, where only `}` ends the loop.
- `selectValidateCommasCore`: the preamble shared by field selection; returns the token after any consumed comma.
- `consumeArraySeparatorCore`: handles the comma between array elements.

The KDoc explains why four similar functions are not merged: their terminal tokens and return contracts differ, and the project requires benchmark evidence before merging hot-path kernels.

In lenient mode a single comma is skipped, but a comma directly before a closer is still an error.

## The code

```kotlin
val bit = SCN.BITMASK_UNIT shl depth
if ((getCommaConsumedMask() and bit) != SCN.RESULT_NONE) {
    setCommaConsumedMask(getCommaConsumedMask() and bit.inv())
    setNeedsCommaMask(getNeedsCommaMask() or bit)
} else {
    val required = (getNeedsCommaMask() and bit) != SCN.RESULT_NONE
    // … comma present? error if not required; trailing comma check
}
```

This pattern recurs in each kernel with small differences.

## Related

- [[GhostJsonReaderSubsystem]]: streaming reader helpers that call these kernels.
- [[GhostJsonStringReaderSubsystem]]: string reader helpers that call them.
- [[GhostJsonFlatReaderSubsystem]]: flat reader helpers that call them.
- [[GhostJsonStructureHelpers]]: clears the same masks when entering a container.
- [[GhostJsonErrorMessages]]: supplies the comma messages.
- [[GhostJsonScanConstants]]: defines `MAX_BITMASK_DEPTH` and `BITMASK_UNIT`.
