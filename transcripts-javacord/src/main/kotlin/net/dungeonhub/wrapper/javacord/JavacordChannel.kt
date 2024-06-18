package net.dungeonhub.wrapper.javacord

import net.dungeonhub.wrapper.DiscordChannel
import net.dungeonhub.wrapper.DiscordMessage
import net.dungeonhub.wrapper.DiscordServer
import org.javacord.api.entity.channel.ServerTextChannel

class JavacordChannel(val channel: ServerTextChannel): DiscordChannel<JavacordFramework> {
    override val framework: JavacordFramework
        get() = JavacordFramework(channel.api)
    override val name: String
        get() = channel.name
    override val server: DiscordServer
        get() = JavacordServer(channel.server)
    override val messages: List<DiscordMessage>
        get() = channel.messagesAsStream.map { JavacordMessage(it) }.toList()
}