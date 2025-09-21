package net.dungeonhub.wrapper

interface DiscordInteraction {
    val id: Long
    val name: String
    val user: DiscordMessageAuthor
    val application: DiscordApplication?
}