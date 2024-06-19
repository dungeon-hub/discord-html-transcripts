package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordMessageEmbedAuthor
import net.dv8tion.jda.api.entities.MessageEmbed.AuthorInfo

class JDAMessageEmbedAuthor(val author: AuthorInfo): DiscordMessageEmbedAuthor {
    override val name: String?
        get() = author.name
    override val iconUrl: String?
        get() = author.iconUrl
    override val url: String?
        get() = author.url
}