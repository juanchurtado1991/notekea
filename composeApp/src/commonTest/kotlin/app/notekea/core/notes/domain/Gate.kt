package app.notekea.core.notes.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.yield

/**
 * What a fake of the disk goes through on every call, as the real adapter switches to the IO dispatcher: [pass] always
 * suspends once, so a caller that was cancelled stops there, and it waits while a test holds the gate ([close]) until
 * [open], to act in the middle of a call. Open at first.
 */
class Gate {
    private val isOpen = MutableStateFlow(value = true)

    fun close() {
        isOpen.value = false
    }

    fun open() {
        isOpen.value = true
    }

    suspend fun pass() {
        yield()
        isOpen.first { it }
    }
}
