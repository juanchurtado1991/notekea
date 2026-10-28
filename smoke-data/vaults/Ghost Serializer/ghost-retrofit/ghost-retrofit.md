---
tags: [ghost-serializer, retrofit, index]
---

# ghost-retrofit

The Retrofit integration: a converter factory that encodes requests and decodes responses with Ghost.

Part of [[Ghost Serializer]]. Its notes, by package:


## retrofit

- [[GhostConverterFactory]]: The Retrofit `Converter.Factory` that serializes and deserializes JSON request and response bodies with Ghost.
- [[GhostProtoConverterFactory]]: The Retrofit converter factory for proto3 JSON, used with `@GhostProtoSerialization` models.
- [[GhostRetrofitBuffers]]: An internal helper that reads a Retrofit response stream into a pooled buffer for Ghost's readers.
- [[GhostRetrofitMediaTypes]]: An internal object with the media-type strings used by Ghost's Retrofit converter factories.
- [[GhostYamlConverterFactory]]: The Retrofit converter factory for YAML request and response bodies.
