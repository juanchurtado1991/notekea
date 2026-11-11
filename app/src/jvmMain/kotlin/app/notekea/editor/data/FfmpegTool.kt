package app.notekea.editor.data

/**
 * The FFmpeg Notekea downloads for a system (#141): bytedeco's LGPL build on Maven Central, its jar's [size] and
 * SHA-256 ([sha256], taken when this version was chosen) and the folder of its files inside the jar ([folder]).
 */
internal data class FfmpegTool(
    val folder: String,
    val sha256: String,
    val size: Long,
    val program: String
) {
    val jarName: String get() = "ffmpeg-$VERSION-$folder.jar"
    val url: String get() = "$REPOSITORY/$VERSION/$jarName"

    companion object {
        const val VERSION = "8.1.2-1.5.14"
        private const val REPOSITORY = "https://repo1.maven.org/maven2/org/bytedeco/ffmpeg"
        private const val PROGRAM = "ffmpeg"
        private const val WINDOWS_PROGRAM = "ffmpeg.exe"

        // SHA-256 of each jar, downloaded once on 2026-11-11; their SHA-1 matched Maven Central's.
        private val TOOLS = listOf(
            FfmpegTool(
                folder = "linux-x86_64",
                sha256 = "6d0f000c4ddede3b669aa7c5c585e9b71f69b7f621fed7ab28ec01045dc336d0",
                size = 26_915_834,
                program = PROGRAM
            ),
            FfmpegTool(
                folder = "linux-arm64",
                sha256 = "c8729978c862b0e2e5643ade1b41266c5ba8c34b791f41104e5794ad3cefd0bf",
                size = 26_304_735,
                program = PROGRAM
            ),
            FfmpegTool(
                folder = "macosx-x86_64",
                sha256 = "b5a8f5124fb2d04412bd01eb5ca469d153d1c396fd0c67a33d3f9a1501c94c0c",
                size = 23_977_215,
                program = PROGRAM
            ),
            FfmpegTool(
                folder = "macosx-arm64",
                sha256 = "af57468c0bb7b2e9c8c93547e6d599e2b4faa4117118555be92675850972420c",
                size = 20_582_183,
                program = PROGRAM
            ),
            FfmpegTool(
                folder = "windows-x86_64",
                sha256 = "67ebc3e6940add83b1b8a9dfd337a5c67556f2b72cb3225efe91a26874445c8e",
                size = 30_441_678,
                program = WINDOWS_PROGRAM
            )
        )

        /** The tool for a system ([system]: "linux", "macosx" or "windows") and its [architecture]; null when none. */
        fun of(
            system: String,
            architecture: String
        ): FfmpegTool? {
            val isArm = architecture.lowercase() in setOf("aarch64", "arm64")
            val folder = "$system-${if (isArm) "arm64" else "x86_64"}"

            return TOOLS.firstOrNull {
                it.folder == folder
            }
        }
    }
}
