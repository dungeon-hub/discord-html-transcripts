package net.dungeonhub.wrapper

interface DiscordApplication : DiscordMessageAuthor {
    override val isBot: Boolean
        get() = true
}