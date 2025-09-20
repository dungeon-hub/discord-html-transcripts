package net.dungeonhub.wrapper

import java.awt.Color

interface DiscordMessageAuthor {
    val id: Long
    val name: String
    val displayName: String
    val roleColor: Color
    val avatar: String
    val isBot: Boolean
}