---
tags: [ghost-serializer, compiler, codegen, ksp]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/GeneratedSourceTrimmer.kt
kind: object
---

# GeneratedSourceTrimmer

A small post-processor that cleans the text KotlinPoet produces and writes the final generated file through KSP.

## What it is about

KotlinPoet is faithful but noisy: it emits imports for standard library types that Kotlin already resolves, and it writes `public` explicitly when explicit API mode is on. These do no harm, but they make generated files longer and harder to read. `GeneratedSourceTrimmer` removes that noise and is the one place where a `FileSpec` becomes a file on disk. It sits at the end of the code-generation pipeline.

## How it works

`trim` works line by line. It drops every line that matches a "redundant Kotlin import" pattern after trimming whitespace (for example `import kotlin.String`), and for the remaining lines replaces a matching redundant `public` modifier with the captured rest of the line. Both patterns are regular expressions kept in [[GhostCodegenConstants]].

`write` calls `trim` on `fileSpec.toString()`, asks KSP's `CodeGenerator` to create a `.kt` file for the spec's package and name with the given `Dependencies`, and writes the content as UTF-8 bytes, closing the stream with `use`. Passing the dependencies is what lets KSP do incremental processing correctly.

## The code

The trimming logic is short:

```kotlin
fun trim(source: String): String {
    return source.lineSequence()
        .filterNot { line -> redundantKotlinImport.matches(line.trim()) }
        .joinToString(CG.STR_NEWLINE) { line ->
            redundantPublic.replace(line, CG.STR_REGEX_GROUP_1)
        }
}
```

The write function:

```kotlin
val content = trim(source = fileSpec.toString())
codeGenerator.createNewFile(
    dependencies = dependencies,
    packageName = fileSpec.packageName,
    fileName = fileSpec.name,
    extensionName = CG.STR_EXT_KT,
).use { stream ->
    stream.write(content.toByteArray(Charsets.UTF_8))
}
```

Because it is a pure string transformation, `trim` can be tested without KSP; only `write` needs the processing environment.

## Related

- [[GhostSerializationProcessor]]: writes each generated serializer file with it.
- [[GhostModuleRegistryGenerator]]: writes the generated module registry file with it.
- [[GhostCodegenConstants]]: holds the regex patterns and string constants.
- [[GhostCodeGenerator]]: produces the `FileSpec` that usually ends up here.
