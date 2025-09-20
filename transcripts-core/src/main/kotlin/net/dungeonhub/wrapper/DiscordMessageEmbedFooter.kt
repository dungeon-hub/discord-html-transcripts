package net.dungeonhub.wrapper

import org.jsoup.nodes.Element
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface DiscordMessageEmbedFooter {
    val text: String?
    val iconUrl: String?

    fun transcriptify(timestamp: Instant?): Element {
        val embedFooter = Element("div")
        embedFooter.addClass("chatlog__embed-footer")

        if (iconUrl != null) {
            val embedFooterIcon = Element("img")
            embedFooterIcon.addClass("chatlog__embed-footer-icon")
            embedFooterIcon.attr("src", iconUrl!!)
            embedFooterIcon.attr("alt", "Footer icon")
            embedFooterIcon.attr("loading", "lazy")

            embedFooter.appendChild(embedFooterIcon)
        }

        val embedFooterText = Element("span")
        embedFooterText.addClass("chatlog__embed-footer-text")

        embedFooterText.text(
            if (timestamp != null) {
                (text ?: "") + " • " + DateTimeFormatter.ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault()).format(timestamp)
            } else {
                text ?: ""
            }
        )

        embedFooter.appendChild(embedFooterText)

        return embedFooter
    }
}