---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlFlowStyleSubsystem.kt
kind: functions
---

# GhostYamlFlowStyleSubsystem

The parser for YAML flow collections, the JSON-like forms `{a: 1, b: 2}` and `[x, y, z]`.

## What it is about

Flow style uses brackets and commas instead of indentation. This file reads flow mappings and flow sequences into the same `Map<String, Any?>` and `List<Any?>` nodes that the block parser produces, so later stages cannot tell which style was used. It is the counterpart of [[GhostYamlBlockStyleSubsystem]].

## How it works

`readFlowMapping` consumes `{`, returns at once for `{}`, and otherwise loops: skip blanks and comments, stop at `}`, reject a stray `,`, read the key, read `:` and the value with `readValue(inFlow = true)`, then require either `,` or `}`. A key that is itself a flow collection goes through `readValue`, because a plain key scan could not tell its inner commas from the separator. A key with no colon (`{ a, b }`) gets a `null` value. A key `<<` merges instead of storing.

`readFlowSequence` has the same shape. Each entry is read by `readFlowSequenceEntry`, because YAML allows an entry to be an implicit one-pair mapping: `[a: 1]` means `[{a: 1}]`. The function also handles an empty key (`[: v]`) and an explicit pair written with `?`. Colons that start a plain scalar such as `::vector` are told apart by `isFlowPairColon`, which needs whitespace or the end after the colon.

Both functions call `checkMaxNestingDepth()` and increase `depth` inside `try`/`finally`, so very deep flow input fails with an error rather than recursing without bound. Empty collections return before the check.

## The code

The mapping entry's value logic:

```kotlin
val value = if (position < localLimit && localRawData[position] == TOK.COLON_BYTE) {
    position++ // consume ':'
    skipWhitespaceAndComments()
    readValue(indent = 0, inFlow = true)
} else if (isEmptyValueShorthand) {
    null
} else {
    yamlError(message = "${EM.ERR_EXPECTED_COLON_AFTER_FLOW_KEY_PREFIX}$key'")
}
```

An implicit pair inside a sequence:

```kotlin
val keyOrValue = readValue(indent = 0, inFlow = true)
skipInlineWhitespace()
if (position < localLimit && localRawData[position] == TOK.COLON_BYTE) {
    position++ // consume ':'
    skipWhitespaceAndComments()
    val value = readValue(indent = 0, inFlow = true)
    return linkedMapOf(stringifyExplicitMappingKey(keyNode = keyOrValue) to value)
}
```

## Related

- [[GhostYamlBlockStyleSubsystem]]: the block-style sibling; shares `checkMaxNestingDepth`.
- [[GhostYamlValueReading]]: dispatches `{` and `[` here; called back for entries.
- [[GhostYamlKeyReading]]: reads flow mapping keys with `inFlow = true`.
- [[GhostYamlExplicitKeySubsystem]]: `stringifyExplicitMappingKey` and `isExplicitKeyIndicator`.
- [[GhostYamlAnchorSubsystem]]: `mergeInto` for `<<`.
- [[GhostYamlFlatReader]]: the receiver holding `position` and `depth`.
- [[GhostYamlErrorMessages]]: the flow-style error texts.
