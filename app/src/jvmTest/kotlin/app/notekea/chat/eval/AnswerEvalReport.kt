package app.notekea.chat.eval

import kotlin.math.roundToInt

/** The numbers of an evaluation run of [label] over [answered], as the tournament reported them. */
class AnswerEvalReport(
    private val label: String,
    private val answered: List<AnsweredQuestion>
) {
    private val answerable = answered.filterNot { it.question.isTrap }
    private val traps = answered.filter { it.question.isTrap }

    /** Strict right answers among the questions the notes answer. */
    val right: Double get() = share(
        part = answerable.count { it.isRight },
        of = answerable.size
    )

    val trapsDeclined: Double get() = share(
        part = traps.count { it.isRight },
        of = traps.size
    )

    fun text(): String {
        val written = answerable.filter {
            it.answer != null
        }

        val sample = answerable.filter {
            it.question.vault == SAMPLE_VAULT
        }

        val ghost = answerable.filter {
            it.question.vault != SAMPLE_VAULT
        }

        val spanish = answerable.filter { it.question.isSpanish }
        val firsts = answered.mapNotNull { it.firstMillis }.sorted()
        val totals = answered.map { it.totalMillis }.sorted()

        return listOf(
            "$label: ${answered.size} questions",
            "right ${pct(right)} (${answerable.count { it.isRight }} of ${answerable.size})",
            "  Sample Vault ${pct(rightOf(sample))}, Ghost Serializer ${pct(rightOf(ghost))}, Spanish ${pct(rightOf(spanish))}",
            "  declined although answerable ${pct(share(answerable.count { it.answer == null }, answerable.size))}",
            "traps declined ${pct(trapsDeclined)} (${traps.count { it.isRight }} of ${traps.size})",
            "cited right ${pct(share(answerable.count { it.isCitedRight }, answerable.count { it.isRight }))} of right",
            "pasted ${written.count { AnswerGrade.isPasted(it.answer.orEmpty(), it.read.map { r -> r.text }) }}, " +
                "other language ${written.count { !AnswerGrade.isInLanguage(it.question, it.answer.orEmpty()) }}, " +
                "failed ${answered.count { it.failure != null }}",
            "first words ms median ${percentile(firsts, MEDIAN)} p90 ${percentile(firsts, P90)}; " +
                "whole answer ms median ${percentile(totals, MEDIAN)} p90 ${percentile(totals, P90)}"
        ).joinToString(separator = "\n")
    }

    private fun rightOf(asked: List<AnsweredQuestion>): Double = share(
        part = asked.count { it.isRight },
        of = asked.size
    )

    private fun share(
        part: Int,
        of: Int
    ): Double = if (of == 0) 0.0 else part.toDouble() / of

    private fun pct(value: Double): String = "${(value * PERCENT).roundToInt()} %"

    private fun percentile(
        sorted: List<Long>,
        at: Double
    ): Long = if (sorted.isEmpty()) 0 else sorted[((sorted.size - 1) * at).roundToInt()]

    private companion object {
        const val SAMPLE_VAULT = "Sample Vault"
        const val MEDIAN = 0.5
        const val P90 = 0.9
        const val PERCENT = 100
    }
}
