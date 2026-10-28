---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlPlainScalarFolding.kt
kind: functions
---

# GhostYamlPlainScalarFolding

The reader for plain (unquoted) scalars, including the ones that continue over several lines, and the check that tells a scalar from a mapping key.

## What it is about

Most YAML values are plain scalars: `name: Ada`, `count: 3`, `- true`. This file reads them, decides whether the text is really the start of a block mapping key (`name:` followed by a space), and folds continuation lines into one string. When the text is complete it passes the bytes to `interpretScalar` in [[GhostYamlScalarDecoding]] to get a number, boolean, null or string.

## How it works

`readPlainScalarOrMapping` scans forward from the current byte until it finds the end of the scalar:

- A `:` followed by a space, tab, line break or the end ends the scalar and means a key. In block context the function rewinds and calls `readBlockMapping`; in flow context it just stops and lets the caller decide. If the key is on the same line as an enclosing key (`a: b: c`), it reports an error.
- A line break ends the line. `#` ends it only when preceded by whitespace, so `a#b` is one scalar.
- In flow context `,`, `]` and `}` also end it. A lone `-` before one of them is an error.

If the scalar ended at a line break, it tries to fold continuation lines. In block context (`foldPlainScalarContinuation`) a line continues the scalar only if it is indented deeper than the enclosing indent, and it may not look like a `key: ` pair or contain a comment before the end. In flow context (`foldFlowPlainScalarContinuation`) there is no indentation rule: a line continues unless it starts with a terminator, a colon separator, a comment or a document marker. The folding rule is the usual one: one newline becomes a space and each blank line becomes a newline, using [[GhostYamlFoldHelpers]]. If no continuation is found, the position is rewound so the caller sees the same input as before.

The common case, a single-line scalar, works without building any string: `interpretScalar` is given the original byte range.

## The code

The key detection inside the scan loop:

```kotlin
if (isMappingColonAt(rawData = localRawData, position = scanPosition, limit = localLimit)) {
    if (!inFlow) {
        if (!allowMappingRedirect) {
            yamlError(message = EM.ERR_UNEXPECTED_INLINE_NESTED_MAPPING_COLON)
        }
        position = startPosition
        return readBlockMapping(blockIndent = indent.coerceAtLeast(0))
    }
    break
}
```

The final decision between folded and single-line text:

```kotlin
if (folded != null) {
    val foldedBytes = folded.encodeToByteArray()
    return interpretScalar(data = foldedBytes, start = 0, end = foldedBytes.size, expectedTag = expectedTag)
}
```

`readPlainScalar` is a thin alias of `readPlainScalarOrMapping`.

## Related

- [[GhostYamlFoldHelpers]]: `scanFoldBlankLine` and `appendFoldedLine`.
- [[GhostYamlScalarDecoding]]: `interpretScalar` converts the text.
- [[GhostYamlValueReading]]: the main caller; also defines `isMappingColonAt`.
- [[GhostYamlBlockStyleSubsystem]]: `readBlockMapping` is the redirect target.
- [[GhostYamlKeyReading]]: reuses `foldFlowPlainScalarContinuation` for flow keys.
- [[GhostYamlWhitespace]]: byte predicates and `trimTrailingSpaces`.
- [[GhostYamlTags]]: the `expectedTag` passed to the interpretation step.
