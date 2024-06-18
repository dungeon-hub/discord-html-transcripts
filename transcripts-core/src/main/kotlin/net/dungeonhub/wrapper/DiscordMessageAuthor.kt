package net.dungeonhub.wrapper

import org.jsoup.nodes.Element
import java.awt.Color

interface DiscordMessageAuthor {
    val id: Long
    val name: String
    val displayName: String
    val roleColor: Color
    val avatar: String
    val isBot: Boolean

    fun transcriptify(): Element {
        val authorElement = Element("div")
        authorElement.addClass("chatlog__author-avatar-container")

        val authorAvatar = Element("img")
        authorAvatar.addClass("chatlog__author-avatar")
        authorAvatar.attr("src", avatar)
        authorAvatar.attr("alt", "Avatar")
        authorAvatar.attr("loading", "lazy")

        authorElement.appendChild(authorAvatar)

        return authorElement
    }
}