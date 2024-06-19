package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordServer
import net.dv8tion.jda.api.entities.Guild

class JDAServer(val guild: Guild): DiscordServer {
    override val name: String
        get() = guild.name
    override val icon: String?
        get() = guild.iconUrl
}