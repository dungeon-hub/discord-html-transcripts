package net.dungeonhub.wrapper.javacord

import net.dungeonhub.wrapper.DiscordServer
import org.javacord.api.entity.server.Server

class JavacordServer(val discordServer: Server): DiscordServer {
    override val name: String
        get() = discordServer.name
    override val icon: String?
        get() = discordServer.icon.map { it.url.toString() }.orElse(null)
}