package app.notekea.autotest.cases

import app.notekea.autotest.cases.m1.PLAYGROUND
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_PLAYGROUND =
    "En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba)"
private const val PROCESS_LIST =
    "la lista de apps en ejecución (Windows: Administrador de tareas, Ctrl+Shift+Esc; macOS: Monitor de " +
        "Actividad, en Aplicaciones › Utilidades; Linux: Monitor del sistema)"
private const val REOPEN =
    "Abre de nuevo Notekea QA desde el lanzador del sistema: esta ventana vuelve a este caso."

/** The General cases of `MVP_SMOKE_TEST.md`: the installed app, the system and privacy. */
fun SuiteBuilder.generalCases() {
    installAndLaunch()
    cleanOutput()
    processExits()
    worksOffline()
    noTraffic()
    followsSystemTheme()
    crashNotice()
    launcherAndUpdate()
}

/** The last case of the QA build (G-09): free use, after every other case, to catch what no case thought of. */
fun SuiteBuilder.freeUseCase() {
    manual(
        id = "G-09",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Usa Notekea libremente durante 15 minutos")

        answers(
            passed = "Nada raro",
            failed = "Encontré algo"
        )

        steps(
            text =
            "1. $OPEN_PLAYGROUND.\n2. Durante 15 minutos, usa Notekea como lo usarías de " +
                "verdad: escribe notas, enlázalas, busca, abre y cierra notas y pestañas, abre " +
                "otra bóveda y vuelve.\n3. Cada vez que algo se vea mal, sorprenda o vaya lento, " +
                "anota qué hiciste y qué viste."
        )

        expected(
            text =
            "No pasa nada raro: ningún error, ningún texto perdido, nada que se vea mal. Si " +
                "pasó algo, elige Encontré algo y descríbelo, con una captura si puedes."
        )
    }
}

private fun SuiteBuilder.installAndLaunch() {
    manual(
        id = "G-01",
        reason = ManualReason.PACKAGED
    ) {
        title(text = "Notekea se instala y se abre como cualquier app")

        steps(
            text =
            "1. Recuerda cómo instalaste Notekea QA desde el archivo que te enviaron (msi en " +
                "Windows, dmg en macOS, deb en Linux) y cómo lo abriste desde el lanzador del " +
                "sistema (el menú Inicio, Launchpad o el menú de aplicaciones)."
        )

        expected(
            text =
            "Se instaló y se abrió. El sistema hizo como mucho su pregunta habitual para una " +
                "app que se abre por primera vez, y nada más."
        )
    }
}

private fun SuiteBuilder.cleanOutput() {
    manual(
        id = "G-02",
        reason = ManualReason.PACKAGED
    ) {
        prepare(PLAYGROUND)
        title(text = "Notekea arranca sin errores")

        steps(
            text =
            "1. $OPEN_PLAYGROUND y abre una nota.\n2. En esta ventana, haz clic en Reporte, " +
                "luego en Abrir la carpeta Report, y abre el archivo output.txt."
        )

        expected(
            text =
            "El archivo no tiene ninguna línea con \"Exception\" ni \"WARNING: A restricted " +
                "method\". Un archivo vacío está bien."
        )
    }
}

private fun SuiteBuilder.processExits() {
    manual(
        id = "G-03",
        reason = ManualReason.PACKAGED
    ) {
        title(text = "Cerrar la ventana cierra Notekea")

        steps(
            text =
            "1. Cierra la ventana de Notekea: esta ventana se cierra con ella.\n2. Unos " +
                "segundos después, busca Notekea QA en $PROCESS_LIST.\n3. $REOPEN"
        )

        expected(text = "Notekea QA no estaba en la lista: se cerró por completo.")
    }
}

