package net.dungeonhub.wrapper.kord

import dev.kord.core.Kord
import dev.kord.core.entity.channel.GuildMessageChannel
import net.dungeonhub.transcripts.DiscordHtmlTranscripts
import net.dungeonhub.wrapper.DiscordChannel
import net.dungeonhub.wrapper.DiscordFramework
import java.awt.Color

class KordFramework(val kord: Kord): DiscordFramework

fun dev.kord.common.Color.toJavaColor(): Color {
    return Color(red, green, blue)
}

fun GuildMessageChannel.toTranscriptableChannel(): DiscordChannel<KordFramework> {
    return KordChannel(this)
}

fun GuildMessageChannel.createTranscript(): String {
    return DiscordHtmlTranscripts.createTranscript(toTranscriptableChannel())
}