package app.notekea.core.notes.data

import java.sql.SQLException

internal actual fun isDatabaseFailure(exception: Exception): Boolean = exception is SQLException
