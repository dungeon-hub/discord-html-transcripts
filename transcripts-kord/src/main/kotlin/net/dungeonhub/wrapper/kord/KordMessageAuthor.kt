package net.dungeonhub.wrapper.kord

import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.entity.effectiveName
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.runBlocking
import net.dungeonhub.wrapper.DiscordMessageAuthor
import java.awt.Color

class KordMessageAuthor(val user: User, val author: Member?) : DiscordMessageAuthor {
    override val id: Long
        get() = user.id.value.toLong()
    override val name: String
        get() = user.username
    override val displayName: String
        get() = author?.effectiveName ?: user.effectiveName
    override val roleColor: Color
        get() = runBlocking {
            author?.roles
                ?.filter { it.color.rgb != 0 }
                ?.reduce { role1, role2 -> if (role1.rawPosition > role2.rawPosition) role1 else role2 }
                ?.color?.toJavaColor()
                ?: Color.WHITE
        }
    override val avatar: String
        get() = user.avatar?.cdnUrl?.toUrl() ?: "https://cdn.discordapp.com/embed/avatars/${
            (user.id.value.toLong().shr(22)).mod(6)
        }.png"
    override val isBot: Boolean
        get() = user.isBot
}