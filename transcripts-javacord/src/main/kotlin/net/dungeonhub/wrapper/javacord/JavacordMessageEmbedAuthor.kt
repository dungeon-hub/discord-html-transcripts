package net.dungeonhub.wrapper.javacord

import net.dungeonhub.wrapper.DiscordMessageEmbedAuthor
import org.javacord.api.entity.message.embed.EmbedAuthor

class JavacordMessageEmbedAuthor(val author: EmbedAuthor): DiscordMessageEmbedAuthor {
    override val name: String?
        get() = author.name
    override val iconUrl: String?
        get() = author.iconUrl.orElse(null)?.toString()
    override val url: String?
        get() = author.url.orElse(null)?.toString()
}