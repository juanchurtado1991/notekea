---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlValueReading.kt
kind: functions
---

# GhostYamlValueReading

The central dispatcher of the YAML reader: `readValue` looks at the next byte and sends the parser to the right subsystem.

## What it is about

Whatever YAML node comes next (a scalar, a quoted string, a block mapping, a flow list, an alias, a tag), the decision of which reader to call is made here. All other parser files either call `readValue` for nested content or are called by it. It also contains two small shared helpers, `isMappingColonAt` and `isFlowScalarTerminatorByte`.

## How it works

`readValue(indent, inFlow, expectedTag, strictDedent, allowMappingRedirect, foldIndent)` skips blanks and then switches on the first byte:

- `|` or `>`: block scalar. `{` and `[`: flow mapping or sequence. `!`: tagged value. `&`: anchor. `*`: alias.
- `"` and `'`: quoted scalar, followed by a check for a `:`, because a quoted string can also be a mapping key (`"400": ok`). In that case the position is rewound and `readBlockMapping` is used. Flow collections get the same check (`[a, b]: value`).
- `-`: a digit after it means a negative number (unless the tag is `!!str`), a space or line break means a block sequence entry, `---` means a document marker (null); otherwise a plain scalar.
- `?`: explicit key in block context, else a plain scalar. `.`: `...` ends the document, otherwise a plain scalar. `%` is an error, because directives cannot be values.
- anything else: `readPlainScalarOrMapping`.

At the end of input the result is null, or an empty string for a `!!str` tag. The parameters carry context rather than state: `indent` is the enclosing indentation, `foldIndent` is the column a continued plain scalar must exceed (it can differ from `indent`), and `allowMappingRedirect` stops `a: b: c` from parsing as nested mappings.

## The code

The head of the dispatch:

```kotlin
return when (val currentByte = rawData[position]) {
    TOK.PIPE_BYTE, TOK.GT_BYTE -> readBlockScalar(indicator = currentByte, indent = indent)
    TOK.LEFT_BRACE_BYTE -> readFlowCollectionOrMappingKey(indent = indent, inFlow = inFlow) { readFlowMapping() }
    TOK.LEFT_BRACKET_BYTE -> readFlowCollectionOrMappingKey(indent = indent, inFlow = inFlow) { readFlowSequence() }
    TOK.EXCLAMATION_BYTE -> readTaggedValue(indent = indent, inFlow = inFlow)
    // …
```

The colon test shared by value and key logic:

```kotlin
internal inline fun isMappingColonAt(rawData: ByteArray, position: Int, limit: Int): Boolean {
    if (position >= limit || rawData[position] != TOK.COLON_BYTE) return false
    val next = position + 1
    return next >= limit ||
        rawData[next] == TOK.SPACE_BYTE ||
        // …
        rawData[next] == TOK.TAB_BYTE
}
```

## Related

- [[GhostYamlBlockStyleSubsystem]]: block mappings and sequences read through it.
- [[GhostYamlFlowStyleSubsystem]]: flow collections.
- [[GhostYamlBlockScalarSubsystem]]: `|` and `>` scalars.
- [[GhostYamlQuotedScalarDecoding]]: quoted scalars.
- [[GhostYamlPlainScalarFolding]]: plain scalars.
- [[GhostYamlAnchorSubsystem]]: anchors and aliases.
- [[GhostYamlTagSubsystem]]: tagged values.
- [[GhostYamlDocumentReading]]: the entry point that calls `readValue` for each document.
