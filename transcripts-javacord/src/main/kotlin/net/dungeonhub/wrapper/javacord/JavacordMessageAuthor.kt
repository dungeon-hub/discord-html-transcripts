package net.dungeonhub.wrapper.javacord

import net.dungeonhub.wrapper.DiscordMessageAuthor
import org.javacord.api.entity.message.MessageAuthor
import java.awt.Color

class JavacordMessageAuthor(val author: MessageAuthor): DiscordMessageAuthor {
    override val id: Long
        get() = author.id
    override val name: String
        get() = author.name
    override val displayName: String
        get() = author.displayName
    override val roleColor: Color
        get() = author.roleColor.orElse(Color.WHITE)
    override val avatar: String
        get() = author.avatar.url.toString()
    override val isBot: Boolean
        get() = author.isBotUser
}