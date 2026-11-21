package app.notekea.query.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/** A query block typed by hand: keywords in any case, comments after values, a sort on one line. */
class QueryYamlHandWrittenTest {
    private val yaml = QueryYaml()

    @Test
    fun `a comment after a value inside filters and sort is left out`() {
        val text = """
            filters:
              - property: rating # out of five
                condition: more than # strictly
                value: 3 # stars
              - property: title
                condition: is
                value: "C# notes"
            sort:
              property: rating
              order: descending # best first
        """.trimIndent()

        assertEquals(
            expected = QueryParse.Valid(
                query = VaultQuery(
                    filters = listOf(
                        QueryFilter(
                            condition = QueryCondition.MORE_THAN,
                            property = "rating",
                            value = "3"
                        ),
                        QueryFilter(
                            condition = QueryCondition.IS,
                            property = "title",
                            value = "C# notes"
                        )
                    ),
                    sort = QuerySort(
                        property = "rating",
                        isDescending = true
                    )
                )
            ),
            actual = yaml.read(text = text)
        )
    }

    @Test
    fun `keys, conditions and orders are read whatever their case and spacing`() {
        val text = """
            Filters:
              - Property: Due
                Condition: Is  Empty
            SORT:
              Property: Rating
              Order: Descending
        """.trimIndent()

        assertEquals(
            expected = QueryParse.Valid(
                query = VaultQuery(
                    filters = listOf(
                        QueryFilter(
                            condition = QueryCondition.IS_EMPTY,
                            property = "Due"
                        )
                    ),
                    sort = QuerySort(
                        property = "Rating",
                        isDescending = true
                    )
                )
            ),
            actual = yaml.read(text = text)
        )
    }

    @Test
    fun `a sort written on its key's line is by that property, ascending`() {
        assertEquals(
            expected = QueryParse.Valid(query = VaultQuery(sort = QuerySort(property = "rating"))),
            actual = yaml.read(text = "sort: rating # lowest first")
        )

        assertEquals(
            expected = QueryParse.Invalid(
                line = 2,
                problem = QueryProblem.UNKNOWN_KEY
            ),
            actual = yaml.read(text = "sort: rating\n  order: descending")
        )
    }
}
