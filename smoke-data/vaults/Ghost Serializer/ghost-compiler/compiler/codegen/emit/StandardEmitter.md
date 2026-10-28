---
tags: [ghost-serializer, compiler, codegen, deserialize]
source: ghost-compiler/src/main/kotlin/com/ghost/serialization/compiler/codegen/emit/StandardEmitter.kt
kind: class
---

# StandardEmitter

The deserialize body generator for ordinary classes, those with up to 40 properties.

## What it is about

For most DTOs the generated `deserialize` function can be a single method: declare a local variable per property, loop over the JSON keys, validate that required fields were seen, and construct the object. `StandardEmitter` generates that method. Large classes use [[FragmentedEmitter]] because of the JVM's method size limit. It is created by [[DeserializeCodeEmitter]].

## How it works

`emit` runs these steps: declare mask constants (aggregate `MASK_REQUIRED_N` only when more than one field is required), wrapped-key constants and capture variables ([[WrappedKeysEmitter]]), local tracking variables and mask variables, then the parse loop. The loop calls `selectNameAndConsume` with the perfect-hash options and a `when` on the index. Each branch assigns one property (or captures a wrapped key, or enters a flattened group). `-1` breaks the loop and `-2` skips an unknown key. After the loop comes wrapped-key post-processing, a call to the validation helper (before `endObject`, so the error's JSON path still includes the current object), `endObject` and the return statement.

The return statement is the intricate part, because Kotlin default values must still apply for absent fields:

- **No defaults:** one constructor call with named arguments.
- **Up to 4 default properties:** a multi-branch return, one `if` per subset of present defaults, ordered from most bits to fewest, with the plain constructor as the fallback. Each branch is a direct call.
- **More defaults:** a private `createInstance` helper receives the masks and local values. It either builds one constructor call where each default argument is `if (present) parsed else default` (when every default has a known expression), or the "legacy" form: construct with required args and `.copy(...)` the present defaults. Defaults that other defaults read are passed to the constructor, with one branch per combination (up to 6 sources), so dependent defaults are computed from the decoded values.

The validation helper checks a single required field directly, or tests the aggregate mask and then names the first missing field in a chain of `if/else if`.

## The code

```kotlin
emitLocalVariables(body = body)
WrappedKeysEmitter.emitCaptureVariables(body = body, properties = properties)
emitMaskVariables(body = body)
emitParseLoop(body = body)
// …
emitFieldValidationCall(body = body)
body.addStatement(C.STR_END_OBJECT)
emitReturnStatement(body = body, typeSpecBuilder = typeSpecBuilder)
```

Resilient properties are assigned inside `reader.decodeResilient { ... }?.let { ... }`, and the mask bit is set only if decoding succeeded.

## Related

- [[BaseDeserializeEmitter]]: the superclass.
- [[DeserializeCodeEmitter]]: creates it for normal classes.
- [[FragmentedEmitter]]: the alternative for large classes.
- [[WrappedKeysEmitter]]: wrapped-key capture and materialization.
- [[GeneratedCallFormat]]: formats the long `createInstance` call.
- [[DispatchNamesResolver]]: gives the top-level names that define the branch indexes.
- [[GhostPropertyModel]]: the property data.
