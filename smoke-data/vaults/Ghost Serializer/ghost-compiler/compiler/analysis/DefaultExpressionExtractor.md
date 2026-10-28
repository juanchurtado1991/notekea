---
tags: [ghost-serializer, compiler, analysis, defaults, parser]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/DefaultExpressionExtractor.kt
kind: object
---

# DefaultExpressionExtractor

Recovers the source text of a constructor parameter's default value, and accepts it only if it is a simple, safe literal.

## What it is about

When a model class has `val retries: Int = 3`, the generated deserializer can build the object with `retries = 3` when the JSON omits the field, instead of constructing it and then calling `.copy()`, which allocates a second object. But KSP only reports `hasDefault`, never the expression. This object reads the original source file, finds the parameter, and extracts what follows `=`. It is a small hand-written scanner, not a Kotlin parser, and that is why it is strict: it only trusts a short list of shapes.

## How it works

`extractRaw(param)` locates the parameter in the file. It takes the parameter's file location, reads the file, jumps to the reported line and searches a bounded window (about 8,000 characters) for the parameter name followed by `:`, skipping comments and string literals. If the location pointed at an annotation above the parameter, it backs up a few hundred characters and, as a last resort, scans the whole file for `name:` followed by a default. Then `findDefaultEquals` skips the type, tracking nesting of `<>`, `()`, `[]` and `{}`, until the single `=` that is not part of `==`, `!=`, `<=`, `>=` or a named-argument colon. `readExpression` reads until a top-level `,` or `)` or a line comment, and the result is trimmed.

`extract` then passes the text to `whitelist`, which returns it only when it is one of:

- `null`, `true` or `false`;
- an integer or floating literal (with optional sign and `L`, `f`, `d` suffix), but no hex or binary;
- a character literal, including a small set of escapes and `\uXXXX`;
- a plain string literal, with no `$` templates and no triple quotes;
- an empty collection call such as `emptyList()` or `mapOf()`;
- an enum-like or constant reference such as `Mode.FAST`, where every part starts with an uppercase letter or is all caps.

Anything else returns null, and callers fall back to `.copy()`. This is the safe direction, since an unrecognized expression could have side effects or refer to names not visible from the generated file.

## The code

```kotlin
internal fun whitelist(expr: String): String? {
    val trimmed = expr.trim()
    if (trimmed.isEmpty()) return null
    return when {
        trimmed in AC.DEFAULT_EXPR_SCALAR_KEYWORDS -> trimmed
        isNumericLiteral(value = trimmed) -> trimmed
        isCharLiteral(value = trimmed) -> trimmed
        isStringLiteral(value = trimmed) -> trimmed
        isEmptyCollectionCall(value = trimmed) -> trimmed
        isEnumOrConstRef(value = trimmed) -> trimmed
        else -> null
    }
}
```

`extractRaw` is also used without the whitelist by [[GhostAnalyzer]], which looks for identifiers in the raw text to find defaults that depend on other parameters.

## Related

- [[GhostAnalyzer]]: calls `extract` and `extractRaw`.
- [[DefaultExpressionQualifier]]: qualifies nested class and companion references in the accepted text.
- [[GhostAnalyzerConstants]]: the regexes, limits and keyword sets.
- [[GhostPropertyModel]]: stores the result as `defaultExpression`.
- [[GhostCommonConstants]]: the shared `"null"`, `"true"` and `"false"` literals.
