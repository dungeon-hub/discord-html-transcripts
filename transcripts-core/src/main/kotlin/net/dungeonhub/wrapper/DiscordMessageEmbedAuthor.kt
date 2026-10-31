package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.components.embedAuthor
import net.dungeonhub.transcripts.components.renderElement
import org.jsoup.nodes.Element

interface DiscordMessageEmbedAuthor {
    val name: String?
    val iconUrl: String?
    val url: String?

    @Deprecated(
        "Use the kotlinx.html component instead",
        ReplaceWith("embedAuthor(this)", "net.dungeonhub.transcripts.components.embedAuthor")
    )
    fun transcriptify(): Element = renderElement { embedAuthor(this@DiscordMessageEmbedAuthor) }
}
