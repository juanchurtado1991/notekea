---
tags: [ghost-serializer, compiler, codegen, formatting]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/GeneratedCallFormat.kt
kind: object
---

# GeneratedCallFormat

Helpers that format generated call expressions across several lines so the output stays readable.

## What it is about

Some calls in a generated serializer have many arguments: the options table for a class with dozens of fields lists every key name. Left to KotlinPoet's default wrapping, such a call would become one very long line. `GeneratedCallFormat` produces `CodeBlock`s with one argument per line and a trailing comma, which keeps diffs and reading manageable. It is a pure formatting utility used by the setup and emit stages.

## How it works

`invoke(name, args)` has three outcomes. With no arguments it emits `name()`. With exactly one argument whose combined length with the name is within a compact limit, it stays on one line. Otherwise it opens the call, indents, writes each argument on its own line, unindents and closes the parenthesis.

`jsonReaderOptionsOf` builds the options-table constructor call. The first arguments are fixed and in order: shift, multiplier, table size and the text-channel flag. If the hash uses the extended key form, an extra `true` follows. After that comes every key name as a string argument, one per line. The order matters because the runtime options class reads them positionally.

## The code

The multi-line form of `invoke`:

```kotlin
return CodeBlock.builder()
    .add(CG.TEMPLATE_INVOKE_OPEN, name)
    .indent()
    .apply {
        args.forEach { arg ->
            add(CG.TEMPLATE_ARG_LINE, arg)
        }
    }
    .unindent()
    .add(CG.STR_CLOSE_PAREN_FLOW)
    .build()
```

And the options builder:

```kotlin
.add(CG.TEMPLATE_TYPED_OF_OPEN, optionsClass)
.indent()
.add(CG.TEMPLATE_ARG_LINE, shift)
.add(CG.TEMPLATE_ARG_LINE, multiplier)
.add(CG.TEMPLATE_ARG_LINE, tableSize)
.add(CG.TEMPLATE_ARG_LINE, textChannel)
```

Templates are constants in [[GhostCodegenConstants]], so the exact output shape lives in one place.

## Related

- [[GhostCodegenConstants]]: holds the templates this object fills in.
- [[SerializerSetupEmitter]]: uses `jsonReaderOptionsOf` for top-level and enum tables.
- [[FlattenOptionsGenerator]]: uses it for nested option tables.
- [[StandardEmitter]]: uses these helpers for long generated calls.
- [[JsonReaderOptions]]: the runtime class whose constructor call is generated.
