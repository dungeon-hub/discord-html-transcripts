package net.dungeonhub.wrapper.kord

import dev.kord.core.entity.Embed
import kotlinx.datetime.toJavaInstant
import net.dungeonhub.wrapper.DiscordMessageEmbed
import net.dungeonhub.wrapper.DiscordMessageEmbedAuthor
import net.dungeonhub.wrapper.DiscordMessageEmbedField
import net.dungeonhub.wrapper.DiscordMessageEmbedFooter
import java.awt.Color
import java.time.Instant

class KordMessageEmbed(val embed: Embed): DiscordMessageEmbed {
    override val color: Color?
        get() = embed.color?.toJavaColor()
    override val title: String?
        get() = embed.title
    override val description: String?
        get() = embed.description
    override val fields: List<DiscordMessageEmbedField>
        get() = embed.fields.map { KordMessageEmbedField(it) }
    override val url: String?
        get() = embed.url
    override val thumbnail: String?
        get() = embed.thumbnail?.url
    override val image: String?
        get() = embed.image?.url
    override val footer: DiscordMessageEmbedFooter?
        get() = embed.footer?.let { KordMessageEmbedFooter(it) }
    override val author: DiscordMessageEmbedAuthor?
        get() = embed.author?.let { KordMessageEmbedAuthor(it) }
    override val timestamp: Instant?
        get() = embed.timestamp?.toJavaInstant()
}