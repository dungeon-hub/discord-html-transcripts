package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordMessageEmbed
import net.dungeonhub.wrapper.DiscordMessageEmbedAuthor
import net.dungeonhub.wrapper.DiscordMessageEmbedField
import net.dungeonhub.wrapper.DiscordMessageEmbedFooter
import net.dv8tion.jda.api.entities.MessageEmbed
import java.awt.Color
import java.time.Instant

class JDAMessageEmbed(val embed: MessageEmbed): DiscordMessageEmbed {
    override val color: Color?
        get() = embed.color
    override val title: String?
        get() = embed.title
    override val description: String?
        get() = embed.description
    override val fields: List<DiscordMessageEmbedField>
        get() = embed.fields.map { JDAMessageEmbedField(it) }
    override val url: String?
        get() = embed.url
    override val thumbnail: String?
        get() = embed.thumbnail?.url
    override val image: String?
        get() = embed.image?.url
    override val footer: DiscordMessageEmbedFooter?
        get() = embed.footer?.let { JDAMessageEmbedFooter(it) }
    override val author: DiscordMessageEmbedAuthor?
        get() = embed.author?.let { JDAMessageEmbedAuthor(it) }
    override val timestamp: Instant?
        get() = embed.timestamp?.toInstant()
}