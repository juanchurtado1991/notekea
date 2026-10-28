---
tags: [ghost-serialization, serialization, yaml, parser, constants]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/yaml/GhostYamlTokens.kt
kind: object
---

# GhostYamlTokens

The dictionary of fixed YAML syntax: the byte value of each structural character, the letters used in tags and scalars, and the literal words such as `true`, `null` and `---`.

## What it is about

The YAML reader and writer work directly on bytes. To keep that code readable, every special character gets a name here instead of a magic number. The object is internal (`@PublishedApi`) and is used by nearly every file in the YAML parser and writer. Numeric limits live elsewhere, in [[GhostYamlScanConstants]] and [[GhostYamlWriterConstants]].

## How it works

The file has no logic. It is a list of `const val`s grouped by purpose. Byte constants are typed `Byte` so comparisons with `rawData[i]` need no conversion; the `*_INT` variants exist for code that already works with `Int` values (for example when reading chars). Because they are `const`, the compiler inlines them, so naming has no runtime cost.

## The code

The groups, in file order:

- **Basic structure:** colon, space, newline, carriage return, tab, `#`, `-`, `?`, `.`.
- **String delimiters and block scalars:** double and single quote, backslash, `|`, `>`, `+`.
- **Flow style:** braces, brackets and comma.
- **Anchors, aliases, tags, directives:** `&`, `*`, `!`, `%`, `<`.
- **Numeric and letter bytes:** the `0`/`9` bounds, the base letters `x`, `o`, `b` in both cases, and a series of letters that the tag and scalar matchers compare against.
- **Boolean and null starters:** `t`, `f`, `F`, `n`, `N` and `~`.
- **Literal strings:** `STR_TRUE`, `STR_FALSE`, `.inf` forms, `.nan`, the merge key `<<`, the directive names `TAG` and `YAML`.
- **Document markers:** `---`, `...` and their length 3.

Two typical entries:

```kotlin
/** ':' — key-value separator */
const val COLON_BYTE: Byte = 0x3A

/** '|' — literal block scalar indicator */
const val PIPE_BYTE: Byte = 0x7C
```

## Related

- [[GhostYamlScanConstants]]: numeric constants that build on these bytes.
- [[GhostYamlWriterConstants]]: writer-only constants.
- [[GhostYamlFlatReader]]: the reader that scans for these bytes.
- [[GhostYamlWhitespace]]: skips spaces, tabs, newlines and comments.
- [[GhostYamlScalarDecoding]]: matches boolean and null literals.
- [[GhostYamlWriter]]: emits these bytes.
