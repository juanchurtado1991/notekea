---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlWhitespace.kt
kind: functions
---

# GhostYamlWhitespace

The low-level scanning helpers of the YAML reader: whitespace, comments, indentation, directives and document markers.

## What it is about

Every line transition of the YAML parser passes through this file, so it is the busiest part of the byte-level reader. It adds extension functions to [[GhostYamlFlatReader]] that move `position` over blanks and comments, keep `currentIndent` and `indentHasTab` up to date (YAML structure depends on indentation), and recognize the markers `---` and `...`, directives such as `%YAML` and `%TAG`, and block sequence entries.

## How it works

- `skipInlineWhitespace` skips spaces and tabs only. `skipWhitespaceAndComments` also skips line breaks (LF, CR or CRLF) and full-line comments, and ends by calling `recomputeCurrentIndent`, which walks back to the start of the line, counts the leading spaces and notes if a tab follows them. A `#` that is not preceded by whitespace is not a comment: it raises "Comment must be preceded by whitespace".
- `skipDirectivesAndDocumentStart` reads `%TAG handle prefix` into `tagDirectives`, validates `%YAML major.minor` (a duplicate or malformed one is an error) and consumes an optional `---`. It returns true when it saw an explicit marker. Directives without a following `---` are an error.
- `skipDocumentEnd` consumes `...` and allows only whitespace or a comment after it on that line.
- `isDocumentMarker`, `isDocumentEndMarker` and `isBlockSequenceEntry` test for three equal bytes (or a dash) followed by a whitespace, line break or the end of input.
- The functions `isLineBreakByte` and `isInlineWhitespaceByte` are small `inline` predicates used by every reading file, and `trimTrailingSpaces` shortens a range that ends in blanks.

Everything compares raw bytes against constants from [[GhostYamlTokens]]; nothing here allocates except the short strings for directive names.

## The code

The core of the blank skipper:

```kotlin
internal fun GhostYamlFlatReader.skipInlineWhitespace() {
    val localLimit = limit
    val localRawData = rawData
    while (position < localLimit) {
        val currentByte = localRawData[position]
        if (currentByte != TOK.SPACE_BYTE && currentByte != TOK.TAB_BYTE) break
        position++
    }
}
```

The marker test avoids a false match on a line such as `----`:

```kotlin
return localRawData[position] == markerByte &&
    localRawData[position + 1] == markerByte &&
    localRawData[position + 2] == markerByte &&
    (position + TOK.DOC_MARKER_LEN >= localLimit ||
        isMarkerTerminator(byte = localRawData[position + TOK.DOC_MARKER_LEN]))
```

Local copies of `limit` and `rawData` in each function help the compiler keep them in registers.

## Related

- [[GhostYamlFlatReader]]: the receiver, owning `position`, `currentIndent` and `indentHasTab`.
- [[GhostYamlTokens]]: the byte constants compared here.
- [[GhostYamlErrorReporting]]: `yamlError` used for comment and directive errors.
- [[GhostYamlDocumentReading]]: uses the marker and directive functions.
- [[GhostYamlBlockStyleSubsystem]]: relies on indentation tracking and `isBlockSequenceEntry`.
- [[GhostYamlFoldHelpers]]: reuses the byte predicates.
- [[GhostYamlTagSubsystem]]: resolves handles against the `%TAG` directives stored here.
