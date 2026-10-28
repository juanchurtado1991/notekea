---
tags: [ghost-serializer, compiler, analysis, defaults]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/analysis/DefaultExpressionQualifier.kt
kind: object
---

# DefaultExpressionQualifier

Rewrites a copied default value so it still compiles when pasted into the generated serializer file.

## What it is about

Ghost avoids `.copy()` calls when a constructor parameter has a default, by copying the default's source into the generated code (see [[DefaultExpressionExtractor]]). A default like `Lifecycle.Type.UNKNOWN` written inside `DeviceLifecycleEventData` may refer to a class nested in the model, or to a constant in its companion object. In the generated serializer, which is a separate top-level file, those names are not visible. This object fixes them by writing out the fully qualified name.

## How it works

`qualify(expression, model)` handles three cases and otherwise returns the text unchanged:

1. **A single identifier** (matches `SIMPLE_REFERENCE`), such as `DEFAULT_TIER`. It walks from the model outward through the enclosing classes, looking at each class's companion objects for a property of that name. If found, the result is `<companion qualified name>.DEFAULT_TIER`.
2. **A dotted reference** (`QUALIFIED_REFERENCE`), such as `Lifecycle.Type.UNKNOWN`. It searches the model and its enclosing classes for a nested class named like the first segment, and replaces that segment with the nested class's qualified name.
3. **Anything else**, such as literals or calls, is returned unchanged.

A null model also returns the text unchanged. The search through enclosing classes (`generateSequence` over `parentDeclaration`) means that a model nested in another class can use names from it.

Both patterns are regexes of plain identifiers, so this works on text only and does not call the compiler to resolve names. That keeps it small, at the price of only covering these two shapes.

## The code

```kotlin
val firstSegment = expression.substringBefore(delimiter = DOT)
val nestedClass = generateSequence(seed = model) { declaration ->
    declaration.parentDeclaration as? KSClassDeclaration
}
    .flatMap { declaration -> declaration.declarations.filterIsInstance<KSClassDeclaration>() }
    .firstOrNull { declaration -> declaration.simpleName.asString() == firstSegment }
    ?: return expression
val qualifiedName = nestedClass.qualifiedName?.asString() ?: return expression
return qualifiedName + expression.removePrefix(prefix = firstSegment)
```

## Related

- [[GhostAnalyzer]]: calls `qualify` on every extracted default.
- [[DefaultExpressionExtractor]]: produces the text that is qualified here.
- [[GhostPropertyModel]]: its `defaultExpression` holds the qualified result.
- [[GhostAnalyzerConstants]]: related patterns and whitelists for default expressions.
