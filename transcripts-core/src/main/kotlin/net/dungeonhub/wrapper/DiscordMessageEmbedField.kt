package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.components.embedField
import net.dungeonhub.transcripts.components.renderElement
import org.jsoup.nodes.Element

interface DiscordMessageEmbedField {
    val name: String
    val value: String
    val isInline: Boolean

    @Deprecated(
        "Use the kotlinx.html component instead",
        ReplaceWith("embedField(this)", "net.dungeonhub.transcripts.components.embedField")
    )
    fun transcriptify(): Element = renderElement { embedField(this@DiscordMessageEmbedField) }
}
