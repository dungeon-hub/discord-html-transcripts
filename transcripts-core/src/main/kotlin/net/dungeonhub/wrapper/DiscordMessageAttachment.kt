package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.components.attachment
import net.dungeonhub.transcripts.components.renderElement
import org.jsoup.nodes.Element

interface DiscordMessageAttachment {
    val fileName: String
    val isImage: Boolean
    val url: String
    val size: Long

    @Deprecated(
        "Use the kotlinx.html component instead",
        ReplaceWith("attachment(this)", "net.dungeonhub.transcripts.components.attachment")
    )
    fun transcriptify(): Element = renderElement { attachment(this@DiscordMessageAttachment) }
}
