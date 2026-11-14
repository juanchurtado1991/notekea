package app.notekea.autotest.cases.m4

import app.notekea.autotest.StandInDocsSite
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.openVault
import app.notekea.autotest.rightClickInTree
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

// The developer's requests of 2026-11-13: what the notes do not hold is looked for in the site the web sources came
// from, and study cards are made of a subject, not only of a note.
internal fun SuiteBuilder.lookFurtherCases() {
    manual(
        id = "4.4-07",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "Lo que las notas no tienen se busca en el sitio de las fuentes web, y solo se " +
                "recorta lo que marques"
        )

        steps(
            text =
            "1. Con un modelo de chat instalado y una documentación recortada con sus páginas, " +
                "abre el chat.\n2. Pregunta algo que ese sitio trate en una página que aún no " +
                "recortaste.\n3. Haz clic en Buscarlo en <sitio>.\n4. Desmarca una página y haz " +
                "clic en Recortar y responder."
        )

        expected(
            text =
            "El chat dice que no lo encontró en tus notas y ofrece Buscarlo en <sitio>. Lista " +
                "hasta cinco páginas no recortadas, cada una con su casilla, marcadas. Solo las " +
                "marcadas pasan a ser notas en la carpeta de la sección y en su índice, y la " +
                "pregunta se responde con ellas. Si ninguna página se parece a la pregunta, lo " +
                "dice y no recorta nada."
        )
    }

    manual(
        id = "4.6-04",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)

        title(text = "Las tarjetas de estudio de un tema salen de las notas que llevan su nombre")

        steps(
            text =
            "1. Con un modelo de chat instalado, abre el chat.\n2. Pide: crea tarjetas de " +
                "estudio de <un tema del que varias notas llevan el nombre>.\n3. Mira la " +
                "tarjeta de la acción sin confirmarla; luego haz clic en Hacerlo."
        )

        expected(
            text =
            "La tarjeta propone tarjetas de estudio para hasta cinco notas con el tema en su " +
                "nombre, ocho por nota como mucho, cada una respaldada por una frase de la " +
                "nota; una nota que no respalda ninguna queda fuera. Nada se escribe antes de " +
                "Hacerlo."
        )
    }
}
