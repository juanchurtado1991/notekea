package app.notekea.chat.eval.blind

/**
 * The blind pool (`docs/eval-blind.md`): 50 exams written and checked blind on 2026-11-17. Ten are sealed and never
 * measured until the final number; a run measures ten of the others picked at random, and their failures are read
 * only by kind.
 */
internal object BlindPool {
    val all = BlindPoolFirst.all + BlindPoolSecond.all

    /** The 40 exams a round samples from, measured whole once as the reference (`docs/loop-goal-plan.md`). */
    val unsealed: List<BlindExam> get() = all.filterNot { it.isSealed }

    /** Ten of the unsealed exams, the same ten for the same [seed]. */
    fun sampleOf(seed: Long): List<BlindExam> = unsealed
        .shuffled(random = kotlin.random.Random(seed = seed))
        .take(n = SAMPLE)

    val sealed: List<BlindExam> get() = all.filter { it.isSealed }

    private const val SAMPLE = 10
}
