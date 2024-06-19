package net.dungeonhub.wrapper.jda

import net.dungeonhub.wrapper.DiscordMessageEmbedField
import net.dv8tion.jda.api.entities.MessageEmbed.Field

class JDAMessageEmbedField(val embedField: Field): DiscordMessageEmbedField {
    override val name: String
        get() = embedField.name!!
    override val value: String
        get() = embedField.value!!
    override val isInline: Boolean
        get() = embedField.isInline
}