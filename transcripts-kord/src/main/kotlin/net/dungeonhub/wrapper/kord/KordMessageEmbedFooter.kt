package net.dungeonhub.wrapper.kord

import dev.kord.core.entity.Embed
import net.dungeonhub.wrapper.DiscordMessageEmbedFooter

class KordMessageEmbedFooter(val footer: Embed.Footer): DiscordMessageEmbedFooter {
    override val text: String
        get() = footer.text
    override val iconUrl: String?
        get() = footer.iconUrl
}