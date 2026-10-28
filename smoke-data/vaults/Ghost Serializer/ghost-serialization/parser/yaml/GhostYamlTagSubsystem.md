---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlTagSubsystem.kt
kind: functions
---

# GhostYamlTagSubsystem

The parser for YAML tags such as `!!str`, `!<Circle>`, `!shape` and a bare `!`.

## What it is about

A tag annotates a node with a type. This file reads the tag and then the node it applies to, and uses the tag either to force an interpretation (for example `!!str 12` stays text) or to remember a custom type name (used for polymorphic data). It is entered from `readValue` when a value starts with `!`, and uses the integer codes of [[GhostYamlTags]].

## How it works

`readTaggedValue` consumes `!` and checks for a second one. Then:

1. **`!!name`** (`resolveDoubleExclamationTag`): the name is matched byte by byte against the core schema (`str`, `int`, `seq`, `map`, `bool`, `null`, `float`) and becomes a `TAG_*` code. If a `%TAG` directive redefined `!!`, the tag is treated as the application's own and gets a full name instead of a core type.
2. **`!<verbose>`**: the text between `<` and `>` is the tag name.
3. **`!name` or `!handle!name`**: the handle is expanded with the `%TAG` directives stored in `tagDirectives`; an unknown handle is an error.
4. **bare `!`**: the non-specific tag, which forces string resolution (`! 12` is the string "12").

After that, the value starts on the same line or the next (the indent is detected). For `!!seq` and `!!map` it reads a flow or block collection; otherwise it calls `readValue` with the tag type as `expectedTag`. A custom tag name is attached to the resulting mutable map under the key `_tag`. Flow indicators end a tag name, but only inside flow collections; in block context a tag touching one is invalid.

## The code

The match for the three-letter names compares bytes directly instead of creating a string:

```kotlin
val isStrTag = localRawData[start] == TOK.LOWERCASE_S_BYTE &&
    localRawData[start + 1] == TOK.LOWERCASE_T_BYTE &&
    localRawData[start + 2] == TOK.LOWERCASE_R_BYTE
if (isStrTag) return GhostYamlTags.TAG_STR
```

Attaching a custom tag:

```kotlin
private fun attachResolvedTag(value: Any?, resolvedTag: String?) {
    if (resolvedTag != null && value is MutableMap<*, *>) {
        @Suppress("UNCHECKED_CAST")
        val map = value as MutableMap<String, Any?>
        map[TOK.STR_TAG_KEY] = resolvedTag
    }
}
```

## Related

- [[GhostYamlTags]]: the integer codes this file produces.
- [[GhostYamlValueReading]]: dispatches `!` here and receives the value back.
- [[GhostYamlScalarDecoding]]: applies `expectedTag` when it interprets scalars.
- [[GhostYamlWhitespace]]: reads the `%TAG` directives stored in `tagDirectives`.
- [[GhostYamlBlockStyleSubsystem]]: reads tagged block collections.
- [[GhostYamlFlowStyleSubsystem]]: reads tagged flow collections.
- [[GhostYamlTokens]]: letter bytes and the `_tag` key.
