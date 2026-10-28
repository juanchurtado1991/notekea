---
tags: [ghost-serializer, compiler, ksp, codegen]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/ksp/GhostSerializationProcessor.kt
kind: class
---

# GhostSerializationProcessor

The KSP processor that drives Ghost's whole compile-time pipeline: find annotated classes, analyze them, generate their serializers and write the per-module registry.

## What it is about

This is the conductor of the compiler. It does not parse properties or write Kotlin code itself. It calls the stages in order: [[GhostAnalyzer]] (reads the class), [[TextChannelPlanner]] (decides which classes also get a string-based reader), [[EnvelopeAnalyzer]], [[GhostYamlEligibility]], [[GhostCodeGenerator]] (writes the file) and finally [[GhostModuleRegistryGenerator]] and [[GhostBuildToolingWriter]]. The reason for all this work at build time is that nothing needs reflection when the app runs.

## How it works

`process` runs once per KSP round:

1. It collects all symbols annotated with `@GhostSerialization` or `@GhostProtoSerialization`. Declarations that are not classes are returned so KSP retries them in a later round.
2. Each class is analyzed into a list of property models. A failure in one class is logged against that class and the others continue.
3. `TextChannelPlanner.plan` picks, per class, whether to emit the text channel, using the `ghost.textChannel` option (`true`, `false` or unset) as a module-wide override.
4. Each class goes through `processClass`, which generates the serializer (skipping a file already produced), records `model class -> serializer class`, and for a sealed class also records every sealed subclass against the same serializer.
5. After the loop, orphan `@GhostYamlSerialization` usages are reported. If at least one class was valid, the registry, the ProGuard rules and the ServiceLoader entry are written.

The registry name is `GhostModuleRegistry_<module>`, built lazily from the `ghost.moduleName` option with `-` and `.` replaced by `_`. Without a module name it is `Default`, and `_Test` is appended in a test compilation so the main and test registries do not collide (see [[TestSourceSetDetection]]).

## The code

The class name used for the generated serializer joins nested names, so `Outer.Inner` becomes `Outer_InnerSerializer`:

```kotlin
return ClassName(
    packageName,
    classDeclaration
        .toClassName()
        .simpleNames
        .joinToString(CC.STR_UNDERSCORE)
            + CC.STR_SERIALIZER_SUFFIX
)
```

The output is written with non-aggregating dependencies on the single source file, so KSP can regenerate only what changed:

```kotlin
GeneratedSourceTrimmer.write(
    fileSpec = fileSpec,
    codeGenerator = codeGenerator,
    dependencies = Dependencies(
        aggregating = false,
        classDeclaration.containingFile!!
    )
)
```

The registry and tooling files, in contrast, are aggregating, because they depend on all the classes of the module.

## Related

- [[GhostSerializationProvider]]: creates this processor.
- [[GhostAnalyzer]]: turns each class into property models.
- [[GhostCodeGenerator]]: builds the serializer file for one class.
- [[GeneratedSourceTrimmer]]: writes and cleans the generated file.
- [[GhostModuleRegistryGenerator]]: writes the registry from the collected map.
- [[GhostBuildToolingWriter]]: writes the keep rules and ServiceLoader file.
- [[GhostYamlEligibility]]: decides if the YAML serializer is also generated.
