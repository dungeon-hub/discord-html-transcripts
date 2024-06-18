package net.dungeonhub.wrapper.kord

import dev.kord.core.entity.Attachment
import net.dungeonhub.wrapper.DiscordMessageAttachment

class KordMessageAttachment(val attachment: Attachment): DiscordMessageAttachment {
    override val fileName: String
        get() = attachment.filename
    override val isImage: Boolean
        get() = attachment.isImage
    override val url: String
        get() = attachment.url
    override val size: Long
        get() = attachment.size.toLong()
}