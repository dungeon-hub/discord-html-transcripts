package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordMessageEmbedFooter
import net.dv8tion.jda.api.entities.MessageEmbed.Footer

class JDAMessageEmbedFooter(val footer: Footer): DiscordMessageEmbedFooter {
    override val text: String?
        get() = footer.text
    override val iconUrl: String?
        get() = footer.iconUrl
}