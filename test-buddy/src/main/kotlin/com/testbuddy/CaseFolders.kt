package com.testbuddy

import java.nio.file.Path

/**
 * The folders one case runs in, fresh for every case and deleted after it: [appData] stands for the app's data
 * folder, never the real one, and [data] holds a copy of the suite's test data.
 */
public data class CaseFolders(
    val appData: Path,
    val data: Path
)
