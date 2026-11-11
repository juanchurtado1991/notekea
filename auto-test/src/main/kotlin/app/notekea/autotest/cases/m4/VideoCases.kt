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
        title(text = "A video plays in its note, and one this computer cannot play opens with its app")

        steps(
            text =
            "1. Open the Playground Vault and a note; drop a real mp4 (a phone video) into it.\n2. Click its card; " +
                "pause it, click the middle of the bar under it, then ✕.\n3. On macOS, drop a .webm video and click " +
                "its card."
        )

        expected(
            text =
            "Step 2 plays the video in place with its time; the bar moves it to the middle; ✕ brings the card back. " +
                "Step 3 says this computer can't play it, and a click opens it with its app."
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
        title(text = "A pasted YouTube link becomes its card, titled once, and plays in a Notekea window")

        steps(
            text =
            "1. Copy a YouTube video's address from the browser.\n2. Open a note of the Playground Vault and paste " +
                "it alone on a line.\n3. Click its card.\n4. Close Notekea."
        )

        expected(
            text =
            "Step 2 writes the embed at once; a moment later it has the video's title and the card its picture. " +
                "Step 3 opens a Notekea window that plays the video. Step 4 closes that window too."
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
        title(text = "A video over 100 MB is made smaller before it is attached, and Cancel attaches nothing")

        steps(
            text =
            "1. Open a note of the Playground Vault with the internet on.\n2. Drop a video over 100 MB into it (a " +
                "phone video of a few minutes) and watch the banner above the note.\n3. Drop it again and click " +
                "Cancel on the banner."
        )

        expected(
            text =
            "Step 2: the first time it downloads the video tool (about 27 MB), then makes the video smaller, then " +
                "embeds an .mp4 under 100 MB that plays. Step 3 stops and attaches nothing. The original video is " +
                "unchanged."
        )
    }
}
