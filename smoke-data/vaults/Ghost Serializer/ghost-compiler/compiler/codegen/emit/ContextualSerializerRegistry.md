---
tags: [ghost-serializer, compiler, codegen, contextual]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/ContextualSerializerRegistry.kt
kind: class
---

# ContextualSerializerRegistry

A small registry that remembers which "contextual" types a generated serializer meets and declares a lazily resolved serializer field for each.

## What it is about

Not every property type has a generated Ghost serializer. When a type is only known at runtime (for example a class from another library, with a serializer registered by the application), the generated code must look its serializer up. This registry gives each such type a stable field name and, at the end, declares the fields. A serializer emitter and a deserializer emitter each own their own instance ([[BaseSerializeEmitter]] and [[BaseDeserializeEmitter]]).

## How it works

`nameFor(type)` returns the field name for a `KSType`, creating it on first use. The name is the contextual prefix, the type's simple name with a lowercase first letter, an optional nullable suffix and the serializer suffix. The nullable and non-null versions of a type therefore get different fields.

`injectInto` adds a private property for each entry, typed as `GhostSerializer<T>` of the non-null type. The key decision is that it uses a delegate, `by lazy`-style, instead of an initializer. As the class comment explains, the generated object may be initialized (by `Ghost.prewarm()` or `getAllSerializers()`) before the registry that provides the contextual serializer has been added. A failed lookup in the initializer would leave the object permanently unusable in the process, whereas a lazy lookup retries at first use.

## The code

```kotlin
fun nameFor(type: KSType): String {
    return entries.getOrPut(type) {
        val simpleName = type.declaration.simpleName.asString()
        val nullableSuffix = if (type.isMarkedNullable) C.STR_NULLABLE_SUFFIX else ""
        C.STR_CONTEXTUAL_PREFIX +
                simpleName.replaceFirstChar { it.lowercase() } +
                nullableSuffix +
                CC.STR_SERIALIZER_SUFFIX
    }
}
```

```kotlin
.delegate(
    C.TEMPLATE_LAZY_RESOLVE_SERIALIZER,
    ghostClass,
    nonNullableTypeName
)
```

The delegate template is `lazy { %T.requireSerializer(%T::class) }`, so the serializer is looked up through the `Ghost` entry point at first access, not at object initialization.

## Related

- [[BaseSerializeEmitter]]: asks for names when it hits an unknown type while writing.
- [[BaseDeserializeEmitter]]: does the same while reading.
- [[Ghost]]: the entry point the lazy delegate calls to find the serializer.
- [[GhostSerializer]]: the type of the generated fields.
- [[GhostEmitterConstants]]: the delegate template and name pieces.
