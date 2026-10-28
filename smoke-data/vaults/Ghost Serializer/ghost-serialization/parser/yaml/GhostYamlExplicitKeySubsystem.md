---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlExplicitKeySubsystem.kt
kind: functions
---

# GhostYamlExplicitKeySubsystem

The reader for YAML's explicit mapping entries, written as `? key` followed by `: value`.

## What it is about

Most mapping keys are implicit: `name: value` on one line. YAML also lets a key be any node, written after a `?` indicator, with the value after a `:` on a later line. This is rare but legal, so Ghost handles it in a separate file to keep [[GhostYamlBlockStyleSubsystem]] short. Ghost's maps are `Map<String, Any?>`, so a non-scalar key is reduced to a string.

## How it works

`isExplicitKeyIndicator` checks for a `?` followed by a blank, a line break or the end of input. `readExplicitKeyEntry(blockIndent)` then:

1. consumes `?` and skips blanks;
2. reads the key node: on a later line (only if it is indented deeper than the block, or is a `-` entry at the same indent), as an empty key if `:` follows directly (`? : x`), or inline through `readValue`, which allows nested block content right after the `?`;
3. converts the node with `stringifyExplicitMappingKey`: `null` becomes `""`, a string stays, anything else uses `toString()`;
4. looks for the `:` (always valid on the key's own line; on a later line it must sit at exactly `blockIndent`) and reads the value with `resolveValueAfterColon`. With no `:`, the value is `null`.

The result is a `Pair<String, Any?>` that the mapping loop stores.

## The code

The two indicator checks share the "followed by a terminator" test:

```kotlin
internal fun GhostYamlFlatReader.isExplicitKeyIndicator(): Boolean {
    if (position >= limit || rawData[position] != TOK.QUESTION_BYTE) return false
    return isFollowedByIndicatorTerminator(nextPosition = position + 1)
}
```

The stringification:

```kotlin
internal fun stringifyExplicitMappingKey(keyNode: Any?): String = when (keyNode) {
    null -> ""
    is String -> keyNode
    else -> keyNode.toString()
}
```

The same function is used by [[GhostYamlKeyReading]] when an alias is used as a key.

## Related

- [[GhostYamlBlockStyleSubsystem]]: calls `readExplicitKeyEntry` and shares `resolveValueAfterColon`.
- [[GhostYamlKeyReading]]: uses `stringifyExplicitMappingKey` for alias keys.
- [[GhostYamlValueReading]]: `readValue` also dispatches a leading `?` here.
- [[GhostYamlWhitespace]]: provides the skipping and `advanceLine` helpers.
- [[GhostYamlFlatReader]]: the receiver holding the parse state.
- [[GhostYamlTokens]]: the `?`, `:` and whitespace bytes.
