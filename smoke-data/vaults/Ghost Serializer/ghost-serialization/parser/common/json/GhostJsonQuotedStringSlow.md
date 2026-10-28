---
tags: [ghost-serialization, serialization, parser, strings, escapes]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/parser/common/json/GhostJsonQuotedStringSlow.kt
kind: functions
---

# GhostJsonQuotedStringSlow

The slow path for reading a quoted JSON string that contains escape sequences, shared by the flat and streaming byte readers.

## What it is about

Most strings contain no backslashes, so a reader can find the closing quote and decode the bytes in one step. When a backslash appears, the string must be decoded byte by byte: `\n` becomes a line feed, `é` becomes two UTF-8 bytes, and a surrogate pair `😀` becomes a four-byte code point. This file implements that once as an `inline` kernel with reader state passed as lambdas. The char-based string reader has its own version.

## How it works

`readQuotedStringSlowCore(start, limit, getByte, setPosition, setNextTokenByte, parseUnicodeHex, grow, throwError)`:

1. Acquires a 1 KiB scratch buffer from the pool and releases it in a `finally` block.
2. Loops over bytes: a quote ends the string (sets the position, resets the token byte and returns `decodeToString` of the buffer); a backslash starts an escape; a raw byte below 32 is "Unescaped control character"; anything else is appended, growing the buffer through the supplied `grow` function when needed.
3. For escapes it handles `\n \r \t \b \f` through `simpleEscapeByte`, `\" \\ \/` as themselves, `\u` through `resolveUnicodeEscape`, and otherwise throws "Invalid escape sequence".

`resolveUnicodeEscape` reads four hex digits through `parseUnicodeHex`, rejects a lone low surrogate, and if the code is a high surrogate requires a following `\uXXXX` low surrogate, combining them into one code point. It returns position and code point packed into one `Long` (position in the high 32 bits) to avoid allocating a `Pair`. `appendUtf8CodePoint` writes 1 to 4 UTF-8 bytes. If the input ends before a closing quote, "Unterminated string" is thrown.

## The code

```kotlin
return (pos.toLong() shl PACKED_POSITION_SHIFT) or (code.toLong() and PACKED_CODE_POINT_MASK)
```

## Related

- [[GhostBufferGrowth]]: provides the `growBuffer` implementation readers pass as `grow`.
- [[GhostJsonFlatReaderStrings]]: flat reader code that calls this kernel.
- [[GhostJsonReader]]: streaming reader that calls it.
- [[GhostJsonErrorMessages]]: supplies the string and escape messages.
- [[GhostJsonTokens]]: supplies surrogate and escape constants.
- [[GhostJsonWriterConstants]]: supplies the UTF-8 encoding constants.
- [[GhostPools]]: owns the scratch buffer pool.
