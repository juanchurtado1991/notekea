---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlScalarDecoding.kt
kind: functions
---

# GhostYamlScalarDecoding

Decides which Kotlin type a plain YAML scalar becomes: null, boolean, integer, float or string.

## What it is about

After the reader has found the bytes of a plain scalar, `interpretScalar` turns them into a value for the tree. It follows the YAML 1.2 core-schema order (null, then bool, then int, then float, then string) and avoids creating a `String` unless the result really is one. It is only called from the plain-scalar reader; quoted strings and number parsing live in [[GhostYamlQuotedScalarDecoding]] and [[GhostYamlNumberDecoding]].

## How it works

1. If the tag is `!!str`, the bytes become a string at once, even when empty.
2. Empty untagged content is `null`.
3. A tag forces the type: `!!null` gives null; `!!bool` accepts only true or false and otherwise raises an error; `!!int` and `!!float` parse a number and raise an error if it does not fit, instead of returning `0` or `false`.
4. With no tag: `~` and a four-letter `null` are null; the words `true` and `false` are booleans; text starting with `-`, a digit or `.` goes to `tryParseNumber`; anything else is a string.

The word checks (`isNullLiteral`, `isTrueLiteral`, `isFalseLiteral`) compare the bytes after setting the ASCII lowercase bit (`or 0x20`), so no string is made and no case-by-case list is needed. As written, this accepts any capitalization of the words, not only the three standard ones.

## The code

The untagged cascade:

```kotlin
if (firstByte == TOK.TILDE_BYTE && length == 1) return null
if (isNullLiteral(data = data, start = start, length = length)) return null

if (isTrueLiteral(data = data, start = start, length = length)) return true
if (isFalseLiteral(data = data, start = start, length = length)) return false

val isNumberLeadByte = firstByte == TOK.DASH_BYTE || isDigit(firstByte) || firstByte == TOK.DOT_BYTE
if (isNumberLeadByte) {
    tryParseNumber(data = data, start = start, end = end)?.let { return it }
}
return data.decodeToString(start, end)
```

A literal check:

```kotlin
val byte0 = (data[start].toInt() or SC.ASCII_TO_LOWER_MASK).toByte()
```

## Related

- [[GhostYamlPlainScalarFolding]]: the caller that supplies the byte range.
- [[GhostYamlNumberDecoding]]: `tryParseNumber` parses numeric text.
- [[GhostYamlTags]]: the `expectedTag` codes.
- [[GhostYamlTagSubsystem]]: produces the tags.
- [[GhostYamlScanConstants]]: `ASCII_TO_LOWER_MASK`.
- [[GhostYamlTokens]]: the letter bytes compared.
- [[GhostYamlFlatReader]]: `isDigit` and the receiver.
