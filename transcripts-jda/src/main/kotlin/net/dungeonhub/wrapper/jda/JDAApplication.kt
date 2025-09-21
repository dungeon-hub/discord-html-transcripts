package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordApplication
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User

class JDAApplication(user: User, member: Member?) : JDAMessageAuthor(user, member), DiscordApplication {
    override val isBot: Boolean
        get() = true
}