package app.notekea.core.data

import app.notekea.core.domain.LocalTimeFormatter
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.time.Instant
import kotlin.time.toJavaInstant

/** [LocalTimeFormatter] with `java.time`, in the computer's time zone. */
class JvmLocalTimeFormatter : LocalTimeFormatter {
    private val formatter = DateTimeFormatter.ofPattern(COMPACT_PATTERN)

    override fun compact(
        instant: Instant
    ): String = formatter.format(instant.toJavaInstant().atZone(ZoneId.systemDefault()))

    companion object {
        private const val COMPACT_PATTERN = "yyyyMMddHHmmss"
    }
}
