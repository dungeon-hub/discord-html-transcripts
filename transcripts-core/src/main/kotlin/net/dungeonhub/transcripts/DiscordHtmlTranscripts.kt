package net.dungeonhub.transcripts

import net.dungeonhub.wrapper.DiscordChannel
import net.dungeonhub.wrapper.DiscordFramework
import net.dungeonhub.wrapper.DiscordMessage
import org.jsoup.Jsoup
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
        val htmlTemplate = javaClass.classLoader.getResourceAsStream("template.html")

        val document = Jsoup.parse(htmlTemplate!!, "UTF-8", "")
        document.outputSettings().indentAmount(0).prettyPrint(true)
        document.getElementsByClass("preamble__guild-icon").first()!!
            .attr("src", channel.server.icon ?: "") // set guild icon

        document.getElementById("transcriptTitle")!!
            .text("#" + channel.name + " | " + messages.size + " messages") // set title
        document.getElementById("guildname")!!.text(channel.server.name) // set guild name
        document.getElementById("ticketname")!!.text("#" + channel.name) // set channel name

        val chatLog = document.getElementById("chatlog")!! // chat log

        val zone = java.time.ZoneId.systemDefault()
        var lastDate: java.time.LocalDate? = null

        for (message in messages.sortedBy { it.creationTime }) {
            val msgDate = message.creationTime.atZone(zone).toLocalDate()
            if (msgDate != lastDate) {
                lastDate = msgDate
                val divider = org.jsoup.nodes.Element("div")
                divider.addClass("chatlog__date-divider")
                divider.text(msgDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy")))
                chatLog.appendChild(divider)
            }
            chatLog.appendChild(message.transcriptify(channel.server))
        }

        return document.outerHtml()
    }
}
