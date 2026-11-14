package app.notekea.autotest.cases.m4

import app.notekea.autotest.cases.m1.PLAYGROUND
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

// The developer's smoke of 2026-11-14: a crash on a click over a citation, a note made up from a pasted contract
// the chat never read, a note created at the root without a word, and the look of the chat and the tree.
internal fun SuiteBuilder.solidChatCases() {
    manual(
        id = "1.4-15",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "Una nota se arrastra a una carpeta dentro del árbol")

        steps(
            text =
            "1. En el árbol, arrastra una nota sobre una carpeta y suéltala.\n2. Arrastra otra " +
                "al espacio vacío bajo las filas.\n3. Arrastra una nota a una carpeta que ya " +
                "tiene otra con ese nombre."
        )

        expected(
            text =
            "Mientras arrastras, la carpeta de destino se tiñe y la fila arrastrada se atenúa. " +
                "Al soltar, la nota pasa a esa carpeta (o a la raíz), su pestaña sigue " +
                "abierta y los enlaces a ella siguen funcionando. Si el nombre ya existe allí, " +
                "no se mueve y el árbol lo dice."
        )
    }

    manual(
        id = "1.4-16",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Duplicar una nota, y las propiedades de una nota nueva")

        steps(
            text =
            "1. Clic derecho en una nota del árbol y elige Duplicar.\n2. Crea una nota nueva " +
                "y ábrela en modo código fuente."
        )

        expected(
            text =
            "Aparece «<nombre> copia» al lado, con el mismo texto y el nombre listo para " +
                "cambiar. La nota nueva empieza con las propiedades created (la fecha de hoy) " +
                "y tags (vacía)."
        )
    }

    manual(
        id = "1.2-08",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)
        title(text = "La X que cierra un diálogo es roja")
        steps(text = "1. Abre Ajustes.\n2. Abre Acerca de.")
        expected(text = "La X de la esquina se ve en rojo en los dos, en tema claro y oscuro.")
    }

    solidChatManualCases()
}

private fun SuiteBuilder.solidChatManualCases() {
    manual(
        id = "3.3-13",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "La pregunta va en una burbuja a la derecha y un clic sobre la respuesta no cierra la app")

        steps(
            text =
            "1. Con un modelo de chat instalado, haz una pregunta que tus notas respondan.\n" +
                "2. Deja el puntero sobre un número de cita hasta que salga su globo.\n3. Haz " +
                "clic sobre el texto de la respuesta y luego sobre la cita."
        )

        expected(
            text =
            "Tu pregunta se ve en una burbuja a la derecha y la respuesta a la izquierda. El " +
                "clic con el globo a la vista no cierra Notekea, y el clic en la cita abre el " +
                "pasaje."
        )
    }

    manual(
        id = "3.9-08",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "La tarjeta dice dónde se crea la nota y deja cambiarlo")

        steps(
            text =
            "1. Con una nota de una carpeta abierta, pide al chat: crea una nota llamada " +
                "Ideas.\n2. En la tarjeta, mira «En» y elige otra carpeta.\n3. Haz clic en " +
                "Hacerlo."
        )

        expected(
            text =
            "La tarjeta propone la carpeta de la nota abierta, no la raíz, y lista las " +
                "carpetas de la bóveda. La nota se crea en la carpeta elegida."
        )
    }

    manual(
        id = "4.7-07",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Una nota escrita desde un documento pegado sale de lo que el documento dice")

        steps(
            text =
            "1. Pega un PDF o un Word con texto en una nota y déjala abierta.\n2. Pide al " +
                "chat: crea una nota con una tabla con los pros y contras de ese documento.\n" +
                "3. Haz clic en Hacerlo y compara la nota con el documento.\n4. Pregunta: " +
                "¿te lo inventaste?\n5. Repite el paso 2 con una nota que solo tiene una imagen " +
                "escaneada."
        )

        expected(
            text =
            "La nota se escribe con datos del documento (nombres, medidas, cifras que están " +
                "en él), sin filas de ejemplo ni «reemplaza con tus datos». A la pregunta " +
                "responde qué documento leyó y que no lo inventó. Con la imagen escaneada dice " +
                "que no pudo leer texto y no escribe ninguna nota."
        )
    }

    citedSourceCases()
}

private fun SuiteBuilder.citedSourceCases() {
    manual(
        id = "3.3-14",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Una fuente citada se muestra seleccionada, sin poner sus líneas en edición")

        steps(
            text =
            "1. Con un modelo de chat instalado, pregunta algo que responda una nota con " +
                "negritas o una lista.\n2. Haz clic en la cita o en la fuente bajo la respuesta."
        )

        expected(
            text =
            "La nota se abre con el pasaje seleccionado y dibujado como se lee (sin ** ni - a " +
                "la vista), y lo que escribas sigue yendo al campo del chat. Un clic en la nota " +
                "edita como siempre."
        )
    }

    manual(
        id = "4.4-08",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Una página recortada se cita desde su texto original, no desde el resumen")

        steps(
            text =
            "1. Con un modelo de chat instalado, recorta una página web.\n2. Pregunta al chat " +
                "algo que la página responda y mira las fuentes."
        )

        expected(
            text =
            "Toda fuente de la nota recortada dice › Original text: el pasaje son palabras de " +
                "la página, nunca una línea de las notas que el modelo escribió encima."
        )
    }
}
