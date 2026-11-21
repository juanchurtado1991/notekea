package app.notekea.chat.eval

/**
 * The report of the frame alone (`docs/loop-goal-plan.md`, step 1.2): the four counts, each by what settled the
 * frame, and every miss and false alarm with its frame line and the frame's calls.
 */
internal object FrameEvalReport {
    fun of(
        title: String,
        verdicts: List<FrameVerdict>
    ): String = buildString {
        val asks = verdicts.count { it.mustAsk }
        val others = verdicts.size - asks

        val misses = verdicts.count {
            it.kind == FrameVerdict.Kind.MISS
        }

        val alarms = verdicts.count {
            it.kind == FrameVerdict.Kind.FALSE_ALARM
        }

        append("# $title\n\n")
        append("$misses misses of $asks tasks that must ask (${percent(misses, asks)} %), ")
        append("$alarms false alarms of $others that must not (${percent(alarms, others)} %)\n\n")

        FrameVerdict.Kind.entries.forEach { kind ->
            val of = verdicts.filter {
                it.kind == kind
            }

            val by = of
                .groupingBy { reasonOf(verdict = it) }
                .eachCount()
                .entries
                .sortedByDescending { it.value }
                .joinToString { "${it.key} ${it.value}" }

            append("- ${of.size} ${kind.written}${if (by.isEmpty()) "" else ": $by"}\n")
        }

        verdicts
            .filter {
                it.kind == FrameVerdict.Kind.MISS || it.kind == FrameVerdict.Kind.FALSE_ALARM
            }
            .forEach { append(shown(verdict = it)) }
    }

    private fun shown(verdict: FrameVerdict): String = buildString {
        append("\n## ${verdict.kind.written}: ${verdict.task}\n\n")
        append("Frame: ${verdict.frame?.let { EvalTraceReport.frameOf(frame = it) } ?: "none"}.\n\n")

        verdict
            .calls
            .filter {
                it.role == TracedCall.Role.FRAME || it.role == TracedCall.Role.ROUTE
            }
            .forEach {
                val written = it.written.replace(
                    oldValue = "\n",
                    newValue = " "
                )

                append("- ${it.role.name.lowercase()}: $written\n")
            }
    }

    private fun reasonOf(verdict: FrameVerdict): String = verdict
        .frame
        ?.reason
        ?.name
        ?.lowercase() ?: "no frame"

    private fun percent(
        part: Int,
        whole: Int
    ): Int = if (whole == 0) 0 else part * PERCENT / whole

    private const val PERCENT = 100
}
