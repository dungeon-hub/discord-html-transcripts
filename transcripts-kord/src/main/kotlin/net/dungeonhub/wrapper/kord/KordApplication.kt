package net.dungeonhub.wrapper.kord

import dev.kord.core.cache.data.UserData
import dev.kord.core.entity.Member
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.runBlocking
import net.dungeonhub.wrapper.DiscordApplication
import java.awt.Color

class KordApplication(val userData: UserData, val member: Member?) : DiscordApplication {
    override val id: Long
        get() = userData.id.value.toLong()
    override val name: String
        get() = userData.username
    override val displayName: String
        get() = member?.effectiveName ?: userData.globalName.value ?: userData.username
    override val roleColor: Color
        get() = runBlocking {
            member?.roles
                ?.filter { it.color.rgb != 0 }
                ?.reduce { role1, role2 -> if (role1.rawPosition > role2.rawPosition) role1 else role2 }
                ?.color?.toJavaColor()
                ?: Color.WHITE
        }
    override val avatar: String
        get() = userData.avatar ?: "https://cdn.discordapp.com/embed/avatars/${
            (userData.id.value.toLong().shr(22)).mod(6)
        }.png"
}