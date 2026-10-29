package net.dungeonhub.wrapper.kord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import net.dungeonhub.wrapper.DiscordServer

class KordServer(val guild: Guild): DiscordServer {
    override val name: String
        get() = guild.name
    override val icon: String?
        get() = guild.icon?.cdnUrl?.toUrl()

    override suspend fun getMemberName(id: Long): String? = guild.getMemberOrNull(Snowflake(id))?.effectiveName
    override suspend fun getRoleName(id: Long): String? = guild.getRoleOrNull(Snowflake(id))?.name
    override suspend fun getChannelName(id: Long): String? = guild.getChannelOrNull(Snowflake(id))?.name
}