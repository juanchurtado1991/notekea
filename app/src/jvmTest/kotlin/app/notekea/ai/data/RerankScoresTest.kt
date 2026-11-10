package app.notekea.ai.data

import kotlin.test.Test
import kotlin.test.assertEquals

class RerankScoresTest {
    @Test
    fun `scores come back in the order of the documents, whatever the reply's order`() {
        val json = """[{"document":"b","index":1,"score":-2.5,"tokens_evaluated":9},""" +
            """{"document":"a","index":0,"score":3.25e0,"tokens_evaluated":8}]"""

        assertEquals(
            expected = listOf(3.25f, -2.5f),
            actual = RerankScores.parse(
                json = json,
                count = 2
            )
        )
    }

    @Test
    fun `a field name written inside a document is not taken for one, and a missing document scores lowest`() {
        val json = """[{"document":"say \"index\": 7 and \"score\": 99","index":0,"score":1.5}]"""

        assertEquals(
            expected = listOf(1.5f, Float.NEGATIVE_INFINITY),
            actual = RerankScores.parse(
                json = json,
                count = 2
            )
        )
    }
}
