package app.notekea.chat.eval.blind

import app.notekea.chat.eval.AgentWideCase
import app.notekea.chat.eval.AgentWideCase.End
import app.notekea.chat.eval.AgentWideCase.Kind

/** A blind task the loop answers: [holds] and [computed] must be in the answer, [lacks] not. */
internal fun answer(
    task: String,
    kind: Kind,
    tools: List<String>,
    holds: List<String> = emptyList(),
    computed: List<String> = emptyList(),
    lacks: List<String> = emptyList()
) = AgentWideCase(task, kind, tools, End.ANSWER, holds, computed, lacks)

/** A blind task the loop ends with a proposal of changes. */
internal fun change(
    task: String,
    kind: Kind,
    tools: List<String>,
    holds: List<String> = emptyList(),
    computed: List<String> = emptyList(),
    lacks: List<String> = emptyList()
) = AgentWideCase(task, kind, tools, End.PROPOSE, holds, computed, lacks)

/** A blind task that can be read two ways: the loop asks, and its question holds [holds]. */
internal fun ask(
    task: String,
    kind: Kind,
    tools: List<String>,
    holds: List<String> = emptyList(),
    computed: List<String> = emptyList(),
    lacks: List<String> = emptyList()
) = AgentWideCase(task, kind, tools, End.ASK, holds, computed, lacks)

/** An exam: its [vault] and its cases, in the order of [parts], as the logged runs measured them. */
internal fun exam(
    id: String,
    vault: String,
    vararg parts: List<AgentWideCase>,
    isSealed: Boolean = false
) = BlindExam(
    id = id,
    vault = vault,
    cases = parts.toList().flatten(),
    isSealed = isSealed
)
