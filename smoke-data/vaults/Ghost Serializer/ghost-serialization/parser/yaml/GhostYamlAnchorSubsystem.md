---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlAnchorSubsystem.kt
kind: functions
---

# GhostYamlAnchorSubsystem

The part of the YAML reader that handles anchors (`&name`), aliases (`*name`) and merge keys (`<<`).

## What it is about

YAML lets a document name a node with an anchor and reuse it later with an alias, and lets a mapping pull in the entries of another one with the `<<` merge key. This file implements those three features as extension functions on [[GhostYamlFlatReader]]. The anchors live in the reader's `anchorTable`, a `HashMap<String, Any?>` cleared at the start of each document.

## How it works

- **Defining an anchor.** `readAnchoredValue` reads the name after `&` (up to a space, line break or flow delimiter), skips an optional comment, and then reads the value that follows, on the same line or on the next. It stores the result in `anchorTable` under the name. It reports errors for an anchor directly followed by an alias, or by a block sequence dash on the same line.
- **Anchor on a key or value?** In block context `&a a: b` anchors the key `a`, not the whole mapping. `readAnchoredValueOrMappingKey` settles this by a speculative parse: it reads a key, checks for a following `:`, then always rewinds `position`. If it looked like a key line it calls `readBlockMapping`, otherwise `readAnchoredValue`. Errors raised during the guess are swallowed, and flow collections after the anchor are excluded from the guess because their inner colons would fool a key scan.
- **Alias.** `readAlias` looks the name up; a missing name raises "Anchor '...' not found". It tests `containsKey` instead of a null result, since an anchor may legitimately hold `null`. `readAliasOrMappingKey` also lets an alias act as a key.
- **Merge.** `mergeInto(target, value)` copies entries of a map into the target only when the key is absent, so explicit keys always win; for a list of maps it merges each one in order.

Aliases are resolved while parsing: the alias points to the same object that was anchored (no copy), which is cheap.

## The code

The alias lookup:

```kotlin
if (!anchorTable.containsKey(key = aliasName)) {
    yamlError(message = "${EM.ERR_ANCHOR_NOT_FOUND_PREFIX}$aliasName${EM.ERR_ANCHOR_NOT_FOUND_SUFFIX}")
}
return anchorTable[aliasName]
```

The merge rule for maps:

```kotlin
for ((k, v) in value) {
    val keyStr = k as? String ?: continue
    if (!target.containsKey(key = keyStr)) {
        target[keyStr] = v
    }
}
```

## Related

- [[GhostYamlFlatReader]]: owns `anchorTable` and `position`.
- [[GhostYamlValueReading]]: dispatches `&` and `*` to this file.
- [[GhostYamlBlockStyleSubsystem]]: calls `mergeInto` for `<<` and `readBlockMapping` here.
- [[GhostYamlFlowStyleSubsystem]]: also merges in flow mappings.
- [[GhostYamlKeyReading]]: binds anchors on keys and resolves alias keys.
- [[GhostYamlException]]: the exception caught during the speculative key read.
- [[GhostYamlErrorMessages]]: the anchor error texts.
