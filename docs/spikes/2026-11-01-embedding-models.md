# Spike — Multilingual embedding models (2026-11-01)

Goal (`ROADMAP.md` 2.2): choose the embedding model Notekea bundles. The developer's requirement: it must be
**multilingual** (notes and questions in Spanish and English, mixed); an English-only model does not serve.

The spike ran as a temporary test in `composeApp` (deleted afterwards), with java-llama.cpp 4.2.0 (natives built
2025-06-20) on the chunks of 2.1.

**Environment:** Linux x86_64, AMD Ryzen 9 7900X (12 cores, 24 threads, AVX2), JBR 21, 12 threads, context 512.

## Candidates

All from Hugging Face, quantized to Q8_0 (Q4_K_M for bge-m3): multilingual vocabularies (~250 000 tokens) are most
of these models' size, so a smaller quantization saves little.

| Model | License | Dimensions | Context | File | Loads? |
|---|---|---|---|---|---|
| `granite-embedding-107m-multilingual` (bartowski Q8_0) | Apache 2.0 | 384 | 512 | 121 MB | ✅ |
| `multilingual-e5-small` (TwinSunsLLC Q8_0) | MIT | 384 | 512 | 132 MB | ✅ |
| `multilingual-e5-small` (cstr Q8_0) | MIT | 384 | 512 | 132 MB | ❌ "could not load model" |
| `multilingual-e5-small` (keisuke-miyako Q8_0) | MIT | 384 | 512 | 132 MB | ⚠️ loads, broken results |
| `paraphrase-multilingual-MiniLM-L12-v2` (cstr Q8_0) | Apache 2.0 | 384 | 128 | 131 MB | ❌ "could not load model" |
| `bge-m3` (gpustack Q4_K_M) | MIT | 1024 | 8192 | 438 MB | ✅ |

## Results

Twenty questions over the Sample Vault, ranked by each note's best chunk: twelve in **Spanish** about notes written
in English (cross-lingual: "¿Cuánta agua lleva la masa de maíz?" → Arepas, "cotización de encimeras de cuarzo" →
Vendor call) and eight English paraphrases ("coffee mixed with hot milk" → Café con leche).

| Model | Recall@1 | Recall@5 | MRR | Load | Memory added | Short chunk | Code chunk (Ghost Serializer) |
|---|---|---|---|---|---|---|---|
| **granite-embedding-107m-multilingual** | **20/20** | 20/20 | 1.00 | 0.42 s | ~330 MB | **2.9 ms** | **15 ms** |
| multilingual-e5-small (TwinSuns) | 19/20 | 20/20 | 0.98 | 0.45 s | ~300 MB | 4.4 ms | 30 ms |
| multilingual-e5-small (keisuke) | 4/20 | 6/20 | 0.27 | 0.23 s | ~210 MB | 4.0 ms | 23 ms |
| bge-m3 | 20/20 | 20/20 | 1.00 | 0.45 s | ~650 MB | 43 ms | 278 ms |

"Memory added" is the process's resident memory before and after loading the model and embedding the Sample Vault;
it includes the mapped model file and llama.cpp's buffers.

## Tokens

Measured with granite's tokenizer on all 1 027 chunks of `smoke-data` (2.1): the estimate of 2.1 (four characters a
token) **undercounts 987 of them**, by up to 1.72×, and the largest chunk is 523 tokens, over the 512 the model reads.
Multilingual tokenizers cut code and rare words into many pieces: the worst chunk has 2.13 characters a token (code),
the median 3.3 (prose). An estimate of one token per 2.2 characters never undercounts any chunk.

## Recommendation (for the developer to decide)

- **Model: `granite-embedding-107m-multilingual`, Q8_0** (bartowski's conversion, SHA-256
  `15691dfe830257f5…`, checked in full when packaging). Best results, fastest, smallest, Apache 2.0, no prefixes for
  questions or passages. bge-m3 is no better on this set and an order of magnitude slower and larger.
- **`TokenEstimate` counts one token per 2.2 characters** (and at least two per word), so a chunk of at most 400
  estimated tokens always fits the model's 512. Prose chunks become smaller in real tokens (about 200 to 270), within
  the 200–400 of `ROADMAP.md` 2.1. The embedder also refuses a text longer than the model's context rather than
  cutting it silently.
- java-llama.cpp loads its natives from a folder given by `de.kherud.llama.lib.path`: the adapter extracts them to the
  app's data folder instead of `/tmp` (the spike of 2026-10-23, gotcha 3).
- The question set is easy (two models are perfect); 2.8 builds the evaluation set that tells close models apart.

## Still unknown

- Speed and memory on macOS (Apple Silicon, Metal) and Windows, measured in the M2 performance pass (2.9).
- Whether a newer java-llama.cpp loads the models that fail here; not needed for granite.
