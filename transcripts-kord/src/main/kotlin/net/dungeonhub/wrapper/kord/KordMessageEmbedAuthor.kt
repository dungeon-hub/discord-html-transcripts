package net.dungeonhub.wrapper.kord

import dev.kord.core.entity.Embed
import net.dungeonhub.wrapper.DiscordMessageEmbedAuthor

class KordMessageEmbedAuthor(val author: Embed.Author): DiscordMessageEmbedAuthor {
    override val name: String?
        get() = author.name
    override val iconUrl: String?
        get() = author.iconUrl
    override val url: String?
        get() = author.url
}