package net.dungeonhub.wrapper.javacord

import net.dungeonhub.wrapper.DiscordMessageEmbed
import net.dungeonhub.wrapper.DiscordMessageEmbedAuthor
import net.dungeonhub.wrapper.DiscordMessageEmbedField
import net.dungeonhub.wrapper.DiscordMessageEmbedFooter
import org.javacord.api.entity.message.embed.Embed
import java.awt.Color
import java.time.Instant

class JavacordMessageEmbed(val embed: Embed): DiscordMessageEmbed {
    override val color: Color?
        get() = embed.color.orElse(null)
    override val title: String?
        get() = embed.title.orElse(null)
    override val description: String?
        get() = embed.description.orElse(null)
    override val fields: List<DiscordMessageEmbedField>
        get() = embed.fields.map { JavacordMessageEmbedField(it) }
    override val url: String?
        get() = embed.url.orElse(null)?.toString()
    override val thumbnail: String?
        get() = embed.thumbnail.orElse(null)?.url?.toString()
    override val image: String?
        get() = embed.image.orElse(null)?.url?.toString()
    override val footer: DiscordMessageEmbedFooter?
        get() = embed.footer.map { JavacordMessageEmbedFooter(it) }.orElse(null)
    override val author: DiscordMessageEmbedAuthor?
        get() = embed.author.map { JavacordMessageEmbedAuthor(it) }.orElse(null)
    override val timestamp: Instant?
        get() = embed.timestamp.orElse(null)
}