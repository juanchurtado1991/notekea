package app.notekea.core.notes.domain

/**
 * Splits a note's body into the [NoteChunk]s the index embeds, by its sections ([NoteSections]): one shorter than
 * [ChunkSize.MIN] joins the next ones under its parent heading, one that fits [ChunkSize.MAX] is one chunk, and a
 * longer one is split between its blocks ([ChunkBlocks]) and packed to [ChunkSize.TARGET] ([ChunkPacker]).
 */
class ChunkNoteUseCase(private val noteSections: NoteSections) {
    operator fun invoke(
        note: RelativePath,
        text: String
    ): List<NoteChunk> {
        val body = NoteDocument.fromText(text = text).body

        return groups(
            body = body,
            sections = noteSections.of(body = body)
        )
            .flatMap { group ->
                rangesOf(
                    body = body,
                    group = group
                ).map {
                    group.headingPath to it
                }
            }
            .mapIndexed { index, (headingPath, range) ->
                val chunkText = body.substring(range = range)

                NoteChunk(
                    note = note,
                    index = index,
                    headingPath = headingPath,
                    range = range,
                    text = chunkText,
                    tokens = TokenEstimate.of(text = chunkText)
                )
            }
    }

    /** [sections] with the short ones joined to the ones after them. */
    private fun groups(
        body: String,
        sections: List<NoteSection>
    ): List<NoteSection> {
        val groups = mutableListOf<NoteSection>()

        sections.forEach { section ->
            val last = groups.lastOrNull()

            if (last != null && joins(body = body, group = last, next = section)) {
                groups[groups.lastIndex] = NoteSection(
                    headingPath = sharedPath(
                        first = last.headingPath,
                        second = section.headingPath
                    ),
                    range = last.range.first..section.range.last
                )
            } else {
                groups += section
            }
        }

        return groups
    }

    /** Whether [next] joins [group]: the group is short, they fit together, and [next] stays under its parent. */
    private fun joins(
        body: String,
        group: NoteSection,
        next: NoteSection
    ): Boolean {
        val shared = sharedPath(
            first = group.headingPath,
            second = next.headingPath
        ).size

        return ChunkSize.of(body = body, range = group.range) < ChunkSize.MIN &&
            ChunkSize.of(
                body = body,
                range = group.range.first..next.range.last
            ) <= ChunkSize.MAX &&
            shared >= group.headingPath.size - 1
    }

    /** The headings [first] and [second] both start with. */
    private fun sharedPath(
        first: List<String>,
        second: List<String>
    ): List<String> = first
        .zip(other = second)
        .takeWhile { (one, other) ->
            one == other
        }
        .map { it.first }

    /** The chunks' ranges of [group]: the whole of it when it fits, otherwise its blocks packed and overlapped. */
    private fun rangesOf(
        body: String,
        group: NoteSection
    ): List<IntRange> {
        val whole = ChunkBlocks.trimmed(
            body = body,
            range = group.range
        )

        if (ChunkSize.of(body = body, range = whole) <= ChunkSize.MAX) return listOf(whole)

        val packed = ChunkPacker.pack(
            body = body,
            ranges = ChunkBlocks.of(
                body = body,
                range = group.range
            )
        )

        return ChunkPacker.overlapped(
            body = body,
            chunks = packed
        )
    }
}
