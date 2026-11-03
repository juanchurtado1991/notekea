package app.notekea.notes.data

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.LowerCaseFilter
import org.apache.lucene.analysis.es.SpanishLightStemFilter
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter
import org.apache.lucene.analysis.standard.StandardTokenizer

/**
 * How the search index reads the words of notes and questions (`ROADMAP.md` 2.3): split as Unicode words, lower case,
 * without accents ("canción" and "cancion" are one word), and Spanish words reduced to their stem ("recetas" and
 * "receta" are one). Notes and questions go through the same steps, so English words match each other as well.
 */
class NoteAnalyzer : Analyzer() {
    override fun createComponents(
        fieldName: String
    ): TokenStreamComponents {
        val tokenizer = StandardTokenizer()
        return TokenStreamComponents(tokenizer, SpanishLightStemFilter(ASCIIFoldingFilter(LowerCaseFilter(tokenizer))))
    }
}
