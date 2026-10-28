---
tags: [ghost-serializer, compiler, ksp, packaging]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/ksp/GhostBuildToolingWriter.kt
kind: class
---

# GhostBuildToolingWriter

Writes the two packaging files that go beside the generated registry: ProGuard/R8 keep rules and a ServiceLoader entry.

## What it is about

Generated serializers are found at runtime by class name, so a shrinker would remove them if nothing told it not to. The runtime core also needs a way to discover each module's registry without a hard dependency on it. This class creates both resources during KSP processing. It was split out of [[GhostSerializationProcessor]] because packaging conventions change independently of registry generation.

## How it works

The processor creates one instance per run with the codegen handle, the logger, the registry class name and the set of originating source files, then calls two methods.

`generateProGuardRules` fills a template with the generated package and the registry name and writes it to `META-INF/proguard/ghost-serialization.pro`. The rules keep everything in the generated package, keep the registry's `INSTANCE` field and `getSerializer` method, and keep every implementation of `GhostSerializer` and `GhostYamlSerializer`. Because they depend on the whole module, the file is registered as aggregating.

`generateServiceFile` writes `META-INF/services/com.ghost.serialization.contract.GhostRegistry`, whose single line is the fully qualified registry name. The JVM `ServiceLoader` reads this file. Here the dependency list also contains all originating files, so KSP knows when to regenerate it.

Both methods catch any exception and only log a warning. A packaging file that cannot be written should not fail the build, since Ghost still works without it (only shrinking or ServiceLoader discovery is affected).

## The code

The keep rules come from a template in [[GhostProcessorConstants]]:

```kotlin
val rules = PC.TEMPLATE_PROGUARD_KEEP
    .trimIndent()
    .format(CC.STR_GENERATED_PKG, registryClassName)
```

The service file has no extension, and its file name is the service interface itself:

```kotlin
codeGenerator.createNewFile(
    dependencies = Dependencies(
        aggregating = true,
        *originatingFiles.toTypedArray()
    ),
    packageName = PC.STR_META_INF_SERVICES,
    fileName = serviceName,
    extensionName = CC.STR_EMPTY
).use { it.write(implementationName.toByteArray()) }
```

## Related

- [[GhostSerializationProcessor]]: creates and calls this writer.
- [[GhostProcessorConstants]]: holds the template, the paths and the log messages.
- [[GhostCommonConstants]]: the generated package name used to build the class names.
- [[GhostModuleRegistryGenerator]]: produces the registry class that these files point at.
- [[GhostRegistry]]: the contract interface that the ServiceLoader entry names.
