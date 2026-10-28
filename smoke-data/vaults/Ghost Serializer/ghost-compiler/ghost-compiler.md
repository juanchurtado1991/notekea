---
tags: [ghost-serializer, compiler, index]
---

# ghost-compiler

The KSP processor that reads annotated classes at compile time and writes a fast, allocation-free serializer for each.

Part of [[Ghost Serializer]]. Its notes, by package:


## compiler

- [[GhostSerializationProvider]]: The entry point that KSP calls to create Ghost's annotation processor.

## compiler/analysis

- [[CustomCoderAnalyzer]]: Turns a `@GhostDecoder` or `@GhostEncoder` annotation on a property into a [[CustomCoderModel]], including which reader types the user's function accepts.
- [[DefaultExpressionExtractor]]: Recovers the source text of a constructor parameter's default value, and accepts it only if it is a simple, safe literal.
- [[DefaultExpressionQualifier]]: Rewrites a copied default value so it still compiles when pasted into the generated serializer file.
- [[DispatchNamesResolver]]: A tiny helper that lists, in a stable order, the top-level JSON field names a generated deserializer has to recognize.
- [[EnvelopeAnalyzer]]: Resolves the `@GhostJsonEnvelope` annotation of a class into a [[GhostEnvelopeModel]] and checks the conventions that the router code relies on.
- [[GhostAnalyzer]]: The first stage of code generation: it validates an annotated class against Ghost's rules and turns its properties into [[GhostPropertyModel]]s.
- [[GhostAnalyzerTypeHelpers]]: A file of small stateless functions that inspect KSP types and annotations for the analyzers.
- [[GhostPropertyExtensions]]: Extension functions on [[GhostPropertyModel]] that compute the Kotlin source snippets the deserializer and serializer emitters need for each property: local variable types, initial values, return expressions and omission rules.
- [[TextChannelPlanner]]: Decides, for every model class of a module, whether its generated serializer also gets the string-based reader and writer overloads (the "text channel").
- [[TypeHelpers]]: Extension functions on KSP's `KSType` (and a few on the property model) that answer "what kind of type is this?" for the emitters.
- [[WrappedKeysAnalyzer]]: Reads and validates `@GhostWrappedKeys`, and works out where each collapsed wire key lives inside the wrapped type.

## compiler/codegen

- [[FlattenOptionsGenerator]]: An object that emits one perfect-hash lookup table per nested object level for properties annotated with `@GhostFlatten` or wrapped paths.
- [[GeneratedCallFormat]]: Helpers that format generated call expressions across several lines so the output stays readable.
- [[GeneratedSourceTrimmer]]: A small post-processor that cleans the text KotlinPoet produces and writes the final generated file through KSP.
- [[GhostCodeGenerator]]: The class that turns the analysed model of one `@GhostSerialization` class into a complete KotlinPoet `FileSpec` holding its generated serializer object.
- [[HeaderConstantNames]]: An object that chooses collision-free names for the cached `H_*` and `HS_*` header constants of a generated serializer.
- [[JsonStringEscaping]]: An object that escapes a JSON field name at compile time, using the same rules the runtime writer applies at run time.
- [[SerializerImportResolver]]: The class that works out which reader extension functions and helper types a generated serializer file must import, and adds exactly those.
- [[SerializerSetupEmitter]]: The emitter for everything a generated serializer declares before its read and write bodies: key lookup options, cached field headers, enum tables and the `warmUp` method.

## compiler/codegen/emit

