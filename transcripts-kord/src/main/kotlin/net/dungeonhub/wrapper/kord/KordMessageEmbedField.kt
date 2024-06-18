package net.dungeonhub.wrapper.kord

import dev.kord.core.entity.Embed
import net.dungeonhub.wrapper.DiscordMessageEmbedField

class KordMessageEmbedField(val embedField: Embed.Field): DiscordMessageEmbedField {
    override val name: String
        get() = embedField.name
    override val value: String
        get() = embedField.value
    override val isInline: Boolean
        get() = embedField.inline ?: false
}