package app.notekea.query.presentation

import app.notekea.core.notes.domain.RelativePath
import app.notekea.core.notes.domain.frontmatter.NoteProperty
import app.notekea.core.notes.domain.frontmatter.PropertyValue
import app.notekea.query.domain.QueryCatalog
import app.notekea.query.domain.QueryCondition
import app.notekea.query.domain.QueryFilter
import app.notekea.query.domain.QueryKey
import app.notekea.query.domain.QueryResult
import app.notekea.query.domain.QueryRow
import app.notekea.query.domain.QuerySort
import app.notekea.query.domain.QueryValueKind
import app.notekea.query.domain.VaultQuery

/** A few books for the Query tab's previews. */
internal object QueryPreviewData {
    val ratingFilter = QueryFilter(
        condition = QueryCondition.MORE_THAN,
        property = "rating",
        value = "3"
    )

    val state = QueryState(
        query = VaultQuery(
            filters = listOf(
                QueryFilter(
                    condition = QueryCondition.IN_FOLDER,
                    value = "Books"
                ),
                ratingFilter
            ),
            sort = QuerySort(
                property = "rating",
                isDescending = true
            ),
            columns = listOf("author", "rating")
        ),
        result = QueryResult(
            rows = listOf(
                row(
                    name = "Dune",
                    author = "Frank Herbert",
                    rating = "5"
                )
            ),
            columns = listOf("author", "rating"),
            total = 40
        ),
        catalog = QueryCatalog(
            keys = listOf(
                QueryKey(
                    name = "author",
                    kind = QueryValueKind.TEXT
                ),
                QueryKey(
                    name = "rating",
                    kind = QueryValueKind.NUMBER
                )
            ),
            folders = listOf("Books"),
            tags = listOf("book")
        ),
        isLoading = false
    )

    private fun row(
        name: String,
        author: String,
        rating: String
    ) = QueryRow(
        path = RelativePath(value = "Books/$name.md"),
        cells = listOf(
            NoteProperty(
                key = "author",
                value = PropertyValue.Text(text = author)
            ),
            NoteProperty(
                key = "rating",
                value = PropertyValue.Number(text = rating)
            )
        )
    )
}
