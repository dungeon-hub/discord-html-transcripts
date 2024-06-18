package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.Formatter
import org.jsoup.nodes.Element
import java.awt.Color
import java.time.Instant

interface DiscordMessageEmbed {
    val color: Color?
    val title: String?
    val description: String?
    val fields: List<DiscordMessageEmbedField>
    val url: String?
    val thumbnail: String?
    val image: String?
    val footer: DiscordMessageEmbedFooter?
    val author: DiscordMessageEmbedAuthor?
    val timestamp: Instant?

    fun transcriptify(): Element {
        val embedDiv = Element("div")
        embedDiv.addClass("chatlog__embed")

        // embed color
        if (color != null) {
            val embedColorPill = Element("div")
            embedColorPill.addClass("chatlog__embed-color-pill")
            embedColorPill.attr(
                "style",
                "background-color: #" + Formatter.toHex(color!!)
            )

            embedDiv.appendChild(embedColorPill)
        }

        val embedContentContainer = Element("div")
        embedContentContainer.addClass("chatlog__embed-content-container")

        val embedContent = Element("div")
        embedContent.addClass("chatlog__embed-content")

        val embedText = Element("div")
        embedText.addClass("chatlog__embed-text")

        // embed author
        if (author != null && author?.name != null) {
            embedText.appendChild(author!!.transcriptify())
        }

        // embed title
        if (title != null) {
            val embedTitle = Element("div")
            embedTitle.addClass("chatlog__embed-title")

            if (url != null) {
                val embedTitleLink = Element("a")
                embedTitleLink.addClass("chatlog__embed-title-link")
                embedTitleLink.attr("href", url!!)

                val embedTitleMarkdown = Element("div")
                embedTitleMarkdown.addClass("markdown preserve-whitespace")
                    .html(Formatter.format(title!!))

                embedTitleLink.appendChild(embedTitleMarkdown)
                embedTitle.appendChild(embedTitleLink)
            } else {
                val embedTitleMarkdown = Element("div")
                embedTitleMarkdown.addClass("markdown preserve-whitespace")
                    .html(Formatter.format(title!!))

                embedTitle.appendChild(embedTitleMarkdown)
            }

            embedText.appendChild(embedTitle)
        }

        // embed description
        if (description != null) {
            val embedDescription = Element("div")
            embedDescription.addClass("chatlog__embed-description")

            val embedDescriptionMarkdown = Element("div")
            embedDescriptionMarkdown.addClass("markdown preserve-whitespace")
            embedDescriptionMarkdown
                .html(Formatter.format(description ?: ""))

            embedDescription.appendChild(embedDescriptionMarkdown)
            embedText.appendChild(embedDescription)
        }

        // embed fields
        if (fields.isNotEmpty()) {
            val embedFields = Element("div")
            embedFields.addClass("chatlog__embed-fields")

            for (field in fields) {
                embedFields.appendChild(field.transcriptify())
            }

            embedText.appendChild(embedFields)
        }

        embedContent.appendChild(embedText)

        // embed thumbnail
        if (thumbnail != null) {
            val embedThumbnail = Element("div")
            embedThumbnail.addClass("chatlog__embed-thumbnail-container")

            val embedThumbnailLink = Element("a")
            embedThumbnailLink.addClass("chatlog__embed-thumbnail-link")
            embedThumbnailLink.attr("href", thumbnail!!)

            val embedThumbnailImage = Element("img")
            embedThumbnailImage.addClass("chatlog__embed-thumbnail")
            embedThumbnailImage.attr("src", thumbnail!!)
            embedThumbnailImage.attr("alt", "Thumbnail")
            embedThumbnailImage.attr("loading", "lazy")

            embedThumbnailLink.appendChild(embedThumbnailImage)
            embedThumbnail.appendChild(embedThumbnailLink)

            embedContent.appendChild(embedThumbnail)
        }

        embedContentContainer.appendChild(embedContent)

        // embed image
        if (image != null) {
            val embedImage = Element("div")
            embedImage.addClass("chatlog__embed-image-container")

            val embedImageLink = Element("a")
            embedImageLink.addClass("chatlog__embed-image-link")
            embedImageLink.attr("href", image!!)

            val embedImageImage = Element("img")
            embedImageImage.addClass("chatlog__embed-image")
            embedImageImage.attr("src", image!!)
            embedImageImage.attr("alt", "Image")
            embedImageImage.attr("loading", "lazy")

            embedImageLink.appendChild(embedImageImage)
            embedImage.appendChild(embedImageLink)

            embedContentContainer.appendChild(embedImage)
        }

        // embed footer
        if (footer != null) {
            embedContentContainer.appendChild(footer!!.transcriptify(timestamp))
        }

        embedDiv.appendChild(embedContentContainer)

        return embedDiv
    }
}