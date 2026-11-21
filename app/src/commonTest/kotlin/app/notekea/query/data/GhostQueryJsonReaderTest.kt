package app.notekea.query.data

import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.VaultQuery
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GhostQueryJsonReaderTest {
    private val reader = GhostQueryJsonReader()

    // Leaving out a filter it cannot read would run a wider query than the one asked for.
    @Test
    fun `a filter with an unknown condition fails the query`() {
        val json = """{"filters":[{"property":"rating","condition":"more than","value":"3"},""" +
            """{"property":"status","condition":"is not","value":"done"}]}"""

        assertNull(actual = reader.read(json = json))
    }

    @Test
    fun `conditions are read whatever their case and spacing`() {
        val json = """{"filters":[{"property":"due","condition":"Is  Empty","value":""}]}"""

        assertEquals(
            expected = VaultQuery(
                filters = listOf(
                    QueryFilter(
                        condition = QueryCondition.IS_EMPTY,
                        property = "due"
                    )
                )
            ),
            actual = reader.read(json = json)
        )
    }
}
