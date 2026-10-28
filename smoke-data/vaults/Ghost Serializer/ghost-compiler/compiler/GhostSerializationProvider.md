---
tags: [ghost-serializer, compiler, ksp, entry-point]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/GhostSerializationProvider.kt
kind: class
---

# GhostSerializationProvider

The entry point that KSP calls to create Ghost's annotation processor.

## What it is about

KSP (Kotlin Symbol Processing) finds compiler plugins through a `SymbolProcessorProvider`. This class is that provider for `ghost-compiler`: it is the first piece of Ghost code that runs in a build. Its only job is to look at the environment KSP hands over and build a [[GhostSerializationProcessor]] with the right settings.

## How it works

KSP gives the provider a `SymbolProcessorEnvironment` containing the code generator (the tool that writes new source files), a logger, the processor options passed from Gradle, and the list of target platforms of the current run.

The provider copies those four things into the processor and computes one extra flag, `emitJvmField`. The generated serializers may carry `@JvmField`, which in Kotlin Multiplatform is an optional expectation. A single-target Kotlin/Native or Kotlin/Wasm compilation (for example `kspWasmJs`) rejects it, so the flag is true only when the run covers more than one platform (a shared metadata run) or when JVM is among the platforms. Computing this once here keeps every later stage free of platform checks.

The provider holds no state of its own, and KSP creates it once per processing round set.

## The code

`create` is the only member:

```kotlin
class GhostSerializationProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val platforms = environment.platforms
        val emitJvmField = platforms.size != 1 || platforms.any { it is JvmPlatformInfo }
        return GhostSerializationProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options,
            emitJvmField = emitJvmField
        )
    }
}
```

The condition reads as "not a single-platform run, or a platform that is JVM". Note that `options` is the raw string map; the processor itself decides which keys it understands (module name, test flag, text channel), see [[GhostProcessorConstants]] for the key names.

## Related

- [[GhostSerializationProcessor]]: the processor this provider constructs.
- [[GhostProcessorConstants]]: names of the option keys the processor reads from the options map.
- [[TestSourceSetDetection]]: one of the things the processor does with those options.
- [[GhostModuleRegistryGenerator]]: produced later by the processor to register all serializers of the module.
