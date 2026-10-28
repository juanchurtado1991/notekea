---
tags: [ghost-serializer, serialization, exception, errors]
source: ghost-serialization/src/commonMain/kotlin/com/ghost/serialization/exception/GhostJsonException.kt
kind: class
---

# GhostJsonException

The exception Ghost throws for malformed JSON or invalid structure, carrying the path, line, column and a fix hint.

## What it is about

Every parser (flat, streaming, string) and the writers report errors through this type. The message points at where the error happened and, when it can, tells the developer how to fix it. It also holds the helper that maps known error messages to hint texts.

## How it works

The exception stores a base message, a JSONPath such as `$.user.addresses[1].zip` (`"$"` when unknown), and an optional hint. Line and column are computed lazily: the primary constructor takes a `computeLineCol` function that is only run, via `lazy`, if someone reads `line`, `column` or `message`. That matters because parsers sometimes raise this exception inside tight probing loops, and scanning the input to count lines is O(N). A public secondary constructor accepts ready-made line and column values (default `-1`).

`message` is built as `"<base> [at line L, col C, path P]"`, followed by a newline and `Hint: ...` if a hint exists. The file also defines the internal `String.hintForJsonError()`, a `when` that matches the beginning of the message against known error prefixes (unknown field, coercion disabled, trailing comma, depth exceeded, required field, unknown discriminator, bad enum, protobuf base64 and range errors and so on) and returns the matching constant from `GhostJsonHintMessages`, or `null` so that unknown errors stay quiet.

## The code

```kotlin
class GhostJsonException @InternalGhostApi internal constructor(
    private val baseMessage: String,
    private val computeLineCol: () -> IntArray,
    val path: String = ROOT_PATH,
    val hint: String? = null,
) : RuntimeException() {

    private val lineCol: IntArray by lazy(mode = LazyThreadSafetyMode.NONE) {
        computeLineCol()
    }
    val line: Int get() = lineCol[0]
    val column: Int get() = lineCol[1]
    // …
}
```

```kotlin
internal fun String.hintForJsonError(): String? = when {
    startsWith(prefix = EM.STRICT_MODE_UNKNOWN_FIELD) -> HINT_UNKNOWN_FIELD
    startsWith(prefix = EM.ERR_COERCION_DISABLED) -> HINT_COERCION_DISABLED
    // …
    else -> null
}
```

## Related

- [[GhostJsonHintMessages]]: the hint texts returned by `hintForJsonError`
- [[GhostJsonErrorMessages]]: the error message prefixes it matches
- [[GhostJsonFlatReader]]: parser that raises it
- [[GhostJsonReader]]: streaming parser that raises it
- [[GhostJsonStringReader]]: string parser that raises it
- [[GhostYamlException]]: YAML counterpart
- [[GhostJsonWriter]]: throws it for non-finite numbers and excessive depth
