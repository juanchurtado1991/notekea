package app.notekea.autotest.cases.m4

import app.notekea.autotest.StandInDocsSite
import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.cases.m1.openPlaygroundNote
import app.notekea.autotest.openVault
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder
import java.nio.file.Files

private const val TRIP = "Trip video"
private const val YOUTUBE = "Arepas video"

/**
 * The cases of 4.9, videos in notes (`MVP_SMOKE_TEST.md`, the developer's request of 2026-11-11): the cards a video and
 * a YouTube link show, without the network, and a clipped page's videos; playing, pasting and compressing are manual.
 */
fun SuiteBuilder.videoCases() {
    videoCard()
    playing()
    youTubeCard()
    pastedYouTube()
    clippedVideo()
    compressed()
}

private fun SuiteBuilder.videoCard() {
    automated(
        id = "4.9-01",
        title = "A video embedded in a note shows as a card with its name and format, and plays nothing until clicked"
    ) {
        Files.createDirectories(file(path = "$PLAYGROUND/attachments"))
        Files.write(file(path = "$PLAYGROUND/attachments/trip.mp4"), ByteArray(size = 1024))
        Files.writeString(file(path = "$PLAYGROUND/$TRIP.md"), "# Trip\n\n![[trip.mp4]]\n")

        openPlaygroundNote(
            name = TRIP,
            shownText = "Video · MP4"
        )

        expectText(text = "trip")
        expectNoText(text = "Pause")
    }
}

private fun SuiteBuilder.playing() {
    manual(
        id = "4.9-02",
        reason = ManualReason.EYES
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "Un video se reproduce en su pestaña, con volumen y pantalla completa, y uno " +
                "que esta computadora no puede reproducir se abre con su app"
        )

        steps(
            text =
            "1. Abre la Playground Vault y una nota; suelta en ella un mp4 real (un video " +
                "del teléfono).\n2. Haz clic en su tarjeta; páusalo, haz clic en el medio de la " +
                "barra, arrastra el volumen, haz clic en silenciar, luego en pantalla completa y " +
                "pulsa Esc.\n3. En macOS, suelta un video .webm y haz clic en su tarjeta."
        )

        expected(
            text =
            "La tarjeta muestra una imagen del video (nunca un cuadro negro si el video " +
                "tiene otro). El paso 2 abre el video en una pestaña propia y lo reproduce en " +
                "toda la pestaña con su tiempo; la barra lo lleva a la mitad; el volumen y " +
                "silenciar cambian el sonido; pantalla completa cubre la pantalla con los mismos " +
                "controles y Esc sale. El paso 3 dice que esta computadora no puede reproducirlo " +
                "aquí, y Abrir con su app lo abre."
        )
    }
}

private fun SuiteBuilder.youTubeCard() {
    automated(
        id = "4.9-03",
        title = "A YouTube video embedded with its title shows as its card, without going on the network"
    ) {
        Files.writeString(
            file(path = "$PLAYGROUND/$YOUTUBE.md"),
            "# Arepas\n\n![Cómo hacer arepas](https://youtu.be/${StandInDocsSite.VIDEO_ID})\n"
        )

        openPlaygroundNote(
            name = YOUTUBE,
            shownText = "Cómo hacer arepas"
        )

        expectText(text = "YouTube")
    }
}

private fun SuiteBuilder.pastedYouTube() {
    manual(
        id = "4.9-04",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "Un enlace de YouTube pegado se convierte en su tarjeta, con título una sola " +
                "vez, y se reproduce en una pestaña de Notekea"
        )

        steps(
            text =
            "1. Copia la dirección de un video de YouTube desde el navegador.\n2. Abre una " +
                "nota de la Playground Vault y pégala sola en una línea.\n3. Haz clic en su " +
                "tarjeta.\n4. Usa el volumen y la pantalla completa del reproductor.\n5. Cierra " +
                "la pestaña."
        )

        expected(
            text =
            "El paso 2 escribe la inserción al instante; un momento después tiene el título " +
                "del video y la tarjeta su imagen. En macOS el paso 3 abre una pestaña con el " +
                "nombre del video que lo reproduce dentro de la ventana de Notekea (sin un " +
                "segundo Notekea en el Dock); el volumen y la pantalla completa son los de " +
                "YouTube; cerrar la pestaña lo detiene. En Windows y Linux lo reproduce una " +
                "ventana de Notekea, que se cierra al cerrar Notekea."
        )
    }
}

private fun SuiteBuilder.clippedVideo() {
    automated(
        id = "4.9-05",
        title = "A clipped page's YouTube video is kept under Videos, as its card"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        click(text = "Clip a web page")
        click(text = "https://example.com/an-article")
        type(text = StandInDocsSite.VIDEO_ADDRESS)
        click(text = "Clip")

        expectFile(
            path = "$PLAYGROUND/Web sources/Watch.md",
            containing = "## Videos\n\n![${StandInDocsSite.VIDEO_TITLE}](https://www.youtube.com/watch?v=" +
                "${StandInDocsSite.VIDEO_ID})"
        )
    }
}

private fun SuiteBuilder.compressed() {
    manual(
        id = "4.9-06",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)

        title(
            text =
            "Un video de más de 100 MB se reduce antes de adjuntarlo, y Cancelar no adjunta " +
                "nada"
        )

        steps(
            text =
            "1. Abre una nota de la Playground Vault con internet.\n2. Suelta en ella un " +
                "video de más de 100 MB (un video del teléfono de unos minutos) y mira el aviso " +
                "sobre la nota.\n3. Suéltalo de nuevo y haz clic en Cancelar en el aviso."
        )

        expected(
            text =
            "Paso 2: la primera vez descarga la herramienta de video (unos 27 MB), luego " +
                "reduce el video y luego inserta un .mp4 de menos de 100 MB que se reproduce. El " +
                "paso 3 se detiene y no adjunta nada. El video original no cambia."
        )
    }
}
