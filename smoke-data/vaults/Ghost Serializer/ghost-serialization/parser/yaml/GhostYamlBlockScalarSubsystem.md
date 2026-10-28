---
tags: [ghost-serialization, serialization, yaml, parser]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/yaml/GhostYamlBlockScalarSubsystem.kt
kind: functions
---

# GhostYamlBlockScalarSubsystem

The parser for YAML block scalars: multi-line text introduced by `|` (literal) or `>` (folded).

## What it is about

Block scalars are how YAML carries long text such as descriptions or scripts without quoting. The text sits on the lines below the indicator, indented, and the indicator can carry options: a chomping mark (`-` strip, `+` keep, or none for clip) and an explicit indentation digit. This file reads the header, finds the indentation of the content, and builds the resulting `String`. It is called by `readValue` in [[GhostYamlValueReading]].

## How it works

1. `readBlockScalar` consumes the indicator and reads the header characters: `+`, `-`, one digit from 1 to 9 (0 or two digits are errors), blanks, and an optional comment which must be preceded by whitespace. Anything else raises an error. It then moves to the next line.
2. The content indentation is either the parent's indentation plus the explicit digit, or auto-detected by `detectBlockScalarIndent`. That function skips leading empty lines, remembers the widest of them, and uses the first real line's indentation. A leading empty line more indented than that line is an error. If the content is not indented beyond the parent (except at the document root), it falls back to the parent plus two so the scalar is empty.
3. `readBlockScalarContent` reads line by line into a `StringBuilder`. A line indented below the block indentation, or a `---`/`...` marker, ends the scalar. Blank lines are counted and then flushed according to the style: literal keeps every newline; folded joins two ordinary lines with a space, but keeps newlines around more-indented lines and around blank lines. Extra spaces beyond the block indentation are kept.
4. The chomping rule then trims the result: `STRIP` removes all trailing newlines, `CLIP` leaves exactly one (or none for empty content), `KEEP` keeps them all.

## The code

The header loop recognizes each option:

```kotlin
when {
    currByte == TOK.PLUS_BYTE -> {
        chomp = GhostYamlFlatReader.ChompStyle.KEEP; position++
    }
    currByte == TOK.DASH_BYTE -> {
        chomp = GhostYamlFlatReader.ChompStyle.STRIP; position++
    }
    // …
}
```

The final trimming:

```kotlin
GhostYamlFlatReader.ChompStyle.CLIP -> {
    var end = content.length
    while (end > 0 && content[end - 1] == '\n') end--
    if (end > 0) content.substring(0, end) + "\n" else ""
}
```

## Related

- [[GhostYamlFlatReader]]: owns the `ChompStyle` enum and `isDigit`.
- [[GhostYamlValueReading]]: calls `readBlockScalar` on `|` and `>`.
- [[GhostYamlWhitespace]]: `skipToEndOfLine` and the byte predicates.
- [[GhostYamlTokens]]: the indicator bytes.
- [[GhostYamlScanConstants]]: `INDENT_UNSET` marks the document root.
- [[GhostYamlErrorMessages]]: the block scalar error texts.
- [[GhostYamlPlainScalarFolding]]: the folding rules for plain scalars, which follow the same idea.
