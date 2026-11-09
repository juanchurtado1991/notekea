package app.notekea.tasks.domain

/** A group of tasks by date, in the order they are shown; [DONE] holds the done ones, whatever their date. */
enum class TaskWhen {
    OVERDUE,
    TODAY,
    THIS_WEEK,
    LATER,
    NO_DATE,
    DONE
}
