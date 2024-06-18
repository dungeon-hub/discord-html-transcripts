package net.dungeonhub.wrapper

import org.jsoup.nodes.Element

interface DiscordMessageEmbedAuthor {
    val name: String?
    val iconUrl: String?
    val url: String?

    fun transcriptify(): Element {
        val embedAuthor = Element("div")
        embedAuthor.addClass("chatlog__embed-author")

        if (iconUrl != null) {
            val embedAuthorIcon = Element("img")
            embedAuthorIcon.addClass("chatlog__embed-author-icon")
            embedAuthorIcon.attr(
                "src",
                iconUrl!!
            )
            embedAuthorIcon.attr("alt", "Author icon")
            embedAuthorIcon.attr("loading", "lazy")

            embedAuthor.appendChild(embedAuthorIcon)
        }

        val embedAuthorName = Element("span")
        embedAuthorName.addClass("chatlog__embed-author-name")

        if (url != null) {
            val embedAuthorNameLink = Element("a")
            embedAuthorNameLink.addClass("chatlog__embed-author-name-link")
            embedAuthorNameLink.attr(
                "href",
                url!!
            )
            embedAuthorNameLink.text(name!!)

            embedAuthorName.appendChild(embedAuthorNameLink)
        } else {
            embedAuthorName.text(name!!)
        }

        embedAuthor.appendChild(embedAuthorName)

        return embedAuthor
    }
}