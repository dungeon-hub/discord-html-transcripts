package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordChannel
import net.dungeonhub.wrapper.DiscordMessage
import net.dungeonhub.wrapper.DiscordServer
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel

class JDAChannel(val channel: GuildMessageChannel): DiscordChannel<JDAFramework> {
    override val framework: JDAFramework
        get() = JDAFramework(channel.jda)
    override val name: String
        get() = channel.name
    override val server: DiscordServer
        get() = JDAServer(channel.guild)
    override val messages: List<DiscordMessage>
        get() = channel.iterableHistory.stream()
            .map { JDAMessage(it) }
            .toList()
}