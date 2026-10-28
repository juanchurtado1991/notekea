---
tags: [ghost-serializer, compiler-plugin, compatibility, kotlin-ir]
source: ghost-compiler-plugin/src/main/kotlin/com/ghost/serialization/compiler/plugin/GhostCompilerCompat.kt
kind: object
---

# GhostCompilerCompat

An internal object that hides differences between Kotlin compiler versions, so one plugin artifact works on several of them.

## What it is about

Kotlin compiler plugin APIs are not stable. The Ghost plugin is compiled against Kotlin 2.2.21 but must also load on newer compilers. Two APIs changed in incompatible ways by Kotlin 2.4, and calling them directly would fail to link. `GhostCompilerCompat` isolates those two calls and resolves them at runtime. It is part of `ghost-compiler-plugin`.

## How it works

The first difference is how an extension is registered. `ExtensionStorage.registerExtension` takes a `ProjectExtensionDescriptor` up to 2.3 and an `ExtensionPointDescriptor` from 2.4. `registerIrGenerationExtension` reads the `Companion` field of `IrGenerationExtension` by reflection, finds the two-argument `registerExtension` method by name and arity, and invokes it.

The second is how annotations are built. Kotlin 2.4 wants an `IrAnnotation` made with a factory named `IrAnnotationImpl`. `newAnnotation` looks that factory up lazily in `BuildersKt` (a method with that name and eight parameters). If it exists it is called; otherwise the code falls back to `IrConstructorCallImpl.fromSymbolOwner`, which older compilers expect.

Reflection costs something, but both bridges run once per compilation or per linked class and never on a serialize or deserialize path.

## The code

```kotlin
private val annotationFactory: Method? by lazy {
    val builders = Class.forName(
        BUILDERS_CLASS,
        false,
        IrConstructorCallImpl::class.java.classLoader
    )
    builders.methods.firstOrNull { method ->
        method.name == ANNOTATION_FACTORY && method.parameterCount == ANNOTATION_FACTORY_ARITY
    }
}
```

`newAnnotation` returns `IrConstructorCall` either way, with no type arguments and `SourceElement.NO_SOURCE`.

## Related

- [[GhostCompilerPluginRegistrar]]: calls `registerIrGenerationExtension` to install the extension.
- [[GhostSerializerLinkExtension]]: calls `newAnnotation` to attach the link annotation.
- [[GhostCompilerPluginConstants]]: the other shared helper of the plugin module.
- [[GhostCompilerPluginSupport]]: the Gradle side that decides which Kotlin versions are considered tested.
