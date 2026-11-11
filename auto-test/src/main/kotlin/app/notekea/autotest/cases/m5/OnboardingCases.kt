package app.notekea.autotest.cases.m5

import app.notekea.autotest.notekea
import app.notekea.autotest.relaunch
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val WELCOME_TITLE = "Welcome to Notekea"

/**
 * The cases of 5.1, the first-run welcome (`MVP_SMOKE_TEST.md`, the developer's design of 2026-11-11): its steps with
 * the sample vault, automatic; its look and the AI download, by eye.
 */
fun SuiteBuilder.onboardingCases() {
    welcomeWithSample()
    welcomeLook()
}

private fun SuiteBuilder.welcomeWithSample() {
    automated(
        id = "5.1-01",
        title = "The first-run welcome opens the sample vault, then offers the AI, and is not shown again"
    ) {
        notekea().setWelcomeSeen(isSeen = false)
        relaunch()
        expectText(text = WELCOME_TITLE)
        click(text = "Continue")
        expectText(text = "Choose your notes")
        click(text = "Try the sample vault")
        expectText(text = "Get the AI")
        click(text = "Not now")
        expectNoText(text = "Get the AI")

        expectText(
            text = "Notekea Sample Vault",
            substring = true
        )

        relaunch()
        expectNoText(text = WELCOME_TITLE)
    }
}

private fun SuiteBuilder.welcomeLook() {
    manual(
        id = "5.1-02",
        reason = ManualReason.EYES
    ) {
        title(
            text =
            "La bienvenida del primer inicio se ve como la de Apple, y Descargar y empezar " +
                "descarga el modelo recomendado"
        )

        steps(
            text =
            "1. Instala Notekea QA desde cero (o Ajustes → General → Bienvenida → Mostrar de " +
                "nuevo, y luego sal y vuelve a iniciarlo).\n2. Mira cada paso; pulsa Esc en el " +
                "primero y luego empieza de nuevo.\n3. Elige Abrir una carpeta… con tus propias " +
                "notas; en Consigue la IA haz clic en Descargar y empezar."
        )

        expected(
            text =
            "Paso 2: una hoja sobre la pantalla de bienvenida con el logo, tres promesas con " +
                "iconos, Continuar y Omitir, \"1 de 3\"; Esc la cierra y no vuelve. Paso 3: la " +
                "bóveda se abre, el paso de la IA nombra el tamaño que cabe en esta computadora, " +
                "y la descarga se ve en la barra de estado mientras se indexa la bóveda."
        )
    }
}
