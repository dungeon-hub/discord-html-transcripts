package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordMessageAuthor
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import java.awt.Color

open class JDAMessageAuthor(val user: User, val member: Member?): DiscordMessageAuthor {
    override val id: Long
        get() = user.idLong
    override val name: String
        get() = user.name
    override val displayName: String
        get() = member?.effectiveName ?: user.effectiveName
    override val roleColor: Color
        get() = member?.color ?: Color.WHITE
    override val avatar: String
        get() = user.effectiveAvatarUrl
    override val isBot: Boolean
        get() = user.isBot
}