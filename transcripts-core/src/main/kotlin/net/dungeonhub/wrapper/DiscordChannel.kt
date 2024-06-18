package net.dungeonhub.wrapper

interface DiscordChannel<FW: DiscordFramework> {
    val framework: FW
    val name: String
    val server: DiscordServer
    val messages: List<DiscordMessage>
}