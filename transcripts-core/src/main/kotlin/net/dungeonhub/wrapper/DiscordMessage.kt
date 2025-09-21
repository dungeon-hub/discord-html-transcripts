package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.Formatter
import org.jsoup.nodes.Element
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// TODO would message.actionRows also be interesting to have here?
interface DiscordMessage {
    val id: Long
    val content: String
    val embeds: List<DiscordMessageEmbed?>
    val attachments: List<DiscordMessageAttachment>
    val creationTime: Instant
    val reference: DiscordMessage?
    val author: DiscordMessageAuthor?
    val interaction: DiscordInteraction?

    fun transcriptify(): Element {
        // create message group
        val messageGroup = Element("div")
        messageGroup.addClass("chatlog__message-group")

        // message reference
        if (reference != null) {
            val referenceMessage = reference!!

            // create symbol
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
                            ) + "\" style=\"color: " + color + "\">" + author.displayName) + "</span>" +
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
            }

            messageGroup.appendChild(referenceSymbol)
            messageGroup.appendChild(reference)
        } else if(interaction != null) {
            val interaction = interaction!!

            // create symbol
            val referenceSymbol = Element("div")
            referenceSymbol.addClass("chatlog__reference-symbol")

            // create reference
            val reference = Element("div")
            reference.addClass("chatlog__reference")

            val applicationIcon = Element("svg")
            applicationIcon.addClass("chatlog__application-icon")

            val applicationIconUse = Element("use")
            applicationIconUse.attr("xlink:href", "#icon-application-command")

            applicationIcon.appendChild(applicationIconUse)

            val author = interaction.user
            val color = Formatter.toHex(author.roleColor)

            reference.html(
                (((("<img class=\"chatlog__reference-avatar\" src=\""
                        + author.avatar) + "\" alt=\"Avatar\" loading=\"lazy\">" +
                        "<span class=\"chatlog__reference-name\" title=\"" + author.name
                        ) + "\" style=\"color: " + color + "\">" + author.displayName) + "</span>" +
                        "<div class=\"chatlog__reference-content\">" +
                        " <span class=\"chatlog__reference-link\">" +
                        ("used <div class=\"chatlog__command_name\">$applicationIcon ${interaction.name}</div>") +
                        "</span>" +
                        "</div>")
            )

            messageGroup.appendChild(referenceSymbol)
            messageGroup.appendChild(reference)
        }

        val authorElement = Element("div")
        authorElement.addClass("chatlog__author-avatar-container")

        val authorAvatar = Element("img")
        authorAvatar.addClass("chatlog__author-avatar")
        authorAvatar.attr("alt", "Avatar")
        authorAvatar.attr("loading", "lazy")

        val authorName = Element("span")
        authorName.addClass("chatlog__author-name")

        if (author != null) {
            authorName.attr("title", author!!.name)
            authorName.text(author!!.displayName)
            authorName.attr("data-user-id", author!!.id.toString())
            authorAvatar.attr("src", author!!.avatar)
        } else if(interaction?.application != null) {
            val app = interaction?.application!!

            val avatarUrl = if (!app.avatar.startsWith("https://")) {
                "https://cdn.discordapp.com/avatars/${app.id}/${app.avatar}.${
                    if (app.avatar.startsWith("a_")) "gif" else "png"
                }"
            } else {
                app.avatar
            }

            authorName.attr("title", app.name)
            authorName.text(app.displayName)
            authorName.attr("data-user-id", app.id.toString())
            authorAvatar.attr("src", avatarUrl)
        } else {
            // Handle the case when author is null (e.g., when the message is from a bot)
            authorName.attr("title", "Bot")
            authorName.text("Bot")
            authorName.attr("data-user-id", "Bot")
            authorAvatar.attr("src", "default_bot_avatar_url") // replace with your default bot avatar URL
        }

        authorElement.appendChild(authorAvatar)
        messageGroup.appendChild(authorElement)

        // message content
        val content = Element("div")
        content.addClass("chatlog__messages")

        content.appendChild(authorName)

        if (author?.isBot == true) {
            val botTag = Element("span")
            botTag.addClass("chatlog__bot-tag").text("APP")
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
            "title",
            "Message sent: ${
                DateTimeFormatter
                    .ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(creationTime)
            }"
        )

        if (this.content.isNotBlank()) {
            val messageContentContent = Element("div")
            messageContentContent.addClass("chatlog__content")

            val messageContentContentMarkdown = Element("div")
            messageContentContentMarkdown.addClass("markdown")

            val messageContentContentMarkdownSpan = Element("span")
            messageContentContentMarkdownSpan.addClass("preserve-whitespace")
            messageContentContentMarkdownSpan.html(Formatter.format(this.content))

            messageContentContentMarkdown.appendChild(messageContentContentMarkdownSpan)
            messageContentContent.appendChild(messageContentContentMarkdown)
            messageContent.appendChild(messageContentContent)
        }

        // message attachments
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

        return messageGroup
    }
}