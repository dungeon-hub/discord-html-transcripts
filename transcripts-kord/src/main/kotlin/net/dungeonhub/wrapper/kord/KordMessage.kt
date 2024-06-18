package net.dungeonhub.wrapper.kord

import dev.kord.core.entity.Message
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.toJavaInstant
import net.dungeonhub.wrapper.DiscordMessage
import net.dungeonhub.wrapper.DiscordMessageAttachment
import net.dungeonhub.wrapper.DiscordMessageAuthor
import net.dungeonhub.wrapper.DiscordMessageEmbed
import java.time.Instant

class KordMessage(val message: Message) : DiscordMessage {
    override val id: Long
        get() = message.id.value.toLong()
    override val content: String
        get() = message.content
    override val embeds: List<DiscordMessageEmbed?>
        get() = message.embeds.map { KordMessageEmbed(it) }
    override val attachments: List<DiscordMessageAttachment>
        get() = message.attachments.map { KordMessageAttachment(it) }
    override val creationTime: Instant
        get() = message.timestamp.toJavaInstant()
    override val reference: DiscordMessage?
        get() = message.referencedMessage?.let { KordMessage(it) }
    override val author: DiscordMessageAuthor?
        get() = message.author?.let { runBlocking {
            net.dungeonhub.wrapper.kord.KordMessageAuthor(
                it,
                message.getAuthorAsMemberOrNull()
            )
        } }
}