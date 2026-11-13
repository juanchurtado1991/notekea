package app.notekea.autotest.cases.m1

import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_IMAGES =
    "1. En Notekea, abre la Playground Vault (mira DATOS DE PRUEBA arriba).\n2. Abre la nota Editor › Images and files."

/** The cases of 1.8 that need the system (its file dialogs, file manager, clipboard, PDF viewer) or a person's eyes. */
internal fun SuiteBuilder.attachmentManualCases() {
    pictureSizes()
    dropFiles()
    pasteScreenshot()
    obsidianAttachmentFolder()
    sameInObsidian()
    pictureInTable()
    waylandClipboard()
    dropNote()
}

private fun SuiteBuilder.pictureSizes() {
    manual(
        id = "1.8-01",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Imágenes al tamaño correcto, archivos como fichas")
        steps(text = "$OPEN_IMAGES\n3. Desplázate por la nota sin hacer clic en su texto.")

        expected(
            text =
            "El plato naranja y azul se ve tres veces: a su propio tamaño (480 de ancho, o " +
                "el ancho de la nota si es más estrecha), a 200 de ancho, y una vez más desde el " +
                "enlace Markdown. Weekly menu.pdf es una tarjeta con la imagen de su primera " +
                "página y \"Se abre en una pestaña\"; broken.png es una ficha de archivo con un " +
                "clip. Lost photo.png dice \"Falta este archivo\" " +
                "con su nombre y Buscar…. En el último párrafo \"the menu\" es un enlace."
        )
    }
}

private fun SuiteBuilder.dropFiles() {
    manual(
        id = "1.8-06",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Soltar imágenes desde el administrador de archivos")

        steps(
            text =
            "$OPEN_IMAGES\n3. En tu administrador de archivos, abre la carpeta Outside files " +
                "de la carpeta Test data y arrastra Found plate.png a la nota.\n4. Luego " +
                "arrastra Arepas plate.png desde la carpeta de adjuntos de la bóveda a la nota."
        )

        expected(
            text =
            "La primera se copia como \"Found plate 1.png\" (el nombre ya existía) y se ve " +
                "con su ancho, ![[Found plate 1.png|320]]. La segunda ya está en la bóveda, así " +
                "que se muestra como ![[Arepas plate.png|400]] sin copiarla. Cambiar uno de los " +
                "números cambia el tamaño de la imagen."
        )
    }
}

private fun SuiteBuilder.pasteScreenshot() {
    manual(
        id = "1.8-07",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Pegar una captura de pantalla")

        steps(
            text =
            "$OPEN_IMAGES\n3. Haz una captura al portapapeles: Win+Shift+S en Windows, " +
                "Ctrl+Shift+Cmd+4 en macOS, la herramienta de capturas de tu escritorio en Linux " +
                "(copiar al portapapeles).\n4. Haz clic en una línea vacía de la nota y pulsa " +
                "Ctrl+V (Cmd+V en macOS).\n5. Copia una palabra de cualquier lugar y pégala " +
                "también en la nota."
        )

        expected(
            text =
            "Aparece una línea ![[Pasted image <fecha y hora>.png|400]] con la captura " +
                "debajo, y el archivo está en la carpeta de adjuntos. La palabra copiada se pega " +
                "como texto simple."
        )
    }
}

private fun SuiteBuilder.obsidianAttachmentFolder() {
    manual(
        id = "1.8-08",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Las imágenes pegadas siguen el ajuste de adjuntos de Obsidian")

        steps(
            text =
            "$OPEN_IMAGES\n3. Cierra Notekea. En la carpeta Playground Vault, abre la " +
                "carpeta oculta .obsidian (en macOS pulsa Cmd+Shift+. en el Finder para verla) y " +
                "abre app.json con un editor de texto.\n4. Agrega \"attachmentFolderPath\": " +
                "\"./\" dentro de sus llaves, guarda y vuelve a abrir la Playground Vault en " +
                "Notekea.\n5. Pega una captura en Editor › Images and files, como en el caso " +
                "anterior."
        )

        expected(
            text =
            "La imagen pegada se guarda junto a la nota, en la carpeta Editor, como lo haría " +
                "Obsidian."
        )
    }
}

private fun SuiteBuilder.sameInObsidian() {
    manual(
        id = "1.8-09",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Las mismas imágenes en Obsidian")

        steps(
            text =
            "Si Obsidian no está instalado, pulsa Saltar este caso.\n$OPEN_IMAGES\n3. Abre " +
                "la Playground Vault en Obsidian y mira Editor › Images and files."
        )

        expected(
            text =
            "El texto no cambió y todas las imágenes, los PDF y la captura pegada se ven " +
                "como en Notekea. Solo Lost photo.png ya no está, reemplazada por Found " +
                "plate.png."
        )
    }
}

private fun SuiteBuilder.pictureInTable() {
    manual(
        id = "1.8-10",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Una imagen dentro de una tabla")

        steps(
            text =
            "$OPEN_IMAGES\n3. Desplázate hasta la tabla del final.\n4. Haz clic en Fuente " +
                "(</>) en la barra de herramientas, agrega una fila | Cachapa |  | debajo de la " +
                "tabla, pon el cursor en su celda Photo vacía y elige Más (⋯) › Imagen o " +
                "archivo, eligiendo Found plate.png de la carpeta Outside files."
        )

        expected(
            text =
            "El plato se ve de 80 de ancho dentro de su celda, no el texto \"80\". La imagen " +
                "nueva se escribe dentro de la celda con su ancho y la barra vertical escapada, " +
                "la fila conserva sus dos columnas, y un segundo clic en Fuente muestra el plato " +
                "verde en ella."
        )
    }
}

private fun SuiteBuilder.waylandClipboard() {
    manual(
        id = "1.8-11",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Pegar imágenes en Linux con Wayland")

        steps(
            text =
            "Solo en un escritorio Linux con Wayland (GNOME, KDE, COSMIC). En Windows y " +
                "macOS pulsa Saltar este caso.\n$OPEN_IMAGES\n3. Si wl-clipboard está instalado " +
                "(wl-paste --version responde), desinstálalo para este caso, copia una captura y " +
                "pulsa Ctrl+V en la nota.\n4. Instala de nuevo wl-clipboard, reinicia Notekea y " +
                "pulsa Ctrl+V."
        )

        expected(
            text =
            "Sin wl-clipboard, en menos de un segundo un aviso dice cómo instalarlo y no se " +
                "pega nada. Con él, la captura se pega al instante. En GNOME, anota en una nota " +
                "si se pegó."
        )
    }
}

private fun SuiteBuilder.dropNote() {
    manual(
        id = "1.8-14",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND, "Outside files", PERSONAL_NOTES)
        title(text = "Soltar una nota desde el administrador de archivos")

        steps(
            text =
            "$OPEN_IMAGES\n3. En tu administrador de archivos, arrastra cualquier nota .md " +
                "(por ejemplo Welcome.md de la bóveda Personal Notes en la carpeta Test data) a " +
                "la nota."
        )

        expected(
            text =
            "La nota se copia en la carpeta de adjuntos, se inserta en una línea propia y su " +
                "texto se ve en un marco."
        )
    }
}
