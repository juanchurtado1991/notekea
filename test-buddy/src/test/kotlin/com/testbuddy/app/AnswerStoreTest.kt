package com.testbuddy.app

import com.testbuddy.ManualAnswer
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

class AnswerStoreTest {
    private val file = Files.createTempDirectory("answers").resolve("work/answers.txt")

    @Test
    fun `answers survive with tabs, line breaks and backslashes in a note`() {
        val answers = listOf(
            ManualAnswer.Passed(id = "1"),
            ManualAnswer.Failed(id = "2", note = "a\ttab, a\nbreak and C:\\path", screenshot = "2.png"),
            ManualAnswer.Failed(id = "3", note = "no picture"),
            ManualAnswer.NotRun(id = "4")
        )
        AnswerStore(file = file).save(answers = answers)
        assertEquals(answers.associateBy { it.id }, AnswerStore(file = file).load())
    }

    @Test
    fun `no file and unreadable lines give no answers`() {
        assertEquals(emptyMap(), AnswerStore(file = file).load())
        Files.createDirectories(file.parent)
        file.writeText("\n7\tunknown\n")
        assertEquals(emptyMap(), AnswerStore(file = file).load())
    }
}
