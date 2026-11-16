package app.notekea.chat.domain.context

/** A [StepMemory] for tests: the parts holding most of the need's words come first, by plain word match. */
class FakeStepMemory : StepMemory {
    val kept = mutableMapOf<String, MutableMap<StepChunk, String>>()

    override suspend fun keep(
        space: String,
        entry: Int,
        chunks: List<String>
    ) {
        chunks.forEachIndexed { index, text ->
            kept
                .getOrPut(key = space) { mutableMapOf() }[
                StepChunk(
                    entry = entry,
                    chunk = index
                )
            ] = text
        }
    }

    override suspend fun ranked(
        space: String,
        need: String,
        entries: Set<Int>,
        most: Int
    ): List<StepChunk> {
        val words = need
            .lowercase()
            .split(Regex(pattern = "[^\\p{L}]+"))
            .filter {
                it.length >= 3
            }

        return kept[space]
            .orEmpty()
            .filterKeys {
                it.entry in entries
            }
            .mapValues { (_, text) ->
                words.count {
                    it in text.lowercase()
                }
            }
            .filterValues {
                it > 0
            }
            .entries
            .sortedByDescending { it.value }
            .map { it.key }
            .take(n = most)
    }

    override suspend fun forget(space: String) {
        kept.remove(key = space)
    }
}
