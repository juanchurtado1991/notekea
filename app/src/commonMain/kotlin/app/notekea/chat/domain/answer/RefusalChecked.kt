package app.notekea.chat.domain.answer

import app.notekea.chat.domain.plan.ActionField
import app.notekea.chat.domain.plan.ActionKind
import app.notekea.chat.domain.plan.PlannedStep

/**
 * A refusal is kept only for what Notekea cannot do, named in the request: an email, a reminder, the weather, the web,
 * deleting for good, Settings, the model, code or the system prompt. The chat probe of 2026-11-11 had the model refuse
 * follow-ups and moves with reasons it made up ("cannot write arbitrary text"); those are planned again.
 */
internal object RefusalChecked {
    private val CANNOT = Regex(
        pattern = "(e-?mail|correo|mail|mensaje|message|whatsapp|sms|remind|recordatorio|recu[eé]rdame|alarm|alarma|" +
            "notif|calendar|calendario|agenda|weather|clima|el tiempo|pron[oó]stico|news|noticias|web|internet|" +
            "google|online|en l[ií]nea|for good|permanent|para siempre|definitiv|todas mis notas|all my notes|" +
            "settings|configuraci[oó]n|ajustes|modelo|model|c[oó]digo|code|script|system prompt|instruc|" +
            "ignore|ignora|password|passwd|contraseña|hack|outside the vault|fuera de la b[oó]veda|/etc|c:\\\\|~/|" +
            "system file|archivo del sistema|desktop|escritorio|downloads|descargas)",
        option = RegexOption.IGNORE_CASE
    )

    /** Whether [steps] refuse a request that names nothing Notekea cannot do. */
    fun isUnfounded(
        steps: List<PlannedStep>,
        request: String
    ): Boolean {
        val isRefused = steps.isNotEmpty() && steps.all { it.kind == ActionKind.REFUSE }

        return isRefused && !CANNOT.containsMatchIn(input = request)
    }

    // A change asked for is no question: refused twice, the refusal is said, never an answer that it was done (the
    // chat probe, 2026-11-11: "delete the note Pancakes" answered "The note has been deleted").
    private val CHANGE = Regex(
        pattern = "^\\W*(please\\s+)?(delete|remove|borr\\w*|elimin\\w*|move|mov[eé]\\w*|tag|etiquet\\w*|rename|" +
            "renombr\\w*|create|cre[aá]\\w*|add|añad\\w*|agreg\\w*|pon\\w*|set|mark|marc[aá]\\w*|bookmark)\\b",
        option = RegexOption.IGNORE_CASE
    )

    /** Whether [request] asks for a change, which a second refusal keeps rather than answering. */
    fun isChange(request: String): Boolean = CHANGE.containsMatchIn(input = request)

    /** What an unfounded refusal becomes when planning again refuses too: the request answered from the notes. */
    fun answered(request: String): List<PlannedStep> = listOf(
        PlannedStep(
            kind = ActionKind.ANSWER,
            fields = mapOf(ActionField.REQUEST to request)
        )
    )
}