private fun SuiteBuilder.worksOffline() {
    manual(
        id = "G-04",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Notekea funciona sin internet")

        steps(
            text =
            "1. Apaga el Wi-Fi y desconecta el cable de red.\n2. $OPEN_PLAYGROUND, edita una " +
                "nota y busca.\n3. Vuelve a encender la red."
        )

        expected(
            text =
            "Todo funciona como con internet; nada se queda esperando ni muestra un error de " +
                "red."
        )
    }
}

private fun SuiteBuilder.noTraffic() {
    manual(
        id = "G-05",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Notekea no envía nada por la red")

        steps(
            text =
            "1. Abre una app que muestre qué apps usan la red (Windows: Monitor de recursos, " +
                "pestaña Red; macOS: Monitor de Actividad, pestaña Red; Linux: el Monitor del " +
                "sistema o tu firewall).\n2. $OPEN_PLAYGROUND y úsalo durante 10 minutos."
        )

        expected(text = "Notekea QA no envía ni recibe nada: su fila se queda en cero.")
    }
}

private fun SuiteBuilder.followsSystemTheme() {
    manual(
        id = "G-06",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)
        title(text = "Notekea sigue el modo claro y oscuro del sistema")

        steps(
            text =
            "1. $OPEN_PLAYGROUND.\n2. En los ajustes del sistema, cambia entre modo claro y " +
                "oscuro con Notekea abierto."
        )

        expected(
            text =
            "Notekea se pone claro u oscuro al instante, con el sistema, y todos los textos " +
                "se leen bien."
        )
    }
}

private fun SuiteBuilder.crashNotice() {
    manual(
        id = "G-07",
        reason = ManualReason.PACKAGED
    ) {
        prepare(PLAYGROUND)
        title(text = "Un cierre inesperado muestra un aviso y conserva lo que escribiste")

        launchWith(
            name = "NOTEKEA_CRASH_TEST",
            value = "1"
        )

        steps(
            text =
            "1. Haz clic abajo en Reiniciar Notekea para este caso: Notekea arranca de nuevo " +
                "en un modo de prueba que falla a propósito.\n2. Rápido, abre la Playground " +
                "Vault desde la carpeta Test data, abre una nota, escribe una palabra y " +
                "espera.\n3. En la ventana que aparece, haz clic en Copiar detalles, luego en " +
                "Reportar en GitHub y luego en Cerrar Notekea.\n4. $REOPEN Abre la misma nota."
        )

        expected(
            text =
            "Unos 3 segundos después de abrirse Notekea, una ventana \"Notekea necesita " +
                "cerrarse\" nombró un archivo de reporte. Copiar detalles dijo \"Copiado al " +
                "portapapeles\"; Reportar en GitHub abrió en el navegador un formulario Crash " +
                "report con tus últimos pasos en el reporte; Cerrar Notekea la cerró. La palabra " +
                "que escribiste está en la nota."
        )
    }
}

private fun SuiteBuilder.launcherAndUpdate() {
    manual(
        id = "G-08",
        reason = ManualReason.PACKAGED
    ) {
        prepare(PLAYGROUND)
        title(text = "Notekea está en el lanzador, e instalarlo de nuevo lo actualiza")

        steps(
            text =
            "1. Busca Notekea QA en el lanzador del sistema.\n2. $OPEN_PLAYGROUND, abre una " +
                "nota y busca \"plan\" (la lupa a la izquierda).\n3. Solo en Windows: cierra " +
                "Notekea, ejecuta de nuevo su instalador y vuelve a abrir Notekea QA: esta " +
                "ventana vuelve a este caso."
        )

        expected(
            text =
            "Notekea QA está en el lanzador con su logo (Windows: el menú Inicio y un acceso " +
                "directo en el escritorio; macOS: Aplicaciones y Launchpad; Linux: el menú de " +
                "aplicaciones). La bóveda se abre y la búsqueda muestra resultados, sin ningún " +
                "aviso de cierre. En Windows, instalarlo de nuevo deja un solo Notekea QA en " +
                "Aplicaciones instaladas."
        )
    }
}
