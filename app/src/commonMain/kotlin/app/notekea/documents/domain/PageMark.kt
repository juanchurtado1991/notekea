package app.notekea.documents.domain

/** Where a passage lies on a page, as fractions of the page's width and height, so it marks any rendered size. */
data class PageMark(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)
