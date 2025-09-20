package net.dungeonhub.wrapper.javacord

import net.dungeonhub.wrapper.DiscordMessageAttachment
import org.javacord.api.entity.message.MessageAttachment

class JavacordMessageAttachment(val attachment: MessageAttachment): DiscordMessageAttachment {
    override val fileName: String
        get() = attachment.fileName
    override val isImage: Boolean
        get() = attachment.isImage
    override val url: String
        get() = attachment.url.toString()
    override val size: Long
        get() = attachment.size.toLong()
}