---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlFoldHelpers.kt
kind: functions
---

# GhostYamlFoldHelpers

Two small pure helpers for folding a multi-line plain scalar into one string.

## What it is about

In YAML, a plain (unquoted) scalar may continue over several lines. The rule is called line folding: a line break between two text lines becomes a single space, and each blank line between them becomes a newline. This file holds the stateless pieces of that rule so [[GhostYamlPlainScalarFolding]] can stay focused on the reader state.

## How it works

`scanFoldBlankLine` starts at a line break, steps over an optional CR and an optional LF, then skips spaces and tabs. It reports three facts in a small result class, `FoldBlankLineScan`: where the line starts, where its content starts, and whether the line is blank (empty or whitespace-only until the next break or the end of input). It does not change any reader field: callers assign `position` from the result. This keeps the function easy to test.

`appendFoldedLine` adds one line to a `StringBuilder`: if `blankLines` is above zero it appends that many `\n`, otherwise a single space, and then the line text.

## The code

The result type and the scan:

```kotlin
internal class FoldBlankLineScan(
    val afterNewline: Int,
    val contentStart: Int,
    val isBlank: Boolean,
)
```

```kotlin
var contentStart = afterNewline
while (contentStart < limit && isInlineWhitespaceByte(byte = rawData[contentStart])) {
    contentStart++
}
val isBlank = contentStart >= limit || isLineBreakByte(byte = rawData[contentStart])
```

And the folding rule itself:

```kotlin
internal fun appendFoldedLine(builder: StringBuilder, blankLines: Int, lineText: String) {
    if (blankLines > 0) repeat(blankLines) { builder.append('\n') } else builder.append(' ')
    builder.append(lineText)
}
```

## Related

- [[GhostYamlPlainScalarFolding]]: the only caller of both helpers.
- [[GhostYamlWhitespace]]: defines the byte predicates such as `isInlineWhitespaceByte`.
- [[GhostYamlTokens]]: provides `CR_BYTE` and `NEWLINE_BYTE`.
- [[GhostYamlFlatReader]]: owns the `position` that callers update from the scan.