- [[BaseDeserializeEmitter]]: The abstract base of the deserialize emitters: it holds the bitmask bookkeeping and builds the reader call for every kind of property and type.
- [[BaseSerializeEmitter]]: The abstract base of the serialize-side emitters: it knows how to write one property, one nested value, one collection and the nesting objects of flattened paths into generated Kotlin.
- [[ContextualSerializerRegistry]]: A small registry that remembers which "contextual" types a generated serializer meets and declares a lazily resolved serializer field for each.
- [[DeserializeCodeEmitter]]: The coordinator that builds the generated `deserialize(reader)` function: it chooses a strategy from the class shape and also contains the sealed, enum, object and inferred-polymorphism generators.
- [[EnvelopeRouterEmitter]]: Emits the helper functions that route an envelope's payload to the right field or type based on a discriminator.
- [[FragmentedEmitter]]: The deserialize body generator for large classes: it moves state into a `DecodingContext` class and splits the key dispatch into chunk functions.
- [[FragmentedSerializeEmitter]]: The serialize emitter for large classes: it spreads the property writes over several private "chunk" functions.
- [[PrimitiveArraySerializers]]: A one-function file that chooses the serializer class a generated serializer delegates to for a primitive array property such as `IntArray`.
- [[ScalarReaderCalls]]: A lookup table that maps each Kotlin scalar type to the reader call that decodes it.
- [[ScalarWriteTemplates]]: A lookup table that maps a Kotlin scalar type to the statement the generated code uses to write it, with a different form for proto3.
- [[SerializeCodeEmitter]]: The coordinator that builds the generated `serialize(writer, value)` function: it looks at the shape of the class and delegates to the right specialised emitter.
- [[StandardEmitter]]: The deserialize body generator for ordinary classes, those with up to 40 properties.
- [[StandardSerializeEmitter]]: The serialize emitter for ordinary classes: it writes the whole object body inline in one function.
- [[WrappedKeysEmitter]]: Code-generation helpers for `@GhostWrappedKeys`: reading several scattered top-level JSON keys into one nested object.

## compiler/hash

- [[PerfectHashConfig]]: A four-field record describing a perfect hash that [[PerfectHashFinder]] found for a set of field names.
- [[PerfectHashFinder]]: Searches, at compile time, for a hash function that places every JSON field name of a class into its own slot of a small table.

## compiler/internal

- [[GhostAnalyzerConstants]]: What the analyzers look for and what they say: annotation names, qualified type names, annotation argument names, default-expression whitelists and every diagnostic text.
- [[GhostCodegenConstants]]: Constants for whole-file code generation: names of generated members, call templates, and the regexes that tidy generated source.
- [[GhostCommonConstants]]: The vocabulary that three or more stages of the compiler share: package names, annotation names and a few tiny literals.
- [[GhostEmitterConstants]]: The largest constants file of the compiler: code templates and snippets that the emitters put inside generated `serialize` and `deserialize` functions.
- [[GhostPerfectHashConstants]]: The numbers and error messages that bound the perfect-hash search.
- [[GhostProcessorConstants]]: Constants for the KSP processor layer: option names, registry generation, packaging output, and the YAML error messages.

## compiler/ksp

- [[GhostBuildToolingWriter]]: Writes the two packaging files that go beside the generated registry: ProGuard/R8 keep rules and a ServiceLoader entry.
- [[GhostModuleRegistryGenerator]]: Generates, with KotlinPoet, the per-module registry class that maps each model class to its generated serializer.
- [[GhostSerializationProcessor]]: The KSP processor that drives Ghost's whole compile-time pipeline: find annotated classes, analyze them, generate their serializers and write the per-module registry.
- [[GhostYamlEligibility]]: Decides whether a class gets YAML serializer code, and reports a build error when `@GhostYamlSerialization` is used in a way that cannot work.
- [[TestSourceSetDetection]]: A small helper that guesses whether the current KSP run is compiling test sources.

## compiler/model

- [[CustomCoderModel]]: Holds what the compiler knows about a user-supplied encoder or decoder for one property.
- [[CustomCoderReaderKind]]: An enum naming the three reader types a custom coder function can take as its first parameter.
- [[EnvelopePayloadMapping]]: One routable payload slot of an envelope: a discriminator value, the field that holds the payload and, optionally, the type to decode it into.
- [[GhostEnvelopeModel]]: The analyzed form of a class annotated with `@GhostJsonEnvelope`.
- [[GhostPropertyModel]]: The compiler's description of one serializable property: everything the analyzer learned about it, in a form the emitters can use without touching KSP again.
- [[GhostSerializerContext]]: An immutable bundle of every fact the code generator needs about one class, computed once.
- [[InferredSubclassModel]]: Pairs a subclass of a sealed class with its analyzed properties, so inferred polymorphism can choose between subclasses by looking at the keys.
- [[WrappedKeysModel]]: A small record with everything `@GhostWrappedKeys` adds to one property.
- [[WrappedUnwrapFieldModel]]: Describes one wire field that is written out when a `@GhostWrappedKeys` property is unwrapped during serialization.
