package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordApplication
import net.dungeonhub.wrapper.DiscordInteraction
import net.dungeonhub.wrapper.DiscordMessageAuthor
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User

class JDAInteraction(val interaction: Message.Interaction, val applicationUser: User, val applicationMember: Member?) : DiscordInteraction {
    override val id: Long
        get() = interaction.idLong
    override val name: String
        get() = interaction.name
    override val user: DiscordMessageAuthor
        get() = JDAMessageAuthor(interaction.user, interaction.member)
    override val application: DiscordApplication
        get() = JDAApplication(applicationUser, applicationMember)
}