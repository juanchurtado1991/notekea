package app.notekea.video.player

/**
 * The page the YouTube tab shows: YouTube's own player for a video, over the whole page, without its tracking cookies.
 * It is loaded as if from [ORIGIN], which YouTube asks an app's player to say who it is (without it the player answers
 * "error 153"); the page's title says what the player is doing ("Notekea:1" while it plays).
 */
internal object YouTubePage {
    const val ORIGIN = "https://app.notekea"
    const val TITLE_PREFIX = "Notekea:"
    private val ID = Regex(pattern = "^[A-Za-z0-9_-]{6,20}$")

    /** The page for the video [id]; null for an id YouTube never gives, which is not written into the page. */
    fun of(
        id: String,
        isMuted: Boolean = false
    ): String? {
        if (!ID.matches(input = id)) return null
        val mute = if (isMuted) "&mute=1" else ""

        val address = "https://www.youtube-nocookie.com/embed/$id" +
            "?autoplay=1&playsinline=1&enablejsapi=1&origin=$ORIGIN$mute"

        return """
            <!doctype html>
            <html>
            <head>
            <meta charset="utf-8">
            <meta name="referrer" content="strict-origin-when-cross-origin">
            <style>html,body{margin:0;height:100%;background:#000;overflow:hidden}iframe{border:0;width:100%;height:100%}</style>
            </head>
            <body>
            <iframe id="player" src="$address" allow="autoplay; encrypted-media; fullscreen; picture-in-picture"
              allowfullscreen referrerpolicy="strict-origin-when-cross-origin"></iframe>
            <script>
            var player = document.getElementById('player');
            player.addEventListener('load', function () {
              player.contentWindow.postMessage(JSON.stringify({event: 'listening', id: 1, channel: 'widget'}), '*');
            });
            window.addEventListener('message', function (message) {
              try {
                var data = JSON.parse(message.data);
                if (data.info && data.info.playerState !== undefined) {
                  document.title = '$TITLE_PREFIX' + data.info.playerState;
                }
              } catch (ignored) {}
            });
            </script>
            </body>
            </html>
        """.trimIndent()
    }
}
