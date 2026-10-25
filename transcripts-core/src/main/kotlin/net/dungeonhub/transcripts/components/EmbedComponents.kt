package net.dungeonhub.transcripts.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.span
import kotlinx.html.style
import net.dungeonhub.transcripts.Formatter
import net.dungeonhub.wrapper.DiscordMessageEmbed
import net.dungeonhub.wrapper.DiscordMessageEmbedAuthor
import net.dungeonhub.wrapper.DiscordMessageEmbedField
import net.dungeonhub.wrapper.DiscordMessageEmbedFooter
import java.time.Instant

/**
 * A rich embed attached to a message.
 */
fun FlowContent.embed(embed: DiscordMessageEmbed) {
    div("chatlog__embed") {
        val color = embed.color
        if (color == null) {
            div("chatlog__embed-color-pill chatlog__embed-color-pill--default")
        } else {
            div("chatlog__embed-color-pill") { style = "background-color: #${Formatter.toHex(color)}" }
        }

        div("chatlog__embed-content-container") {
            div("chatlog__embed-content") {
                div("chatlog__embed-text") {
                    embed.author?.let { if (it.name != null) embedAuthor(it) }
                    embed.title?.let { embedTitle(it, embed.url) }
                    embed.description?.let { description ->
                        div("chatlog__embed-description") { markdown(description) }
                    }
                    if (embed.fields.isNotEmpty()) {
                        div("chatlog__embed-fields") {
                            embed.fields.forEach { embedField(it) }
                        }
                    }
                }

                embed.thumbnail?.let { thumbnail ->
                    div("chatlog__embed-thumbnail-container") {
                        a(href = thumbnail, classes = "chatlog__embed-thumbnail-link") {
                            img(alt = "Thumbnail", src = thumbnail, classes = "chatlog__embed-thumbnail") { lazy() }
                        }
                    }
                }
            }

            embed.image?.let { image ->
                div("chatlog__embed-image-container") {
                    a(href = image, classes = "chatlog__embed-image-link") {
                        img(alt = "Image", src = image, classes = "chatlog__embed-image") { lazy() }
                    }
                }
            }

            embed.footer?.let { embedFooter(it, embed.timestamp) }
        }
    }
}

private fun FlowContent.embedTitle(title: String, url: String?) {
    div("chatlog__embed-title") {
        if (url != null) {
            a(href = url, classes = "chatlog__embed-title-link") { markdown(title) }
        } else {
            markdown(title)
        }
    }
}

/**
 * The author line at the top of an embed.
 */
fun FlowContent.embedAuthor(author: DiscordMessageEmbedAuthor) {
    div("chatlog__embed-author") {
        author.iconUrl?.let { iconUrl ->
            img(alt = "Author icon", src = iconUrl, classes = "chatlog__embed-author-icon") { lazy() }
        }

        span("chatlog__embed-author-name") {
            val url = author.url
            if (url != null) {
                a(href = url, classes = "chatlog__embed-author-name-link") { +author.name.orEmpty() }
            } else {
                +author.name.orEmpty()
            }
        }
    }
}

/**
 * A single name/value field of an embed.
 */
fun FlowContent.embedField(field: DiscordMessageEmbedField) {
    div(if (field.isInline) "chatlog__embed-field--inline" else "chatlog__embed-field") {
        div("chatlog__embed-field-name") { markdown(field.name) }
        div("chatlog__embed-field-value") { markdown(field.value) }
    }
}

/**
 * The footer of an embed, followed by the embed's [timestamp] if it has one.
 */
fun FlowContent.embedFooter(footer: DiscordMessageEmbedFooter, timestamp: Instant?) {
    div("chatlog__embed-footer") {
        footer.iconUrl?.let { iconUrl ->
            img(alt = "Footer icon", src = iconUrl, classes = "chatlog__embed-footer-icon") { lazy() }
        }

        span("chatlog__embed-footer-text") {
            val text = footer.text.orEmpty()
            +(if (timestamp != null) "$text • ${timestamp.toLongTime()}" else text)
        }
    }
}
