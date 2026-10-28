---
tags: [ghost-serializer, serialization, proto, wkt, field-mask, codec]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/proto/wkt/ProtoFieldMaskCodec.kt
kind: functions
---

# ProtoFieldMaskCodec

Two internal functions that convert FieldMask paths between snake_case (the model) and camelCase (the JSON text).

## What it is about

Proto3 JSON writes a FieldMask as one string where each path is camelCase and paths are joined by commas, whereas `.proto` files and [[ProtoFieldMask]] use snake_case. `formatFieldMask` and `parseFieldMask` perform that translation, and [[ProtoFieldMaskSerializer]] calls them.

## How it works

`formatFieldMask` walks every character of every path with index loops, without regex or `split`. An underscore sets a flag so that the next character is appended in upper case; the underscore itself is dropped. Paths are separated by commas, and an empty mask gives an empty string.

`parseFieldMask` does the inverse in a single pass over the text. Commas end a path (empty paths are skipped). An upper-case character is lowered and, unless it starts a path or follows a dot (which separates nested field names), preceded by an underscore. So `"user.displayName,photo"` becomes the paths `user.display_name` and `photo`. Nothing in these functions throws, since any string is accepted.

Both functions are `internal` and use the `StringBuilder` they create themselves, so they are safe to call from any thread.

## The code

```kotlin
            if (character == TOK.CHAR_UNDERSCORE) {
                uppercaseNext = true
            } else {
                if (uppercaseNext) {
                    stringBuilder.append(character.uppercaseChar())
                    uppercaseNext = false
                } else {
                    stringBuilder.append(character)
                }
            }
```

The parse direction:

```kotlin
            if (character.isUpperCase()) {
                val startsSegment = stringBuilder.isEmpty() || stringBuilder.last() == TOK.CHAR_DOT
                if (!startsSegment) {
                    stringBuilder.append(TOK.CHAR_UNDERSCORE)
                }
                stringBuilder.append(character.lowercaseChar())
            } else {
```

## Related

- [[ProtoFieldMask]]: the model whose paths are converted
- [[ProtoFieldMaskSerializer]]: calls both functions
- [[GhostJsonTokens]]: supplies the comma, dot and underscore characters
