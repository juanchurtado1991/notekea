# Spike — Parallel inference and chat models (2026-11-07)

Goal (`ROADMAP.md` 3.0): measure what 3.1–3.9 and 4.7 need before the chat engine is built: parallel slots, the GPU
route, prompt reading time, memory, grammar, the reranker, picking actions, and which open models become the Small,
Medium and Large defaults.

**Linux only.** Only a Linux machine was available, so Windows and macOS were not measured (the developer agreed on
2026-11-07): their numbers come from 3.7 and the release run. The scope change is recorded in `ROADMAP.md` 3.0.

The spike ran outside the repository, as a small Java server (one loaded model, driven by JSON lines) and Python
scripts that measure it from outside: resident memory from `/proc`, video memory from `nvidia-smi`. Nothing in the
app changed.

**Environment:** Linux x86_64, AMD Ryzen 9 7900X (12 cores, 24 threads, AVX2), 62 GB, NVIDIA RTX 5060 Ti 16 GB
(driver 595.104.02), JDK 21. `net.ladenthin:llama` 6.0.0 (the app's version since 2.9; llama.cpp build b11887).
"cpu12" and "cpu4" are the CPU with 12 or 4 threads (4 stands in for a modest laptop); "GPU" is Vulkan with every layer
on the GPU. Context 4,096 tokens per slot unless stated; seed 42; temperature 0.2.

## Candidates

Open models current in November 2026, chosen for Spanish and English and for following a JSON grammar; all Q4_K_M,
all Apache 2.0, none gated, every file checked against its SHA-256. File sizes are in GB of 10⁹ bytes; memory is in
MB as `/proc` and `nvidia-smi` report it (2²⁰ bytes).

| Size | Model | File | Source |
|---|---|---|---|
| Small | Qwen3.5-2B | 1.28 GB | unsloth |
| Small | Gemma 4 E2B (instruct) | 3.11 GB | unsloth |
| Medium | granite-4.2-3b | 2.24 GB | ibm-granite |
| Medium | Qwen3.5-4B | 2.74 GB | unsloth |
| Medium | Gemma 4 E4B (instruct) | 4.98 GB | unsloth |
| Large | granite-4.2-8b | 5.35 GB | ibm-granite |
| Large | Qwen3.5-9B | 5.68 GB | unsloth |
| Reranker | bge-reranker-v2-m3 | 0.44 GB | gpustack |
| Reranker | Qwen3-Reranker-0.6B (Q8_0) | 0.64 GB | ggml-org |

**Reasoning must be turned off.** Qwen3.5 and Gemma 4 think before they answer by default. The template flag
`enable_thinking: false` is not enough for Gemma, which still spends the whole token budget thinking and returns no
answer; a reasoning budget of 0 (`withReasoningBudgetTokens(0)`) stops it. Granite does not reason, and a budget of 0
on Granite with a JSON grammar fails every call ("Unexpected empty grammar stack after accepting piece"), so the
budget is set only for the models that reason. The model catalog of 3.1 records it per model.

## Parallel slots

`setParallel(n)` with `setCtxSize(ctx × n)` and a separate KV cache per slot (`setKvUnified(false)`); four calls from
four threads at the same time.

- **Each call gets its own slot:** four answers of 96 tokens take, one after another and then at the same time,
  5.8 and 2.1 s (Qwen3.5-2B, cpu12), 13.2 and 4.8 s (Qwen3.5-4B, cpu12), 1.0 and 0.6 s (Qwen3.5-2B, GPU).
- **Same seed, same output:** a single call repeated with the same seed returns the same text every time, on CPU and
  GPU. **Run in parallel, the text changes:** batched calls add the numbers in another order, so on CPU most answers
  differ from the single call and on GPU some do. Tests and the 3.4 evaluation must fix the slot count; no test may
  expect a parallel answer to equal a single one.

### Tokens per second, all slots together

| Model | GPU 1 / 2 / 4 slots | GPU video memory 1 / 4 slots | cpu12 1 / 2 / 4 slots | cpu4 1 / 2 / 4 slots |
|---|---|---|---|---|
| Qwen3.5-2B | 196 / 310 / 272 | 1,352 / 1,560 MB | 33 / 59 / 98 | 37 / 53 / 71 |
| Gemma 4 E2B | 152 / 258 / 148 | 1,570 / 1,698 MB | 30 / 53 / 100 | 31 / – / – |
| granite-4.2-3b | 137 / 224 / 255 | 2,404 / 3,365 MB | 23 / 43 / 80 | 24 / – / – |
| Qwen3.5-4B | 102 / 165 / 208 | 2,880 / 3,421 MB | 15 / 26 / 42 | 17 / 23 / 31 |
| Gemma 4 E4B | 90 / 152 / 207 | 3,134 / 3,447 MB | 15 / 27 / 54 | 16 / – / – |
| granite-4.2-8b | 68 / 126 / 165 | 5,641 / 7,564 MB | 9.7 / 18 / 35 | 10 / – / – |
| Qwen3.5-9B | 63 / 97 / 128 | 5,171 / 5,710 MB | 8.9 / 16 / 29 | 9.7 / – / – |

(Gemma 4 E2B at 2 slots on GPU first measured 27, an outlier; three new runs gave 255–258.)

- **Slots pay off on the CPU too**, against what M3's introduction assumed: writing one answer is limited by memory
  bandwidth, not by the cores (4 threads write as fast as 12), and a batch of slots reads the weights once for all of
  them. With 4 threads the gain is smaller (Qwen3.5-2B 37 → 71 tokens/s with 4 slots) and each answer is slower
  (20 tokens/s each). The independent steps of 3.5 run in parallel on the CPU as well; the introduction of M3 is
  corrected.
- **RAM per extra slot is small** for these models, because most of their layers keep no growing KV cache (Qwen3.5
  and Granite 4 are hybrid models, Gemma 4 uses sliding windows): from 1 to 4 slots at 4k, resident memory grows by
  about 95 MB a slot for Qwen3.5-2B, 50 MB for Gemma 4 E2B, 240 MB for Qwen3.5-4B and 9B, 330 MB for granite-3b and
  650 MB for granite-8b.

## The GPU route

What `net.ladenthin:llama` 6.0.0 publishes on Maven Central, as natives jars that go **next to** the CPU jar (the CPU
jar must stay on the classpath, or nothing loads):

| System | GPU jars |
|---|---|
| Linux x86-64 | `vulkan` (10 MB), `cuda13` (175 MB), `rocm`, `sycl`, `openvino` |
| Windows x86-64 | `vulkan`, `cuda13`, `rocm`, `sycl`, `openvino`, `opencl` |
| macOS | `metal-macos-aarch64` only: Apple Silicon, no Intel Mac jar |

- **Vulkan works with nothing but the GPU driver** (`libvulkan.so.1`), on NVIDIA, AMD and Intel alike, and costs 10 MB
  in the installer. **CUDA does not**: its jar needs `libcudart.so.13`, `libcublas.so.13` and `libcublasLt.so.13`
  from the CUDA runtime, which it does not include and which are hundreds of megabytes; without them the device
  does not register and the model loads on the CPU without any error. CUDA was not measured (the runtime was not
  installed).
- **Without a Vulkan driver** the model also loads on the CPU without an error (34 tokens/s instead of 168).
- **Layers:** with Gemma 4 E2B, 0, 8, 16 and all of its layers on the GPU give 30, 44, 58 and 168 tokens/s and 141,
  657, 988 and 1,570 MB of video memory: a partial offload helps little, so Notekea puts every layer on the GPU when it
  fits and uses the CPU otherwise. `-1` and `99` both mean every layer.
- **Detection:** the library has no call that lists devices or their video memory; the device appears only in the
  llama.cpp log (`ggml_vulkan: 0 = NVIDIA GeForce RTX 5060 Ti …`). 3.1 reads the video memory itself (Vulkan's memory
  budget, or the system's tools) and 3.2 checks the log to know which device was used.
- **A model too large for the GPU kills the process:** Gemma 4 E2B with 16 slots of 32k (a 512k cache) ended in a
  native abort ("pre-allocated tensor … cannot run the operation"), with no exception to catch. Notekea must check the
  size before loading (file + KV cache of every slot + about 300 MB) and never load what does not fit, or load chat
  models in a separate process. **Decision for 3.2: Vulkan on Windows and Linux, Metal on macOS, every layer on the GPU
  when the check passes, CPU otherwise.**

## Time to the first token

Milliseconds to read a prompt of about 1k / 2k / 4k tokens of notes (one slot); writing starts after it.

| Model | GPU | cpu12 | cpu4 |
|---|---|---|---|
| Qwen3.5-2B | 648¹ / 239 / 465 | 1,800 / 3,600 / 7,400 | 3,600 / 7,100 / 14,600 |
| Gemma 4 E2B | 539 / 331 / 664 | 2,100 / 4,500 / 9,300 | 4,900 / 9,900 / 20,900 |
| granite-4.2-3b | 367 / 418 / 933 | 3,100 / 6,600 / 15,300 | 7,700 / 16,500 / 38,000 |
| Qwen3.5-4B | 311 / 577 / 1,151 | 4,000 / 8,000 / 16,500 | 8,800 / 17,700 / 36,200 |
| Gemma 4 E4B | 319 / 577 / 1,139 | 4,000 / 8,100 / 16,700 | 9,600 / 19,100 / 39,200 |
| granite-4.2-8b | 620 / 882 / 1,905 | 7,300 / 14,900 / 32,800 | 18,400 / 37,600 / 82,000 |
| Qwen3.5-9B | 469 / 883 / 1,763 | 6,600 / 13,000 / 26,400 | 15,400 / 30,600 / 61,900 |

¹ The first call warms the GPU up.

On the CPU, reading the notes takes longer than writing the answer: 4k tokens take 15 s with Qwen3.5-2B on 4 threads.
So 3.5 sends each chunk to its own short extraction call (a few hundred tokens each, read in parallel), and only the
extracted quotes reach the answer.

## Memory

Resident memory of the server process (a JVM with a 256 MB heap limit plus the model), cpu12, one slot.

| Model | 2k | 4k | 8k | 4k, no mmap |
|---|---|---|---|---|
| Qwen3.5-2B | 1,998 | 2,022 | 2,070 | **1,532** |
| Gemma 4 E2B | 4,279 | 4,291 | 4,316 | – |
| Qwen3.5-4B | 4,306 | 4,369 | 4,497 | **3,076** |
| Qwen3.5-9B | 8,429 | 8,493 | 8,621 | **5,886** |

- **The context size costs little memory** (2k to 8k adds 70–190 MB) for the same reason slots are cheap; it costs
  prompt reading time instead.
- **The KV cache in 8 bits (`Q8_0`) saves only 10–120 MB** at these sizes, with no visible change in speed, and the
  same quality on the evaluation set (Qwen3.5-4B: 0.96 answered, 0.81 quotes exact, 0.89 right note, every
  unanswerable question declined; 16 bits: 0.95, 0.81, 0.88, all declined). Worth it only with long contexts or many
  slots.
- **On the CPU, mmap doubles the weights.** With the default load mode the CPU backend copies the weights into its own
  layout and the mapped file stays in memory too: resident memory is about 1.6 times the file. Loading without mmap
  (`LoadMode.NONE`) saves 24–31 % with the same speed, and loading takes 0.2–1.2 s longer. **Decision for 3.2: no
  mmap on the CPU** (this changes the "read with mmap" line of 3.2). On the GPU the weights live in video memory and
  resident memory was 850–2,740 MB with mmap; 3.2 measures it without.
- **Gemma 4 is heavy on the CPU:** its E2B file is 3.1 GB and its process 4.3 GB, against Qwen3.5-2B's 1.3 and 2.0.

### The 8 GB laptop, approximated

The Small pipeline on 4 threads, with the chat model and the reranker loaded (each in its own server process, as in
every run here), inside a memory limit of 2,500 MB (`systemd-run --user --scope -p MemoryMax=2500M -p
MemorySwapMax=0`). **It was killed by the out-of-memory killer within 20 seconds**, twice. Measured without the limit:

| Process | Resident memory |
|---|---|
| Qwen3.5-2B, no mmap, context 2k, 1 / 2 / 4 slots | 1,510 / 1,588 / 1,735 MB |
| bge-reranker-v2-m3, with / without mmap | 790 / 664 MB |
| The Python driver | 83 MB |

Each server is a JVM of its own, so in the app (one JVM) the sum is smaller. A rough estimate for the app while it
answers: the editor (390 MB idle in 2.9) + the chat model (about 1.4–1.65 GB) + the reranker (about 0.55–0.6 GB) +
the embedding model for the question (about 330 MB, 2.2) **≈ 2.7–3.0 GB, over the 2.5 GB budget of 5.4**. Ways to fit
it, for 3.2 and 3.7 to measure in the app: load the reranker and the embedding model only for their step and free
them after (the reranker takes about 1.5 s to load, which adds to every answer), one slot on 8 GB, and no mmap
for every model. This approximation does not replace a real 8 GB laptop.

## Grammar-constrained output

Thirty questions of the 2.8 set, each turned into "two or three search queries" as JSON (`jsonSchemaToGrammar`
through `withJsonSchema`), on GPU.

| Model | Valid JSON | Tokens/s with / without grammar |
|---|---|---|
| Qwen3.5-2B | 30 / 30 | 146 / 216 |
| Gemma 4 E2B | 30 / 30 | 128 / 169 |
| granite-4.2-3b | 30 / 30 | 136 / 144 |
| Qwen3.5-4B | 30 / 30 | 104 / 109 (cpu12: 15 / 16) |
| Gemma 4 E4B | 30 / 30 | 87 / 97 |
| granite-4.2-8b | 30 / 30 | 70 / 71 |
| Qwen3.5-9B | 30 / 30 | 66 / 68 |

Every answer is valid JSON. The grammar costs 2–10 % of the speed, and 25–32 % on the two smallest models, where it is
a larger part of each step. Valid is not the same as right: Gemma 4 E4B wrote the queries for an English question
("What should I read to understand the compile-time pipeline?") in Portuguese. A grammar can still fail with an
exception (the Granite case above), so every structured call handles a `LlamaException` as a failed step.

## Reranker

The 80 questions of the 2.8 evaluation set (`smoke-data/eval/retrieval.json`), the top 20 chunks of the embedding
search reranked; recall@5 is whether an expected note is among the first five.

| Reranker | Recall@5 (0.9375 before) | Time per 20 chunks GPU / cpu12 / cpu4 | Memory | Video memory |
|---|---|---|---|---|
| **bge-reranker-v2-m3** | **0.975** | 217 / 648 / 1,136 ms | ~810 MB | 276 MB |
| Qwen3-Reranker-0.6B | 0.9625 | 258 / 1,773 / 4,107 ms | ~1,000 MB | 944 MB |

With the Qwen reranker the answers of Qwen3.5-4B were no better (0.95 answered, 0.76 quotes exact, against 0.95 and
0.81). **Decision: bge-reranker-v2-m3**, downloaded with every chat model (3.1).

**The reranker's score tells when the notes hold no answer.** The best bge score among the top 20 chunks, for the
80 questions of the set and for 8 questions no note answers (searched in both vaults, 16 cases):

| Threshold (score) | Questions kept (of 80) | Unanswerable rejected (of 16) |
|---|---|---|
| −6 | 76 | 14 |
| **−5** | **76** | **16** |
| −4 | 72 | 16 |
| 0 | 44 | 16 |

The unanswerable cases score −10.9 to −5.6; the answerable ones have a median of 0.6. Below −5, Notekea says "I
couldn't find this in your notes" without calling the chat model at all. The threshold comes from the same small set
it is measured on, so 3.4 checks it on its own questions.

## Picking actions

Thirty actions of a notes app (search, open, create, move, tag, trash, summarize…), 60 requests in Spanish and
English plus 6 that match no action, on GPU, as grammar-constrained JSON with the action names as an enum.

| Model | One step | Kind first, then action | Plan exact (12 plans, 2–5 steps) | Plan covers the steps | Next step from a result (10) | ms, one step |
|---|---|---|---|---|---|---|
| Qwen3.5-2B | 0.86 | 0.68 | 0.42 | 0.86 | 5 / 10 | 176 |
| Gemma 4 E2B | 0.91 | 0.64 | 0.75 | 0.91 | 9 / 10 | 329 |
| granite-4.2-3b | 0.65 | 0.64 | 0.42 | 0.89 | 5 / 10 | 226 |
| Qwen3.5-4B | **0.98** | 0.86 | **0.92** | 0.97 | 7 / 10 | 359 |
| Gemma 4 E4B | **0.98** | 0.58 | 0.75 | 1.00 | 9 / 10 | 496 |
| granite-4.2-8b | 0.88 | 0.76 | 0.50 | 0.98 | 5 / 10 | 423 |
| Qwen3.5-9B | **0.98** | 0.80 | **0.92** | 1.00 | 8 / 10 | 507 |

- **One step beats kind first for every model**: 3.9 picks the action in one step from the whole list.
- **Plans of up to 5 steps from Medium up** (Qwen3.5-4B and 9B: 11 of 12 exact). The Small model gets 5 of 12.
- **Choosing the next step from a result** (4.7) has only 10 cases; Gemma does best, Qwen3.5-9B gets 8. Agent mode is
  offered from Large only, until 4.7 measures it on a larger set. Plain coroutines are enough for these steps; Koog
  was not tried.

## Answer quality

### One call

One call per question with the top 5 reranked chunks, answering as JSON (`found`, `answer`, up to 3 quotes with their
source). "Answered" is `found`; "quotes exact" is every quote found word for word in its chunk; "right note" is a quote
from an expected note; "declined" is the 8 questions the notes cannot answer. GPU.

| Model | Valid | Answered | Quotes exact | Right note | Language | Declined | Median |
|---|---|---|---|---|---|---|---|
| Qwen3.5-2B | 0.97 | 0.80 | 0.65 | 0.71 | 0.92 | 7 / 8 | 602 ms |
| Gemma 4 E2B | 0.99 | 0.59 | 0.44 | 0.45 | 0.92 | 7 / 8 | 745 ms |
| granite-4.2-3b | 0.99 | 0.96 | 0.81 | 0.62 | 0.87 | 8 / 8 | 726 ms |
| Qwen3.5-4B | 1.00 | 0.95 | 0.81 | 0.88 | 0.95 | 8 / 8 | 1,245 ms |
| Gemma 4 E4B | 1.00 | 0.71 | 0.68 | 0.64 | 0.90 | 3 / 8 | 1,045 ms |
| granite-4.2-8b | 0.96 | 0.91 | 0.81 | 0.81 | 0.93 | 8 / 8 | 1,318 ms |
| Qwen3.5-9B | 0.99 | 0.95 | 0.85 | 0.90 | 0.99 | 8 / 8 | 2,222 ms |

A hand reading of 14 answers each from the three Qwen models found them mostly correct; Qwen3.5-2B once answered a
Spanish question in English and its answers are thinner. "Right note" understates a little: it counts only quotes
from the notes the set expects.

### The pipeline of `ARCHITECTURE.md` §12

Steps 2–5 of 3.5 on the top 5 reranked chunks: one extraction call per chunk in parallel ("copy word for word the
sentence that answers, or none", JSON), quotes kept only if they appear in the chunk, an answer written only from the
quotes, and one verification call per sentence in parallel ("do the quotes support this sentence?", JSON). The
rewrite step (1) was not part of it. 4 slots unless stated; context 2,048 a slot; "Extract" is the median time of
the extraction step.

| Model | Backend | Answered | Right note | Sentences supported¹ | Declined (of 8) | Median / 90th | Extract |
|---|---|---|---|---|---|---|---|
| Qwen3.5-2B | GPU | 1.00 | 0.90 | 0.73 | **0** | 2.1 / 2.5 s | 1.7 s |
| Gemma 4 E2B | GPU | 0.96 | 0.78 | 0.92 | 6 | 2.3 / 3.1 s | 1.9 s |
| granite-4.2-3b | GPU | 0.93 | 0.69 | 0.87 | 2 | 1.8 / 2.6 s | 0.9 s |
| Qwen3.5-4B | GPU | 1.00 | 0.90 | 0.79 | 7 | 3.0 / 4.0 s | 2.0 s |
| Gemma 4 E4B | GPU | 0.95 | 0.90 | 0.94 | 8 | 2.4 / 4.3 s | 1.4 s |
| granite-4.2-8b | GPU | 0.97 | 0.90 | 0.74 | 4 | 2.9 / 4.4 s | 1.4 s |
| Qwen3.5-9B | GPU | 0.99 | 0.89 | 0.81 | 8 | 3.2 / 4.4 s | 1.9 s |
| Qwen3.5-2B | cpu12, 1 slot | 1.00 | 0.91 | 0.81 | 0 | 9.9 / 13.0 s | 7.8 s |
| Qwen3.5-2B | cpu12, 4 slots | 1.00 | 0.91 | 0.86 | 1 | 6.6 / 8.5 s | 4.5 s |
| Qwen3.5-2B | cpu4, 1 slot | 1.00 | 0.91 | 0.83 | 0 | 10.5 / 13.9 s | 8.1 s |
| Qwen3.5-4B | cpu4, 1 slot | 1.00 | 0.91 | 0.80 | 8 | 19.8 / 30.0 s | 14.6 s |

¹ Judged by the same model that wrote the answer, so it compares a model with itself, not models with each other: a
lenient judge scores high. A hand reading of eight questions across Qwen3.5-2B, Qwen3.5-4B and Gemma 4 E4B shows the
judge is noisy (Qwen3.5-4B rejected its own correct "five minutes a side"), that Qwen3.5-2B once wrote its reasoning
into the answer ("Analyze the quotes…") even with reasoning off, and that Gemma 4 E4B's answers are short and well
quoted. The CPU runs of Qwen3.5-2B used a verification budget of 12 tokens, enough for Qwen (its GPU run gave 0.73
with 12 and with 32).

- **The pipeline reaches the right note far more often than one call:** Qwen3.5-2B goes from 0.71 to 0.90, Gemma 4
  E4B from 0.64 to 0.90 (here a kept quote from an expected note, close to but not the same measure as the single
  call's).
- **The Small model never declines by itself here:** some chunk always yields a quote, and Qwen3.5-2B answers from
  it. The reranker threshold above fixes that before the model runs; the answer step of 3.5 also keeps a `found` flag,
  which Qwen3.5-2B used correctly in the single call (7 of 8).
- **With that gate, a 2B model reaches what 3.4 is likely to ask** (right note 0.90, every unanswerable question
  declined), provisionally: Qwen3.5-2B is the default Small model.
- Gemma 4 E4B is the strongest Medium on the GPU, but heavy on the CPU (a 5 GB file, 7.2 GB of process, 16 tokens/s);
  3.4 compares it with Qwen3.5-4B with one fixed judge.

## Decisions

Preliminary until 3.4 measures them on its own set.

| | Default | File | Why |
|---|---|---|---|
| **Small** (8 GB) | Qwen3.5-2B Q4_K_M | 1.28 GB | Smallest process (1.5 GB without mmap), fastest, right note 0.90 with the pipeline |
| **Medium** (16 GB) | Qwen3.5-4B Q4_K_M | 2.74 GB | Best value: plans 11 of 12, actions 0.98, declines correctly; 3.1 GB on the CPU |
| **Large** (16 GB with a GPU, or 32 GB) | Qwen3.5-9B Q4_K_M | 5.68 GB | Best one-call answers (0.90 right note, 0.99 language), plans, 8 of 10 next steps |
| **Reranker** | bge-reranker-v2-m3 Q4_K_M | 0.44 GB | Recall@5 0.94 → 0.975, 0.2–1.1 s per 20 chunks, its score gates unanswerable questions |

Files for 3.1's catalog (Hugging Face repository, commit, SHA-256): `unsloth/Qwen3.5-2B-GGUF` f6d5376b `aaf42c8b7c3c…`,
`unsloth/Qwen3.5-4B-GGUF` e87f1764 `00fe7986ff5f…`, `unsloth/Qwen3.5-9B-GGUF` 3885219b `03b74727a860…`,
`gpustack/bge-reranker-v2-m3-GGUF` 3093af03 (file `bge-reranker-v2-m3-Q4_K_M.gguf`) `e186a244ed45…`.

Gemma 4 E4B stays a candidate for Medium on the GPU; granite-4.2 is dropped (weakest at actions and at choosing the
right note, and the grammar failure with a reasoning budget).

- **Slots by memory** (3.2 and 3.6): **1 on 8 GB**, 2 on 16 GB, 4 with a GPU that holds them; Deep answer writes as
  many drafts as slots. On the CPU the slots still run in parallel (4 slots: 6.6 instead of 9.9 s an answer with 12
  threads), so 3.5 does not need a separate sequential path; it only uses fewer slots.
- **Context:** 4,096 tokens a slot (the pipeline uses about 2k; 8k costs prompt time, not memory).
- **Latency budget for 3.5** (median / 90th percentile of a whole answer): **GPU 4 / 6 s**; **CPU, Small, 12 s /
  15 s**; Medium on a 4-thread CPU takes 20 / 30 s, so it is offered on the CPU only with a warning. Deep answer
  (3.6) gets twice the budget of its model and backend. The steps' share on the CPU: extraction is about 75 % of the
  time, so its prompt (one chunk) is kept short.
- **Gate before the model:** a best reranker score below −5 answers "I couldn't find this in your notes" directly.
- **Actions** (3.9): one step from the whole list; plans from Medium up; agent mode (4.7) from Large, decided again
  with 4.7's own cases. Plain coroutines, no Koog.
- **Loading** (3.1, 3.2): no mmap on the CPU; Vulkan natives jar added next to the CPU jar on Windows and Linux; check
  the size (file + every slot's KV cache + about 300 MB) against free video memory and RAM before loading, since an
  oversized load aborts the process; reasoning budget 0 for the models that reason, and the catalog says which.

## Not measured

- **Windows and macOS** (no machine); Metal, and Vulkan on Windows, are measured in 3.7.
- **CUDA** (the runtime was not installed; Vulkan needs nothing extra, so it is the route anyway).
- **A real 8 GB laptop:** approximated with a memory limit and 4 threads on this machine.
- **The 3.4 evaluation set** does not exist yet: quality here uses the 2.8 questions with provisional measures, so
  the defaults are preliminary until 3.4 confirms them.
- **Reasoning turned on**, which might help plans and agent mode at the cost of time.
