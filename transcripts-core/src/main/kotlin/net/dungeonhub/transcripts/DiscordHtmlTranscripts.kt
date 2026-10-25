package net.dungeonhub.transcripts

import kotlinx.html.html
import kotlinx.html.stream.appendHTML
import net.dungeonhub.transcripts.components.transcript
import net.dungeonhub.wrapper.DiscordChannel
import net.dungeonhub.wrapper.DiscordFramework
import net.dungeonhub.wrapper.DiscordMessage
import java.io.IOException

/**
 * Created by Ryzeon
 * Modified by Taubsie, for Kotlin and greater Framework support
 * Project: discord-html-transcripts
 * Date: 2/12/21 @ 00:32
 * Twitter: @Ryzeon_ 😎
 * GitHub: https://github.ryzeon.me/
 * GitHub: https://github.dungeon-hub.net/
 */
object DiscordHtmlTranscripts {
    @Throws(IOException::class)
    fun <FW : DiscordFramework> createTranscript(channel: DiscordChannel<FW>): String {
        return generateFromMessages(channel, channel.messages)
    }

    @Throws(IOException::class)
    fun <FW : DiscordFramework> generateFromMessages(
        channel: DiscordChannel<FW>,
        messages: List<DiscordMessage>
    ): String {
        return buildString { writeTranscript(channel, messages, this) }
    }

    /**
     * Streams the transcript of [messages] into [output], without building the whole document in memory first.
     */
    @Throws(IOException::class)
    fun <FW : DiscordFramework> writeTranscript(
        channel: DiscordChannel<FW>,
        messages: List<DiscordMessage>,
        output: Appendable
    ) {
        output.append("<!DOCTYPE html>\n")
        output.appendHTML(prettyPrint = false).html {
            transcript(channel, messages.sortedBy { it.creationTime })
        }
    }
}
