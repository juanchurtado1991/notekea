---
tags: [ghost-serializer, compiler-plugin, kotlin-ir, native, wasm]
source: ghost-compiler-plugin/src/main/kotlin/com/ghost/serialization/compiler/plugin/GhostSerializerLinkExtension.kt
kind: class
---

# GhostSerializerLinkExtension

The IR extension that does the plugin's real work: it attaches a link annotation to every Ghost model class.

## What it is about

On the JVM, Ghost finds serializers through ServiceLoader. Kotlin/Native and Kotlin/Wasm have no such mechanism, so the runtime uses associated objects: a class is annotated with `@GhostSerializerLink(<Model>Serializer::class)` and the runtime resolves the serializer from it. Writing that annotation by hand would be a chore, so this extension adds it during compilation. It is the core of `ghost-compiler-plugin`.

## How it works

`generate` runs once per module. It first resolves `GhostSerializerLink`; if the class is not on the classpath (JVM targets, older runtimes) it returns and does nothing. Otherwise it collects every class in every file by walking declarations recursively by hand rather than with an IR visitor, to depend on as few compiler APIs as possible.

For each class `linkSerializer` does the following:

1. Skip it if it already carries the link annotation.
2. Decide the owner: the class itself when it has `@GhostSerialization` or `@GhostProtoSerialization`, otherwise the annotated sealed parent (so unannotated direct subclasses of a sealed model are linked to the parent's serializer, the same mapping the KSP registry uses).
3. Compute the serializer name from the owner class id: nested segments joined with `_`, plus the suffix `Serializer`.
4. Look the serializer up; if it is missing for a real model, report a compiler warning and skip it.
5. Append the annotation, built with a class reference to the serializer.

## The code

```kotlin
private fun isGhostModel(
    irClass: IrClass
): Boolean = irClass.hasAnnotation(classId = C.GHOST_SERIALIZATION_ID) ||
    irClass.hasAnnotation(classId = C.GHOST_PROTO_SERIALIZATION_ID)
```

`buildLinkAnnotation` creates the annotation through [[GhostCompilerCompat]] and sets its first argument to an `IrClassReferenceImpl` of type `KClass<Serializer>`. The missing-serializer case is only a warning because KSP may legitimately not have run for that source set, and the developer can still call `Ghost.addRegistry`.

## Related

- [[GhostCompilerPluginRegistrar]]: installs this extension unless disabled.
- [[GhostCompilerCompat]]: builds the annotation portably across Kotlin versions.
- [[GhostCompilerPluginConstants]]: class ids, suffixes and warning text.
- [[GhostSerializerLink.native]]: the runtime link annotation on Kotlin/Native.
- [[GhostSerializerLink.wasmJs]]: the runtime link annotation on Kotlin/Wasm.
- [[GhostSerializerLookup]]: the runtime lookup that ends up finding the serializer this annotation links.
- [[GhostSerialization]]: the annotation that triggers linking.
