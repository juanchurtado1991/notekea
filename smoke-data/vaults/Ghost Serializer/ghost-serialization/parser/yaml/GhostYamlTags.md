---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlTags.kt
kind: object
---

# GhostYamlTags

Eight integer codes naming the core-schema types that a YAML tag can select.

## What it is about

A YAML tag such as `!!str`, `!!int` or a bare `!` tells the reader how to interpret a scalar or collection, regardless of how it looks. After the tag is read, the reader needs a compact way to remember the result. This internal object provides it: one `Int` per type. The values drive the scalar interpretation step, which switches on them.

## How it works

The tag subsystem parses the tag text and maps it to one of these constants, then stores it in the reader's state. When a scalar is later interpreted, a `TAG_NONE` result means "decide from the text" (plain YAML resolution), while any other value forces that type: for example, `TAG_STR` keeps `123` as a string, and `TAG_NULL` makes a scalar null. Using small integers instead of strings or an enum avoids allocation and makes the dispatch a simple `when`.

## The code

The complete object:

```kotlin
internal object GhostYamlTags {
    const val TAG_NONE = 0
    const val TAG_STR = 1
    const val TAG_INT = 2
    const val TAG_FLOAT = 3
    const val TAG_BOOL = 4
    const val TAG_NULL = 5
    const val TAG_SEQ = 6
    const val TAG_MAP = 7
}
```

`TAG_SEQ` and `TAG_MAP` cover the collection tags `!!seq` and `!!map`; the others describe scalars.

## Related

- [[GhostYamlTagSubsystem]]: parses tags and produces these codes.
- [[GhostYamlScalarDecoding]]: dispatches scalar interpretation on them.
- [[GhostYamlValueReading]]: reads values with a pending tag.
- [[GhostYamlPlainScalarFolding]]: also consults the tag for plain scalars.
- [[GhostYamlTokens]]: provides the bytes used when matching tag names.
