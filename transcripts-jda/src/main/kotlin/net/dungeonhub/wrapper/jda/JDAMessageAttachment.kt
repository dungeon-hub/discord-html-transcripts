package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordMessageAttachment
import net.dv8tion.jda.api.entities.Message.Attachment

class JDAMessageAttachment(val attachment: Attachment): DiscordMessageAttachment {
    override val fileName: String
        get() = attachment.fileName
    override val isImage: Boolean
        get() = attachment.isImage
    override val url: String
        get() = attachment.url
    override val size: Long
        get() = attachment.size.toLong()
}