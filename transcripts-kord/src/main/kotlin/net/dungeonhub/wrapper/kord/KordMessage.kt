package net.dungeonhub.wrapper.kord

import dev.kord.core.behavior.channel.asChannelOf
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.GuildMessageChannel
import kotlinx.coroutines.runBlocking
import net.dungeonhub.wrapper.DiscordMessage
import net.dungeonhub.wrapper.DiscordMessageAttachment
import net.dungeonhub.wrapper.DiscordMessageAuthor
import net.dungeonhub.wrapper.DiscordMessageEmbed
import java.time.Instant
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

class KordMessage(val message: Message, override val channel: KordChannel) : DiscordMessage {
    constructor(message: Message) : this(
        message,
        runBlocking { KordChannel(message.channel.asChannelOf<GuildMessageChannel>()) }
    )

    override val id: Long
        get() = message.id.value.toLong()
    override val content: String
        get() = message.content
    override val embeds: List<DiscordMessageEmbed?>
        get() = message.embeds.map { KordMessageEmbed(it) }
    override val attachments: List<DiscordMessageAttachment>
        get() = message.attachments.map { KordMessageAttachment(it) }
    @OptIn(ExperimentalTime::class)
    override val creationTime: Instant
        get() = message.timestamp.toJavaInstant()
    override val reference: DiscordMessage?
        get() = message.referencedMessage?.let { KordMessage(it, channel) }
    override val author: DiscordMessageAuthor?
        get() = message.author?.let {
            runBlocking {
                KordMessageAuthor(
                    it,
                    message.getAuthorAsMemberOrNull()
                )
            }
        }
    override val interaction: KordInteraction?
        get() = message.interaction?.let {
            runBlocking {
                KordInteraction(
                    message.getGuild(),
                    it,
                    message.data.author
                )
            }
        }
}