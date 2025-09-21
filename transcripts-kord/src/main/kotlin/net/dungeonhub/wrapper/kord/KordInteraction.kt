package net.dungeonhub.wrapper.kord

import dev.kord.core.behavior.GuildBehavior
import dev.kord.core.cache.data.UserData
import dev.kord.core.entity.Message
import kotlinx.coroutines.runBlocking
import net.dungeonhub.wrapper.DiscordInteraction
import net.dungeonhub.wrapper.DiscordMessageAuthor

class KordInteraction(val guild: GuildBehavior, val interaction: Message.Interaction, val userData: UserData?) : DiscordInteraction {
    override val id: Long
        get() = interaction.id.value.toLong()
    override val name: String
        get() = interaction.name
    override val user: DiscordMessageAuthor
        get() = interaction.user.let {
            runBlocking {
                KordMessageAuthor(
                    it.asUser(),
                    it.asMemberOrNull(guild.id)
                )
            }
        }
    override val application: KordApplication?
        get() = userData?.let {
            runBlocking {
                KordApplication(
                    userData,
                    guild.getMemberOrNull(userData.id)
                )
            }
        }
}