package app.notekea.search.eval

/** How a vault's questions did: of those [asked], the ones [missed]. */
data class RetrievalMeasure(
    val vault: String,
    val asked: Int,
    val missed: List<RetrievalQuestion>
) {
    val recall: Double get() = (asked - missed.size).toDouble() / asked

    fun report(): String {
        val found = asked - missed.size
        return (listOf("$vault: recall@5 $recall ($found of $asked)") + missed.map { it.question })
            .joinToString(separator = "\n- ")
    }
}
