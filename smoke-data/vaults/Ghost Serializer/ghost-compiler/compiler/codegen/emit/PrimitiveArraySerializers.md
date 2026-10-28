---
tags: [ghost-serializer, compiler, codegen, arrays]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/PrimitiveArraySerializers.kt
kind: functions
---

# PrimitiveArraySerializers

A one-function file that chooses the serializer class a generated serializer delegates to for a primitive array property such as `IntArray`.

## What it is about

Primitive arrays have no element-by-element code in the generated serializer. Ghost ships ready serializers for them in the runtime, in a JSON flavor and a YAML flavor. This function picks the right one from the channel class (reader or writer) being generated, so the same property logic works for both formats.

## How it works

The function receives the channel class and the array type name (for example `IntArray`). It looks at the channel class's simple name: if it starts with the YAML prefix, the channel is a YAML reader or writer, and the result is a class from the YAML serializer package whose name comes from the YAML array template. Otherwise it is a class in the common serializers package named after the array type plus the serializer suffix, such as `IntArraySerializer`.

It decides by the channel class's name, because the emitters only hold KotlinPoet `ClassName`s and the YAML reader and writer classes all start with `GhostYaml`.

## The code

```kotlin
internal fun primitiveArraySerializerClass(channelClass: ClassName, primitiveArrayType: String?): ClassName {
    val isYamlChannel = channelClass.simpleName.startsWith(C.STR_GHOST_YAML_PREFIX)
    return if (isYamlChannel) {
        ClassName(CC.PKG_YAML_SERIALIZER, C.TEMPLATE_YAML_ARRAY_SERIALIZER.format(primitiveArrayType))
    } else {
        ClassName(CC.STR_SERIALIZERS_PKG, "$primitiveArrayType${CC.STR_SERIALIZER_SUFFIX}")
    }
}
```

The result is used by `emitValue` in [[BaseSerializeEmitter]] and by [[BaseDeserializeEmitter]], which emits a call to the chosen serializer object for the property.

## Related

- [[BaseSerializeEmitter]]: uses it to write a primitive-array property.
- [[BaseDeserializeEmitter]]: uses it to read one.
- [[IntArraySerializer]]: an example of the JSON runtime class it names.
- [[GhostYamlIntArraySerializer]]: an example of the YAML runtime class it names.
- [[GhostEmitterConstants]]: the prefix and template constants.
