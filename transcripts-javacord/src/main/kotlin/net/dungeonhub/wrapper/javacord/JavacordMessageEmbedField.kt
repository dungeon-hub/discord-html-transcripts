package net.dungeonhub.wrapper.javacord

import net.dungeonhub.wrapper.DiscordMessageEmbedField
import org.javacord.api.entity.message.embed.EmbedField

class JavacordMessageEmbedField(val embedField: EmbedField): DiscordMessageEmbedField {
    override val name: String
        get() = embedField.name
    override val value: String
        get() = embedField.value
    override val isInline: Boolean
        get() = embedField.isInline
}