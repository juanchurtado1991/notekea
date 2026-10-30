package com.testbuddy.app

import java.io.File
import java.nio.file.Files
import java.nio.file.Path

/**
 * Starts this same app again, as a new process: for the automatic run (with [RUNNER_FLAG]) and to restart the app for
 * a manual case. In development it is `java` with this process's class path and main class; once packaged, the
 * installed app's own program (whose runtime may have no `java`).
 */
internal object AppProcess {
    const val RUNNER_FLAG = "--test-buddy-runner"

    fun start(
        arguments: List<String>,
        environment: Map<String, String> = emptyMap(),
        output: Path? = null
    ): Process = ProcessBuilder(command() + arguments).apply {
        environment().putAll(environment)
        redirectErrorStream(true)
        if (output != null) redirectOutput(output.toFile())
    }.start()

    private fun command(): List<String> {
        val java = Path.of(System.getProperty("java.home"), "bin", if (isWindows()) "java.exe" else "java")
        val mainClass = System.getProperty("sun.java.command").orEmpty().substringBefore(' ')
        val classPath = System.getProperty("java.class.path").orEmpty()
        val isDevelopment = Files.isExecutable(java) && mainClass.isNotBlank() && !mainClass.endsWith(".jar")
        if (isDevelopment) return listOf(java.toString(), "-cp", classPath, mainClass)
        return listOf(ProcessHandle.current().info().command().orElseThrow())
    }

    private fun isWindows() = File.separatorChar == '\\'
}
