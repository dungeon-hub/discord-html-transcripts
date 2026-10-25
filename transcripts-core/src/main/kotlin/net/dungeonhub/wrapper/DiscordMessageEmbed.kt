package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.components.embed
import net.dungeonhub.transcripts.components.renderElement
import org.jsoup.nodes.Element
import java.awt.Color
import java.time.Instant

interface DiscordMessageEmbed {
    val color: Color?
    val title: String?
    val description: String?
    val fields: List<DiscordMessageEmbedField>
    val url: String?
    val thumbnail: String?
    val image: String?
    val footer: DiscordMessageEmbedFooter?
    val author: DiscordMessageEmbedAuthor?
    val timestamp: Instant?

    @Deprecated(
        "Use the kotlinx.html component instead",
        ReplaceWith("embed(this)", "net.dungeonhub.transcripts.components.embed")
    )
    fun transcriptify(): Element = renderElement { embed(this@DiscordMessageEmbed) }
}
