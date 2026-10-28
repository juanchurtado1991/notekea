---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlBlockStyleSubsystem.kt
kind: functions
---

# GhostYamlBlockStyleSubsystem

The parser for YAML block collections: indentation-based mappings (`key: value`) and sequences (`- item`).

## What it is about

Block style is the usual way people write YAML, and this file reads it. It turns the bytes of a mapping or a sequence into a `LinkedHashMap<String, Any?>` or a `List<Any?>`, which are the nodes of the tree that [[GhostYamlCursorTraversal]] later walks. It is an extension-function group on [[GhostYamlFlatReader]], and calls [[GhostYamlValueReading]] for every value.

## How it works

`readBlockMapping(blockIndent)` loops over lines. For each line it skips blanks and comments, then stops if the line is dedented below `blockIndent`, or is a `---`/`...` marker. A tab in the indentation is an error. It then handles an explicit `?` key through [[GhostYamlExplicitKeySubsystem]], or reads a key with `readKey`, requires a `:`, and calls `resolveValueAfterColon`. A key equal to `<<` is a merge key, so its value is merged into the result instead of stored under that name.

`resolveValueAfterColon` decides where the value is: nothing (null) at the end of input or when the next line is not indented deeper, a nested node starting on the next line, or an inline value. The `allowMappingRedirect = false` flag for implicit entries rejects inputs such as `a: b: c`, and a block sequence on the key line is rejected with its own error. The same function serves explicit entries, where "compact notation" is allowed.

`readBlockSequence(seqIndent)` repeats: check the `-`, step over it and its space, skip an optional comment, then read the item inline (indent `lineIndent + 2`) or on following lines.

Both loops call `checkMaxNestingDepth()` and keep `depth` balanced with `try`/`finally`, so a hostile deeply nested file ends with an error at 64 levels instead of a stack overflow.

## The code

The mapping loop's exit conditions:

```kotlin
val lineIndent = currentIndent
if (result.isNotEmpty() && lineIndent < blockIndent) break  // dedented — end of this mapping
if (isDocumentMarker() || isDocumentEndMarker()) break
if (indentHasTab) yamlError(message = EM.ERR_TAB_IN_BLOCK_MAPPING_INDENT)
```

Merge keys are handled in one place:

```kotlin
if (key == TOK.STR_MERGE_KEY) {
    mergeInto(target = result, value = value)
} else {
    result[key] = value
}
```

The depth check shared with the flow parser:

```kotlin
internal fun GhostYamlFlatReader.checkMaxNestingDepth() {
    if (depth >= SC.MAX_DEPTH) {
        yamlError(message = "${EM.ERR_MAX_NESTING_DEPTH_PREFIX}${SC.MAX_DEPTH}${EM.ERR_MAX_NESTING_DEPTH_SUFFIX}")
    }
}
```

## Related

- [[GhostYamlFlatReader]]: the receiver holding `position`, `depth` and indentation state.
- [[GhostYamlValueReading]]: reads each value and dispatches back here for nested nodes.
- [[GhostYamlKeyReading]]: reads mapping keys.
- [[GhostYamlExplicitKeySubsystem]]: handles `? key` entries.
- [[GhostYamlWhitespace]]: skips blanks and tracks indentation.
- [[GhostYamlAnchorSubsystem]]: provides `mergeInto` for `<<` merge keys.
- [[GhostYamlFlowStyleSubsystem]]: the `{}` and `[]` counterpart.
- [[GhostYamlScanConstants]]: `MAX_DEPTH`, `SEQUENCE_ENTRY_INDENT` and `DEFAULT_MAP_CAPACITY`.
