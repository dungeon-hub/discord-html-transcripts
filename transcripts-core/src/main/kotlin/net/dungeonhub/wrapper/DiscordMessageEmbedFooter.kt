package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.components.embedFooter
import net.dungeonhub.transcripts.components.renderElement
import org.jsoup.nodes.Element
import java.time.Instant

interface DiscordMessageEmbedFooter {
    val text: String?
    val iconUrl: String?

    @Deprecated(
        "Use the kotlinx.html component instead",
        ReplaceWith("embedFooter(this, timestamp)", "net.dungeonhub.transcripts.components.embedFooter")
    )
    fun transcriptify(timestamp: Instant?): Element =
        renderElement { embedFooter(this@DiscordMessageEmbedFooter, timestamp) }
}
