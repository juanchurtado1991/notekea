---
tags: [ghost-serializer, compiler, ksp, registry, codegen]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/ksp/GhostModuleRegistryGenerator.kt
kind: class
---

# GhostModuleRegistryGenerator

Generates, with KotlinPoet, the per-module registry class that maps each model class to its generated serializer.

## What it is about

At runtime Ghost needs to go from a `KClass` (for example `User::class`) to its serializer without reflection. Each Gradle module therefore gets one generated class, `GhostModuleRegistry_<module>`, that implements the `GhostRegistry` contract. This file builds that class from the `Map<ClassName, ClassName>` collected by [[GhostSerializationProcessor]]. It was split out of the processor because the lookup and sharding strategy changes independently of KSP visiting.

## How it works

The entries are sorted by canonical class name so the output is stable between builds, then split into chunks of 500 (`REGISTRY_CHUNK_SIZE`). The generated class has:

- a private lazy property `serializersByClass` (a `Map<KClass<*>, GhostSerializer<*>>`), built from `mapOf(...)`, only touched when someone asks for all serializers;
- `getSerializer(clazz)`, a `when (clazz)` over `Foo::class -> FooSerializer` that ends with `else -> null` and a cast. A `when` was chosen so that a cold start loads only the serializer classes it needs, instead of instantiating the whole map up front;
- `prewarm()`, `registeredCount()` and `getAllSerializers()`;
- a companion `INSTANCE`, annotated with `@JvmField` only if `emitJvmField` is true.

Sharding is the reason for chunks. When there are more than 500 entries a single method would approach the JVM limit on method size, so the generator emits `getShard0`, `getShard1`, ... and `getShardMap0`, ... The main methods then just call each shard in turn. With 500 or fewer entries no shard methods are generated.

The file is written through [[GeneratedSourceTrimmer]] as an aggregating output that depends on all originating files.

## The code

The main lookup for a sharded registry is a list of calls that return early on a hit:

```kotlin
if (chunks.size > 1) {
    for (index in chunks.indices) {
        getCode.addStatement(
            PC.TEMPLATE_GET_SHARD_CALL,
            index,
            serializerType.parameterizedBy(type).copy(nullable = true)
        )
    }
    getCode.addStatement(PC.STR_RETURN_NULL)
} else {
    getCode.add(buildWhenBlock(entries, serializerType, type))
}
```

The registry file is written as an aggregating output:

```kotlin
GeneratedSourceTrimmer.write(
    fileSpec = FileSpec.builder(CC.STR_GENERATED_PKG, registryClassName)
        .addType(registrySpec)
        .build(),
    codeGenerator = codeGenerator,
    dependencies = Dependencies(aggregating = true, *originatingFiles.toTypedArray()),
)
```

## Related

- [[GhostSerializationProcessor]]: builds the map and calls `generate`.
- [[GhostRegistry]]: the interface the generated class implements.
- [[GhostProcessorConstants]]: the code templates, member names and chunk size.
- [[GeneratedSourceTrimmer]]: writes and cleans the output.
- [[GhostBuildToolingWriter]]: writes the ServiceLoader file that points at this class.
- [[GhostSerializer]]: the type of the values in the map.
