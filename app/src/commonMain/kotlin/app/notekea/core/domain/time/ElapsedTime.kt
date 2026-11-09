package app.notekea.core.domain.time

/** How long ago something happened, in the largest whole unit people use in conversation ("3 days ago"). */
sealed interface ElapsedTime {
    data class Days(val count: Int) : ElapsedTime

    data class Hours(val count: Int) : ElapsedTime

    data object JustNow : ElapsedTime

    data class Minutes(val count: Int) : ElapsedTime

    data class Months(val count: Int) : ElapsedTime

    data class Weeks(val count: Int) : ElapsedTime

    data class Years(val count: Int) : ElapsedTime
}
