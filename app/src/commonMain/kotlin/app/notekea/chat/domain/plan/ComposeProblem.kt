package app.notekea.chat.domain.plan

/** Why a step's text could not be written, thrown while it is composed and said in the chat as its [problem]. */
internal class ComposeProblem(val problem: ActionProblem) : RuntimeException()
