package app.notekea.autotest.cases.m3

import app.notekea.autotest.cases.m1.PLAYGROUND
import app.notekea.autotest.cases.m1.WELCOME
import app.notekea.autotest.openVault
import com.testbuddy.ManualReason
import com.testbuddy.SuiteBuilder

private const val OPEN_AI_MODEL =
    "1. In Notekea, open the Playground Vault (see TEST DATA above).\n2. Click Settings (the " +
        "gear at the bottom of the narrow bar on the far left), then AI model in the list on the left."

/** The cases of 3.1, the model manager (`MVP_SMOKE_TEST.md`): Settings → AI model. */
fun SuiteBuilder.modelManagerCases() {
    recommendedSize()
    startPauseAndCancel()
    downloadAndResume()
    cancelAndDelete()
    droppedConnection()
}

private fun SuiteBuilder.recommendedSize() {
    automated(
        id = "3.1-01",
        title = "Settings → AI model recommends a size, with the others folded under it"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Settings")
        click(text = "AI model")
        expectText(text = "Recommended")
        expectText(text = "OTHER SIZES")
        click(text = "OTHER SIZES")
        expectText(text = "Small · Qwen3.5 2B")
        expectText(text = "Medium · Qwen3.5 4B")
        expectText(text = "Large · Qwen3.5 9B")
    }
}

private fun SuiteBuilder.startPauseAndCancel() {
    automated(
        id = "3.1-05",
        title = "A download shows its progress, pauses and is cancelled, leaving nothing behind"
    ) {
        openVault(
            name = PLAYGROUND,
            shownText = WELCOME
        )

        clickIcon(label = "Settings")
        click(text = "AI model")
        click(text = "Download")
        expectText(text = "Pause")

        expectText(
            text = " GB of ",
            substring = true
        )

        click(text = "Pause")

        expectText(
            text = "Paused · ",
            substring = true
        )

        expectText(text = "Resume")
        click(text = "Cancel")
        expectText(text = "Download")
        expectNoText(text = "Resume")
    }
}

private fun SuiteBuilder.downloadAndResume() {
    manual(
        id = "3.1-02",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "The recommended model downloads, pauses, resumes and is put in use")

        steps(
            text =
            "$OPEN_AI_MODEL\n3. Click Download on the card marked Recommended.\n4. When the bar passes a " +
                "quarter, click Pause, press Esc to close Settings, and open AI model again.\n5. Click Resume and " +
                "wait until the download ends (from a few minutes to half an hour, depending on the connection)."
        )

        expected(
            text =
            "The bar grows with the gigabytes done of the total and the speed (as in 1.9 GB of 3.2 GB · " +
                "24.5 MB/s). In step 4 it stays paused where it was, also after closing Settings. In step 5 it goes " +
                "on from there, not from the start, shows Checking the file… for a moment, and then the card says " +
                "In use, with Delete next to it."
        )
    }
}

private fun SuiteBuilder.cancelAndDelete() {
    manual(
        id = "3.1-03",
        reason = ManualReason.COSTLY
    ) {
        prepare(PLAYGROUND)
        title(text = "Deleting a model asks first and frees it")

        steps(
            text =
            "$OPEN_AI_MODEL\n3. On the model in use (download one first if none says In use), click Delete, " +
                "then Cancel in the question.\n4. Click Delete again and confirm with Delete."
        )

        expected(
            text = "Step 3 keeps the model. Step 4 deletes it: the card shows Download again and no size says In use."
        )
    }
}

private fun SuiteBuilder.droppedConnection() {
    manual(
        id = "3.1-04",
        reason = ManualReason.SYSTEM
    ) {
        prepare(PLAYGROUND)

        title(
            text = "A dropped connection stops the download, which goes on later from where it stopped"
        )

        steps(
            text =
            "$OPEN_AI_MODEL\n3. Click Download on a size that is not downloaded.\n4. While it downloads, turn " +
                "off the Wi-Fi or unplug the network cable and wait half a minute.\n5. Turn the connection on " +
                "again and click Try again."
        )

        expected(
            text =
            "Step 4 shows that the download stopped and asks to check the connection, with Try again. In " +
                "step 5 the bar goes on from where it stopped, not from zero."
        )
    }
}
