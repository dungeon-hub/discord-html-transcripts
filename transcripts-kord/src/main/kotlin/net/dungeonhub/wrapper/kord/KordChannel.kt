package net.dungeonhub.wrapper.kord

import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import net.dungeonhub.wrapper.DiscordChannel
import net.dungeonhub.wrapper.DiscordMessage
import net.dungeonhub.wrapper.DiscordServer

class KordChannel(val channel: GuildMessageChannel) : DiscordChannel<KordFramework> {
    override val framework: KordFramework
        get() = KordFramework(channel.kord)
    override val name: String
        get() = channel.name
    override val server: DiscordServer
        get() = runBlocking { KordServer(channel.getGuild()) }
    override val messages: List<DiscordMessage>
        get() = runBlocking {
            channel.withStrategy(EntitySupplyStrategy.rest).messages.map { KordMessage(it) }.toList()
        }
}