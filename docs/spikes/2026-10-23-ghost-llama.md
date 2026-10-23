# Spike — App launch, Ghost and java-llama.cpp (2026-10-23)

Goal: find out, before M1, whether the riskiest pieces of the stack work: the desktop app itself, Ghost
(JSON and YAML through KSP in a KMP module) and java-llama.cpp (embeddings and chat).
The spike ran in a throwaway `:spike` module that was deleted afterwards.

**Environment:** Linux x86_64 (Pop!_OS), JBR 21.0.11, 24 threads, AVX2. macOS and Windows were **not** tested
locally; CI covers compilation and tests there, not native model loading.

## Results

| Area | Result |
|---|---|
| App launch (`./gradlew :composeApp:run`) | ✅ Window opens (790×570) and renders. One JVM warning from Skiko about native access |
| Ghost JSON (nested DTO + list) | ✅ Decode and round trip, 47 ms cold |
| Ghost YAML `List<String>` (block and flow style) | ✅ `tags:\n  - a` and `tags: [a, b]` both decode |
| Ghost YAML unknown keys | ⚠️ Decoded without error, but **unknown keys are dropped** |
| Ghost YAML `Map<String, String>` | ✅ Decodes and encodes |
| Ghost YAML encoding style | ⚠️ Strings are always written quoted (`title: "Pricing ideas"`) |
| java-llama.cpp natives | ✅ Linux x64/arm64, macOS x64/arm64, Windows x64/x86 bundled. ❌ **No Windows ARM64** |
| Embeddings (all-MiniLM-L6-v2 Q4_K_M, 21 MB) | ✅ Load 57 ms, 384 dimensions, ~1.3 ms per short text. Similarity is meaningful: price↔pricing 0.43 vs price↔soup 0.07 |
| Chat (SmolLM2-135M-Instruct IQ4_XS, 91 MB) | ✅ Load 115 ms, coherent answer, ~311 tokens/s (tiny model; not representative of a 3B model) |
| Model integrity | ✅ Hugging Face's LFS `oid` is the file's SHA-256; both downloads matched |

## Gotchas found

1. **The JVM does not exit after closing the models.** llama.cpp leaves non-daemon native threads alive even after
   `LlamaModel.close()`; the spike hung for 12+ minutes after `main` returned. With `exitProcess(0)` it exits in
   2 s. Compose Desktop's `application {}` already calls `exitProcess` on exit (`exitProcessOnExit = true`), so the
   app is fine; **integration tests and any CLI tooling must exit the process explicitly**.
2. **Chat prompt:** `InferenceParameters("").setMessages(...).setUseChatTemplate(true)` produced an **empty answer**.
   What works: `model.applyTemplate(InferenceParameters("").setMessages(system, messages))` to build the prompt,
   then `InferenceParameters(prompt)` for `generate`.
3. **Native library extraction:** java-llama.cpp extracts `libjllama.so` to `/tmp`. On systems where `/tmp` is
   mounted `noexec` loading will fail; the app should point the extraction directory to its own data directory
   (to verify in `feature/embeddings`).
4. **Frontmatter must not be round-tripped through a DTO.** Ghost drops unknown keys and re-quotes strings, so
   decoding and re-encoding a note's frontmatter would delete user properties and change the file. Ghost is fine
   for **reading** known keys (title, tags, aliases); editing properties must change only the edited lines of the
   raw YAML text.
5. The embedding model logs `special_eos_id is not in special_eog_ids`; harmless for embeddings.

## Decisions taken from the spike

- `ROADMAP.md` 1.14 (Properties view): edits patch the raw frontmatter text; Ghost is used only to read typed values.
- `ROADMAP.md` 3.2 (Chat engine): build prompts with `applyTemplate`; integration tests exit the process.
- `LIBRARIES.md`: Windows ARM64 is not supported by java-llama.cpp; documented as a known limitation.
- Add `--enable-native-access=ALL-UNNAMED` to the desktop app's JVM arguments to silence the Skiko warning
  (with `feature/platform-conventions`).

## Still unknown

- Behavior and speed of a real 3B–4B chat model (RAM, tokens/s) — measured in `feature/model-manager`.
- Native loading on macOS (Apple Silicon and Intel) and Windows — first smoke tests on those OSes.
- GPU acceleration (Metal / CUDA / Vulkan): the bundled natives appear to be CPU builds.
