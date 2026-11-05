package app.notekea.query.domain

import kotlin.test.Test
import kotlin.test.assertEquals

class QueryYamlTest {
    private val yaml = QueryYaml()

    @Test
    fun `a query written as its block reads back the same`() {
        val query = VaultQuery(
            filters = listOf(
                QueryFilter(condition = QueryCondition.MORE_THAN, property = "rating", value = "3"),
                QueryFilter(condition = QueryCondition.IN_FOLDER, value = "Books"),
                QueryFilter(condition = QueryCondition.IS_EMPTY, property = "due"),
                QueryFilter(condition = QueryCondition.IS, property = "status", value = "on: hold")
            ),
            sort = QuerySort(property = "rating", isDescending = true),
            columns = listOf("author", "first, last")
        )

        assertEquals(expected = QueryParse.Valid(query = query), actual = yaml.read(text = yaml.write(query = query)))
    }

    @Test
    fun `the block reads as a person would write it, comments and quotes included`() {
        val text = """
            # Books I liked
            filters:
              - property: "rating"
                condition: more than
                value: 3

              - condition: has tag
                value: book
            sort:
              property: rating
            columns:
              - author
              - rating
        """.trimIndent()

        assertEquals(
            expected = QueryParse.Valid(
                query = VaultQuery(
                    filters = listOf(
                        QueryFilter(condition = QueryCondition.MORE_THAN, property = "rating", value = "3"),
                        QueryFilter(condition = QueryCondition.HAS_TAG, value = "book")
                    ),
                    sort = QuerySort(property = "rating"),
                    columns = listOf("author", "rating")
                )
            ),
            actual = yaml.read(text = text)
        )
    }

    @Test
    fun `an empty block is the query of every note`() {
        assertEquals(expected = QueryParse.Valid(query = VaultQuery()), actual = yaml.read(text = ""))
    }

    @Test
    fun `what cannot be read names its line and why`() {
        val cases = mapOf(
            "sorted: rating" to QueryParse.Invalid(line = 1, problem = QueryProblem.UNKNOWN_KEY),
            "not a query" to QueryParse.Invalid(line = 1, problem = QueryProblem.UNKNOWN_KEY),
            "columns: [a]\nfilters:\n  - condition: about\n    property: a\n    value: b" to
                QueryParse.Invalid(line = 3, problem = QueryProblem.UNKNOWN_CONDITION),
            "filters:\n  - property: rating\n    condition: more than" to
                QueryParse.Invalid(line = 2, problem = QueryProblem.MISSING_PART),
            "filters:\n  - condition: has tag\n    value: a\n  - condition: is\n    wrong: b" to
                QueryParse.Invalid(line = 5, problem = QueryProblem.UNKNOWN_KEY),
            "sort:\n  order: descending" to QueryParse.Invalid(line = 1, problem = QueryProblem.MISSING_PART),
            "sort:\n  property: a\n  by: b" to QueryParse.Invalid(line = 3, problem = QueryProblem.UNKNOWN_KEY),
            "columns: [a]\nnot a query" to QueryParse.Invalid(line = 1, problem = QueryProblem.UNKNOWN_KEY)
        )

        assertEquals(expected = cases.values.toList(), actual = cases.keys.map { yaml.read(text = it) })
    }
}
