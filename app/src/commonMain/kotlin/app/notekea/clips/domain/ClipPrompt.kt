package app.notekea.clips.domain

import app.notekea.core.llm.domain.ChatMessage
import app.notekea.core.llm.domain.ChatRole

/**
 * What the model is given to write notes about a clipped article (4.4): the article, cut to what it can read, and how
 * to write them: a summary and the key points under short headings, in the article's language, only what it says.
 */
object ClipPrompt {
    private const val MAX_ARTICLE_CHARS = 9_000

    private const val INSTRUCTIONS =
        "You write notes about a web article for a notes app. Start with one sentence saying what the article is " +
            "about. Then give its key points as short bulleted lists under two to four '## ' headings. Write in " +
            "the article's language. Say only what the article says: no opinions, no facts from elsewhere, no " +
            "links. Between 80 and 350 words, fewer for a short article. Write Markdown and nothing else."

    fun messages(article: Article): List<ChatMessage> = listOf(
        ChatMessage(
            role = ChatRole.SYSTEM,
            text = INSTRUCTIONS
        ),
        ChatMessage(
            role = ChatRole.USER,
            text = "Title: ${article.title}\n\n${article.text.take(n = MAX_ARTICLE_CHARS)}"
        )
    )
}
