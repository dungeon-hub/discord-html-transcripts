package net.dungeonhub.wrapper.kord

import dev.kord.core.entity.Guild
import net.dungeonhub.wrapper.DiscordServer

class KordServer(val guild: Guild): DiscordServer {
    override val name: String
        get() = guild.name
    override val icon: String?
        get() = guild.icon?.cdnUrl?.toUrl()
}