package app.notekea.clips.data

import app.notekea.clips.domain.FetchedPage
import app.notekea.core.domain.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A clipped article keeps its shape as Markdown (the developer's request of 2026-11-13, clips that read well): it
 * was plain paragraphs, its headings, lists, code and tables lost, and a site's menu and buttons among them.
 */
class HtmlMarkdownTest {
    private val filler =
        "This tutorial explains in full sentences how a theme is made from its first file to its last. "

    private val html = """
        <html><head><title>Make a theme</title></head><body>
        <nav><a href="/">Home</a><a href="/docs">Docs</a></nav>
        <main>
          <h1>Make a theme</h1>
          <div class="breadcrumbs"><a href="/docs">Docs</a></div>
          <p>A <strong>theme</strong> is made of <em>files</em>, see <a href="/docs/files">the files</a>.</p>
          <p>${filler.repeat(n = 6)}</p>
          <h2>Steps</h2>
          <ol><li>Create <code>style.css</code><ul><li>Add a header</li></ul></li><li>Activate it</li></ol>
          <pre><code class="language-php"><span class="line">&lt;?php</span><span class="line">get_header();</span></code></pre>
          <table><tr><th>File</th><th>Use</th></tr><tr><td>style.css</td><td>Styles</td></tr></table>
          <blockquote><p>Keep it small.</p></blockquote>
          <div style="display: none">Hidden words</div>
          <button>Copy</button>
        </main>
        <footer>Footer words</footer>
        </body></html>
    """.trimIndent()

    @Test
    fun `headings, lists, code, a table, a quote and links are written as Markdown, the page's chrome left out`() {
        val article = ReadabilityArticleExtractor().extract(
            page = FetchedPage(
                url = "https://x.com/docs/theme",
                html = html
            )
        ) as Outcome.Success

        assertEquals(
            expected = listOf(
                "A **theme** is made of *files*, see [the files](https://x.com/docs/files).",
                filler.repeat(n = 6).trim(),
                "### Steps",
                "1. Create `style.css`\n   - Add a header\n2. Activate it",
                "```php\n<?php\nget_header();\n```",
                "| File | Use |\n| --- | --- |\n| style.css | Styles |",
                "> Keep it small."
            ),
            actual = article.value.paragraphs
        )
    }
}
