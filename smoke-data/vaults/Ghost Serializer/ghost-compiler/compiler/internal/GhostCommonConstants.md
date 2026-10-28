---
tags: [ghost-serializer, compiler, constants, naming]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/internal/GhostCommonConstants.kt
kind: object
---

# GhostCommonConstants

The vocabulary that three or more stages of the compiler share: package names, annotation names and a few tiny literals.

## What it is about

The Ghost compiler is split into stages (analysis, the KSP processor, whole-file codegen and the emitters), and the project keeps every string literal in a constants object rather than inline. Each object owns one concern, and this one holds what does not belong to a single stage. If a name is needed in the analyzer and also in an emitter, it lives here.

## How it works

Nothing runs: it is an `internal object` of `const val` strings, so the compiler inlines the values at the use site and there is no runtime lookup. The names fall into three groups:

1. Fully qualified annotation names (`com.ghost.serialization.annotations.GhostSerialization`, and the proto and YAML variants) next to their simple names such as `GhostSerialization`. The processor uses the qualified name to ask KSP for annotated symbols and the simple name to compare against annotations on a property.
2. Package names of the runtime library that generated code must import: parser (`parser.common`, `parser.strings`, `parser.bytes`, `parser.streaming`), writers (`writer.bytes`, `writer.strings`, `writer.yaml`), contracts, exceptions and types. They are what the generated source's `import` lines are built from, which is why changing a runtime package means changing one line here.
3. Small literals such as `"null"`, `"true"`, `"false"`, `"_"`, `"."`, an empty string, and `MAX_DEPENDENCY_SOURCE_COUNT = 6`.

## The code

The annotation names are written out in full once:

```kotlin
const val STR_ANNOTATION_SERIALIZATION =
    "com.ghost.serialization.annotations.GhostSerialization"

const val STR_ANNOTATION_PROTO_SERIALIZATION =
    "com.ghost.serialization.annotations.GhostProtoSerialization"

const val STR_ANNOTATION_YAML_SERIALIZATION =
    "com.ghost.serialization.annotations.GhostYamlSerialization"
```

The package constants group the runtime's modules, for instance:

```kotlin
const val PKG_PARSER_COMMON = "com.ghost.serialization.parser.common"
const val PKG_PARSER_STRINGS = "com.ghost.serialization.parser.strings"
const val PKG_PARSER_BYTES = "com.ghost.serialization.parser.bytes"
const val PKG_PARSER_STREAMING = "com.ghost.serialization.parser.streaming"
```

`STR_GENERATED_PKG` (`com.ghost.serialization.generated`) is the package where generated registries are placed, and `STR_SERIALIZER_SUFFIX` (`Serializer`) is appended to a model's name to name its generated class.

## Related

- [[GhostAnalyzerConstants]]: analyzer-only names; refers back to the package constants here.
- [[GhostEmitterConstants]]: emitter templates, built partly from these package names.
- [[GhostCodegenConstants]]: whole-file codegen vocabulary.
- [[GhostProcessorConstants]]: KSP processor vocabulary.
- [[GhostSerializationProcessor]]: uses the annotation names to find annotated classes.
- [[GhostCodeGenerator]]: uses the package names for generated imports.
