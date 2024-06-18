package net.dungeonhub.wrapper.javacord

import net.dungeonhub.wrapper.DiscordMessage
import net.dungeonhub.wrapper.DiscordMessageAttachment
import net.dungeonhub.wrapper.DiscordMessageAuthor
import net.dungeonhub.wrapper.DiscordMessageEmbed
import org.javacord.api.entity.message.Message
import java.time.Instant

class JavacordMessage(val message: Message): DiscordMessage {
    override val id: Long
        get() = message.id
    override val content: String
        get() = message.content
    override val embeds: List<DiscordMessageEmbed?>
        get() = message.embeds.map { JavacordMessageEmbed(it) }
    override val attachments: List<DiscordMessageAttachment>
        get() = message.attachments.map { JavacordMessageAttachment(it) }
    override val creationTime: Instant
        get() = message.creationTimestamp
    override val reference: DiscordMessage?
        get() = message.messageReference.flatMap { it.message }.map { JavacordMessage(it) }.orElse(null)
    override val author: DiscordMessageAuthor
        get() = JavacordMessageAuthor(message.author)
}