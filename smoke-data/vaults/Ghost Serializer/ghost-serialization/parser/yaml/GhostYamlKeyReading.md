---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlKeyReading.kt
kind: functions
---

# GhostYamlKeyReading

Reads the key of a YAML mapping entry: a plain scalar that ends at `:`, a quoted string, or an alias.

## What it is about

Block and flow mappings both need to read a key before the `:` and the value. Keys have their own rules, different from values, so `readKey(inFlow)` has a file of its own. It returns the key text, or `null` when there are no more keys. The block and flow mapping readers call it in their loops.

## How it works

1. An alias key (`*name`) is resolved at once and stringified.
2. `consumeKeyAnchorAndTagPrefixes` takes any `&anchor` or `!tag` prefix before the key (for example `&a5 !!str key5:`). The tag is dropped, the anchor name is remembered, and the function reports whether anything was consumed. After a prefix, the key must follow; otherwise "Anchor/tag prefix on a key must be followed by the key itself" is raised. An anchor followed directly by an alias is an error.
3. By first byte, a double or single quote goes to the quoted-string readers, wrapped by `readQuotedKeyRejectingMultiLine`. That wrapper rejects a multi-line implicit key in block context, while allowing it in flow context. Otherwise `readBareKeyOrEmptyString` scans the text.
4. A bare key ends at a mapping colon, at a line break, at a `#` preceded by whitespace, or (in flow) at `,`, `]` or `}`. Trailing blanks are trimmed. If nothing precedes the colon the key is the empty string. In flow context a key may continue on the next line through `foldFlowPlainScalarContinuation`.
5. If an anchor prefix was present, the key text is stored in `anchorTable` under the anchor name.

## The code

The dispatch after the prefixes:

```kotlin
val key = when (localRawData[position]) {
    TOK.DOUBLE_QUOTE_BYTE -> readQuotedKeyRejectingMultiLine(inFlow = inFlow) { readDoubleQuotedString() }
    TOK.SINGLE_QUOTE_BYTE -> readQuotedKeyRejectingMultiLine(inFlow = inFlow) { readSingleQuotedString() }
    else -> readBareKeyOrEmptyString(inFlow = inFlow, hadPrefixes = hadPrefixes)
}
if (key != null && anchorName != null) {
    anchorTable[anchorName] = key
}
```

The comment rule for bare keys:

```kotlin
val isWhitespacePrecededComment = currentByte == TOK.HASH_BYTE && position > startPosition &&
    isInlineWhitespaceByte(byte = localRawData[position - 1])
if (isWhitespacePrecededComment) break
```

## Related

- [[GhostYamlBlockStyleSubsystem]]: reads block keys with `inFlow = false`.
- [[GhostYamlFlowStyleSubsystem]]: reads flow keys with `inFlow = true`.
- [[GhostYamlQuotedScalarDecoding]]: reads quoted keys.
- [[GhostYamlAnchorSubsystem]]: `readAlias` for alias keys; anchors are bound here.
- [[GhostYamlExplicitKeySubsystem]]: `stringifyExplicitMappingKey`.
- [[GhostYamlPlainScalarFolding]]: `foldFlowPlainScalarContinuation` for folded flow keys.
- [[GhostYamlValueReading]]: `isMappingColonAt` and `isFlowScalarTerminatorByte`.
