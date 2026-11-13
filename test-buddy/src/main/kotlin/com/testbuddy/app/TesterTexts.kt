package com.testbuddy.app

import com.testbuddy.ManualReason
import com.testbuddy.TesterLanguage

/** The manager's own words, in the suite's [TesterLanguage]; read through [LocalTesterTexts]. */
internal class TesterTexts(
    val tabAutomatic: String,
    val tabManual: String,
    val tabReport: String,
    val noManualCases: String,
    val testDataLine: (data: String) -> String,
    val passedMark: String,
    val failedMark: String,
    val caseOf: (index: Int, count: Int) -> String,
    val previousCase: String,
    val skipCase: String,
    val stepOf: (step: Int, count: Int) -> String,
    val restartApp: (appName: String) -> String,
    val back: String,
    val done: String,
    val doneNextStep: String,
    val reasonEyes: String,
    val reasonSystem: String,
    val reasonPackaged: String,
    val reasonCostly: String,
    val passed: String,
    val failed: String,
    val saveAndNext: String,
    val backToSteps: String,
    val shouldHaveSeen: String,
    val whatDidYouSee: String,
    val screenshotAdded: String,
    val addScreenshot: String,
    val lastStepsAdded: String,
    val testData: String,
    val freshCopyOf: (name: String) -> String,
    val openDataHint: (appName: String) -> String,
    val skippedNeed: (count: Int, missing: String) -> String,
    val copied: String,
    val copyReport: String,
    val openReportFolder: String,
    val sendReport: String,
    val sendReportHint: String,
    val automaticTests: String,
    val automaticIntro: (total: Int) -> String,
    val startAutomatic: String,
    val keepUsing: String,
    val stop: String,
    val runningTests: String,
    val doneOf: (done: Int, total: Int) -> String,
    val now: String,
    val starting: String,
    val recent: String,
    val runAgain: String,
    val nextManual: String,
    val allPassed: String,
    val automaticDone: String,
    val failuresInReport: String,
    val passedCount: (count: Int) -> String,
    val failedCount: (count: Int) -> String,
    val toGo: (count: Int) -> String,
    val minutesLeft: (minutes: Long) -> String
) {
    fun hint(reason: ManualReason): String = when (reason) {
        ManualReason.EYES -> reasonEyes
        ManualReason.SYSTEM -> reasonSystem
        ManualReason.PACKAGED -> reasonPackaged
        ManualReason.COSTLY -> reasonCostly
    }

    companion object {
        fun of(language: TesterLanguage): TesterTexts = when (language) {
            TesterLanguage.ENGLISH -> English
            TesterLanguage.SPANISH -> Spanish
        }

        val English = TesterTexts(
            tabAutomatic = "Automatic",
            tabManual = "Manual",
            tabReport = "Report",
            noManualCases = "There are no manual cases.",
            testDataLine = { "Test data: a fresh copy of $it" },
            passedMark = " · passed",
            failedMark = " · failed",
            caseOf = { index, count -> "Case $index of $count" },
            previousCase = "‹ Previous case",
            skipCase = "Skip this case ›",
            stepOf = { step, count -> "STEP $step OF $count" },
            restartApp = { "Restart $it for this case" },
            back = "‹ Back",
            done = "Done ›",
            doneNextStep = "Done, next step ›",
            reasonEyes = "Needs your eyes",
            reasonSystem = "Uses your system",
            reasonPackaged = "Uses the installed app",
            reasonCostly = "Checked by hand",
            passed = "Passed",
            failed = "Failed",
            saveAndNext = "Save and go to the next case",
            backToSteps = "‹ Back to the steps",
            shouldHaveSeen = "YOU SHOULD HAVE SEEN",
            whatDidYouSee = "What did you see?",
            screenshotAdded = "Screenshot added",
            addScreenshot = "Add a screenshot",
            lastStepsAdded = "Your last steps in the app are added for you.",
            testData = "TEST DATA",
            freshCopyOf = { "This case uses a fresh copy of $it:" },
            openDataHint = {
                "Open that folder in $it. Each case starts from a new copy, so what you changed in an earlier " +
                    "case is gone."
            },
            skippedNeed = { count, missing -> "$count skipped: they need $missing." },
            copied = "Copied",
            copyReport = "Copy report",
            openReportFolder = "Open the Report folder",
            sendReport = "Send the report",
            sendReportHint =
            "Copy it into a message to whoever asked you to test, with the pictures of the Report folder.",
            automaticTests = "Automatic tests",
            automaticIntro = {
                "$it cases check the app on their own, on copies of the test data, out of sight. It takes a while " +
                    "and you can keep using the computer."
            },
            startAutomatic = "Start the automatic tests",
            keepUsing = "You can keep using the computer.",
            stop = "Stop",
            runningTests = "Running tests",
            doneOf = { done, total -> "$done of $total" },
            now = "NOW",
            starting = "Starting…",
            recent = "RECENT",
            runAgain = "Run again",
            nextManual = "Next: manual cases ›",
            allPassed = "All automatic tests passed",
            automaticDone = "Automatic tests done",
            failuresInReport = "The failures go in the report: there is nothing to do about them now.",
            passedCount = { "✓ $it passed" },
            failedCount = { "✕ $it failed" },
            toGo = { "$it to go" },
            minutesLeft = { " · about $it minute${if (it == 1L) "" else "s"} left" }
        )

        val Spanish = TesterTexts(
            tabAutomatic = "Automáticos",
            tabManual = "Manuales",
            tabReport = "Reporte",
            noManualCases = "No hay casos manuales.",
            testDataLine = { "Datos de prueba: una copia nueva de $it" },
            passedMark = " · pasó",
            failedMark = " · falló",
            caseOf = { index, count -> "Caso $index de $count" },
            previousCase = "‹ Caso anterior",
            skipCase = "Saltar este caso ›",
            stepOf = { step, count -> "PASO $step DE $count" },
            restartApp = { "Reiniciar $it para este caso" },
            back = "‹ Atrás",
            done = "Listo ›",
            doneNextStep = "Listo, siguiente paso ›",
            reasonEyes = "Necesita tus ojos",
            reasonSystem = "Usa tu sistema",
            reasonPackaged = "Usa la app instalada",
            reasonCostly = "Se revisa a mano",
            passed = "Pasó",
            failed = "Falló",
            saveAndNext = "Guardar e ir al siguiente caso",
            backToSteps = "‹ Volver a los pasos",
            shouldHaveSeen = "DEBERÍAS HABER VISTO",
            whatDidYouSee = "¿Qué viste?",
            screenshotAdded = "Captura agregada",
            addScreenshot = "Agregar una captura",
            lastStepsAdded = "Tus últimos pasos en la app se agregan solos.",
            testData = "DATOS DE PRUEBA",
            freshCopyOf = { "Este caso usa una copia nueva de $it:" },
            openDataHint = {
                "Abre esa carpeta en $it. Cada caso empieza con una copia nueva, así que lo que cambiaste en un " +
                    "caso anterior ya no está."
            },
            skippedNeed = { count, missing -> "$count omitidos: necesitan $missing." },
            copied = "Copiado",
            copyReport = "Copiar el reporte",
            openReportFolder = "Abrir la carpeta Report",
            sendReport = "Enviar el reporte",
            sendReportHint =
            "Cópialo en un mensaje para quien te pidió probar, con las imágenes de la carpeta Report.",
            automaticTests = "Pruebas automáticas",
            automaticIntro = {
                "$it casos revisan la app solos, con copias de los datos de prueba, sin que se vean. Tarda un rato " +
                    "y puedes seguir usando la computadora."
            },
            startAutomatic = "Empezar las pruebas automáticas",
            keepUsing = "Puedes seguir usando la computadora.",
            stop = "Detener",
            runningTests = "Corriendo pruebas",
            doneOf = { done, total -> "$done de $total" },
            now = "AHORA",
            starting = "Empezando…",
            recent = "RECIENTES",
            runAgain = "Correr de nuevo",
            nextManual = "Siguiente: casos manuales ›",
            allPassed = "Todas las pruebas automáticas pasaron",
            automaticDone = "Pruebas automáticas terminadas",
            failuresInReport = "Los fallos van al reporte: no tienes que hacer nada con ellos ahora.",
            passedCount = { "✓ $it pasaron" },
            failedCount = { "✕ $it fallaron" },
            toGo = { "faltan $it" },
            minutesLeft = { " · faltan unos $it minuto${if (it == 1L) "" else "s"}" }
        )
    }
}
