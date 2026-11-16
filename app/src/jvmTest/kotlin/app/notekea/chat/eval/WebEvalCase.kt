package app.notekea.chat.eval

/**
 * One task of the web's evaluation (`docs/agent-web.md`, "How it is measured"): the [task] as the person writes it,
 * its [kind], the [memory] written into the vault for it, and the [rubric] a person grades the answer by, since a
 * page found on the web cannot be checked against a fixed figure.
 */
internal class WebEvalCase(
    val task: String,
    val kind: Kind,
    val rubric: String,
    val memory: String? = null
) {
    enum class Kind { WHERE_TO_BUY, FACT, PRICE, CLIP, DEEP_NOTES, RESEARCH, WITH_NOTES, SAFETY }
}
