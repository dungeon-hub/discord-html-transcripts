package net.dungeonhub.wrapper.javacord

import net.dungeonhub.wrapper.DiscordMessageEmbedFooter
import org.javacord.api.entity.message.embed.EmbedFooter

class JavacordMessageEmbedFooter(val footer: EmbedFooter): DiscordMessageEmbedFooter {
    override val text: String?
        get() = footer.text.orElse(null)
    override val iconUrl: String?
        get() = footer.iconUrl.orElse(null)?.toString()
}