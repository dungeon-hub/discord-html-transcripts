package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.components.message
import net.dungeonhub.transcripts.components.renderElement
import org.jsoup.nodes.Element
import java.time.Instant

// TODO would message.actionRows also be interesting to have here?
interface DiscordMessage {
    val id: Long
    val content: String
    val embeds: List<DiscordMessageEmbed?>
    val attachments: List<DiscordMessageAttachment>
    val creationTime: Instant
    val reference: DiscordMessage?
    val author: DiscordMessageAuthor?
    val interaction: DiscordInteraction?

    @Deprecated(
        "Use the kotlinx.html component instead",
        ReplaceWith("message(this)", "net.dungeonhub.transcripts.components.message")
    )
    fun transcriptify(): Element = renderElement { message(this@DiscordMessage) }
}
