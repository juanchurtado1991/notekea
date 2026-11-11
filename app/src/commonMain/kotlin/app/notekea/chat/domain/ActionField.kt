package app.notekea.chat.domain

/** A field of a planned step (3.9), as the model names it ([written]) in the plan's JSON. */
enum class ActionField(val written: String) {
    NOTE(written = "note"),
    NOTES(written = "notes"),
    FOLDER(written = "folder"),
    TAG(written = "tag"),
    TITLE(written = "title"),
    TEXT(written = "text"),
    TEMPLATE(written = "template"),
    PROPERTY(written = "property"),
    VALUE(written = "value"),
    TARGET(written = "target"),
    DUE(written = "due"),
    PERIOD(written = "period"),
    PANEL(written = "panel"),
    THEME(written = "theme"),
    REQUEST(written = "request"),
    URL(written = "url")
}
