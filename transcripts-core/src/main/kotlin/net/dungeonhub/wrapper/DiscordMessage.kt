package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.Formatter
import org.jsoup.nodes.Element
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

interface DiscordMessage {
    val id: Long
    val content: String
    val embeds: List<DiscordMessageEmbed?>
    val attachments: List<DiscordMessageAttachment>
    val creationTime: Instant
    val reference: DiscordMessage?
    val author: DiscordMessageAuthor?

    fun transcriptify(): Element {
        val messageGroup = Element("div")
        messageGroup.addClass("chatlog__message-group")

        if (reference != null) {
            val referenceMessage: DiscordMessage = reference!!

            val referenceSymbol = Element("div")
            referenceSymbol.addClass("chatlog__reference-symbol")

            // create reference
            val reference = Element("div")
            reference.addClass("chatlog__reference")

            val author = referenceMessage.author
            if (author != null) {
                val color = Formatter.toHex(author.roleColor)

                reference.html(
                    ((((("<img class=\"chatlog__reference-avatar\" src=\""
                            + author.avatar) + "\" alt=\"Avatar\" loading=\"lazy\">" +
                            "<span class=\"chatlog__reference-name\" title=\"" + author.name
                            ) + "\" style=\"color: " + color + "\">" + author.name) + "\"</span>" +
                            "<div class=\"chatlog__reference-content\">" +
                            " <span class=\"chatlog__reference-link\" onclick=\"scrollToMessage(event, '"
                            + referenceMessage.id) + "')\">" +
                            "<em>" +
                            (if (referenceMessage.content.isNotBlank())
                                if (referenceMessage.content.length > 42)
                                    referenceMessage.content.substring(0, 42)
                                            + "..."
                                else
                                    referenceMessage.content
                            else
                                "Click to see attachment") +
                            "</em>" +
                            "</span>" +
                            "</div>")
                )

                messageGroup.appendChild(referenceSymbol)
                messageGroup.appendChild(reference)
            }
        }

        if (author != null) {
            messageGroup.appendChild(author!!.transcriptify())

            val content = Element("div")
            content.addClass("chatlog__messages")
            val authorName: Element = Element("span")
            authorName.addClass("chatlog__author-name")
            authorName.attr("title", author!!.displayName)
            authorName.text(author!!.name)
            authorName.attr("data-user-id", author!!.id.toString())
            content.appendChild(authorName)

            if (author!!.isBot) {
                val botTag = Element("span")
                botTag.addClass("chatlog__bot-tag").text("BOT")
                content.appendChild(botTag)
            }

            // timestamp
            val timestamp = Element("span")
            timestamp.addClass("chatlog__timestamp")

            timestamp.text(
                DateTimeFormatter
                    .ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(creationTime)
            )

            content.appendChild(timestamp)

            val messageContent = Element("div")
            messageContent.addClass("chatlog__message")
            messageContent.attr("data-message-id", id.toString())
            messageContent.attr("id", "message-$id")
            messageContent.attr(
                "title", "Message sent: "
                        + DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault())
                    .format(creationTime)
            )

            if (this.content.isNotBlank()) {
                val messageContentContent = Element("div")
                messageContentContent.addClass("chatlog__content")

                val messageContentContentMarkdown = Element("div")
                messageContentContentMarkdown.addClass("markdown")

                val messageContentContentMarkdownSpan = Element("span")
                messageContentContentMarkdownSpan.addClass("preserve-whitespace")
                messageContentContentMarkdownSpan
                    .html(Formatter.format(this.content))

                messageContentContentMarkdown.appendChild(messageContentContentMarkdownSpan)
                messageContentContent.appendChild(messageContentContentMarkdown)
                messageContent.appendChild(messageContentContent)
            }

            // messsage attachments
            if (attachments.isNotEmpty()) {
                for (attachment in attachments) {
                    messageContent.appendChild(attachment.transcriptify())
                }
            }

            content.appendChild(messageContent)

            if (embeds.isNotEmpty()) {
                for (embed in embeds) {
                    if (embed == null) {
                        continue
                    }

                    content.appendChild(embed.transcriptify())
                }
            }

            messageGroup.appendChild(content)
        }

        return messageGroup
    }
}