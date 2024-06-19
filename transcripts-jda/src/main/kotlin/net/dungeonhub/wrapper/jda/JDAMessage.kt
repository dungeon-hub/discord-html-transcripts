package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordMessage
import net.dungeonhub.wrapper.DiscordMessageAttachment
import net.dungeonhub.wrapper.DiscordMessageAuthor
import net.dungeonhub.wrapper.DiscordMessageEmbed
import net.dv8tion.jda.api.entities.Message
import java.time.Instant

class JDAMessage(val message: Message): DiscordMessage {
    override val id: Long
        get() = message.idLong
    override val content: String
        get() = message.contentDisplay
    override val embeds: List<DiscordMessageEmbed?>
        get() = message.embeds.map { JDAMessageEmbed(it) }
    override val attachments: List<DiscordMessageAttachment>
        get() = message.attachments.map { JDAMessageAttachment(it) }
    override val creationTime: Instant
        get() = message.timeCreated.toInstant()
    override val reference: DiscordMessage?
        get() = message.referencedMessage?.let { JDAMessage(it) }
    override val author: DiscordMessageAuthor
        get() = JDAMessageAuthor(message.author, message.member)
}