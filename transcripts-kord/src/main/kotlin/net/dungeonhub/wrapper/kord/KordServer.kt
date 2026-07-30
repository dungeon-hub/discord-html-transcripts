package net.dungeonhub.wrapper.kord

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import kotlinx.coroutines.runBlocking
import net.dungeonhub.wrapper.DiscordServer

class KordServer(val guild: Guild): DiscordServer {
    override val name: String
        get() = guild.name
    override val icon: String?
        get() = guild.icon?.cdnUrl?.toUrl()

    // ponytail: runBlocking — same pattern as the rest of this wrapper; upgrade path is suspendable transcript generation.
    override fun getMemberName(id: Long): String? = runBlocking { guild.getMemberOrNull(Snowflake(id))?.effectiveName }
    override fun getRoleName(id: Long): String? = runBlocking { guild.getRoleOrNull(Snowflake(id))?.name }
    override fun getChannelName(id: Long): String? = runBlocking { guild.getChannelOrNull(Snowflake(id))?.name }
}