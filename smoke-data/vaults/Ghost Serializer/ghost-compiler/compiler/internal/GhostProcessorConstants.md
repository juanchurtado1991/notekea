---
tags: [ghost-serializer, compiler, constants, ksp]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/internal/GhostProcessorConstants.kt
kind: object
---

# GhostProcessorConstants

Constants for the KSP processor layer: option names, registry generation, packaging output, and the YAML error messages.

## What it is about

The processor package ([[GhostSerializationProcessor]], [[GhostModuleRegistryGenerator]], [[GhostBuildToolingWriter]], [[GhostYamlEligibility]], [[TestSourceSetDetection]]) has its own vocabulary: what to call the options a user passes through Gradle, what the registry looks like, where to write ProGuard rules, and what to say when `@GhostYamlSerialization` is misused. They are collected here, an `internal object` of `const val`s, so no processor file has inline literals.

## How it works

The values fall into these groups:

- **Options**: `ghost.moduleName`, `ghost.isTest`, `ghost.textChannel`. Together with `Default` and `_Test` they determine the registry's name (`GhostModuleRegistry_<module>`).
- **Log text**: a common prefix `>>> [GhostSerialization]` and fragments such as "Successfully optimized: " and "Critical error processing ".
- **Registry code templates**: pieces of KotlinPoet text for `mapOf(...)`, the `when (clazz)` lookup, the lazy map, and shard methods named `getShard<n>` and `getShardMap<n>`. `REGISTRY_CHUNK_SIZE = 500` is the number of classes per shard.
- **Packaging output**: `META-INF.proguard`, the file name `ghost-serialization` with extension `pro`, `META-INF.services`, and the keep-rule template.
- **YAML diagnostics**: about a dozen error messages, each starting with `GhostYamlSerialization:`. They cover the orphan case, `@GhostResilient`, sealed classes, envelopes, inferred polymorphism, custom coders, structural annotations, nested Ghost types, enums without the YAML annotation, `RawJson` and non-proto `ByteArray`.
- **Test paths**: `/src/test/`, `/src/androidTest/`, `/src/testKsp/` and the `_Test` suffix.

## The code

The keep-rule template has two `%` placeholders, the generated package and the registry class name:

```kotlin
const val TEMPLATE_PROGUARD_KEEP = $$"""
    # Ghost KSP module rules (registry + serializers for this compilation unit)
    -keep class %1$s.** { *; }
    -keep class %1$s.%2$s {
        public static ** INSTANCE;
        public *** getSerializer(...);
    }
    -keep class * implements com.ghost.serialization.contract.GhostSerializer {
        *;
    }
    // …
```

and the YAML orphan message, as an example of the diagnostics:

```kotlin
const val STR_ERR_YAML_ORPHAN =
    "GhostYamlSerialization: @GhostYamlSerialization requires @GhostSerialization or @GhostProtoSerialization on the same class."
```

## Related

- [[GhostSerializationProcessor]]: reads the option names and log text.
- [[GhostModuleRegistryGenerator]]: uses the registry templates and chunk size.
- [[GhostBuildToolingWriter]]: uses the ProGuard and ServiceLoader names.
- [[GhostYamlEligibility]]: reports the YAML errors defined here.
- [[TestSourceSetDetection]]: uses the test path markers and the option key.
- [[GhostCommonConstants]]: the shared vocabulary this object builds on.
