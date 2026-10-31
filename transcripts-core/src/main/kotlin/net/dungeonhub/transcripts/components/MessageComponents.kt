package net.dungeonhub.transcripts.components

import kotlinx.html.FlowContent
import kotlinx.html.div
import kotlinx.html.em
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.onClick
import kotlinx.html.span
import kotlinx.html.style
import kotlinx.html.title
import net.dungeonhub.transcripts.Formatter
import net.dungeonhub.wrapper.DiscordApplication
import net.dungeonhub.wrapper.DiscordInteraction
import net.dungeonhub.wrapper.DiscordMessage
import net.dungeonhub.wrapper.DiscordMessageAuthor

private const val REFERENCE_PREVIEW_LENGTH = 42

/**
 * The user (or application) a message is displayed as sent by.
 */
private data class Sender(val id: String, val name: String, val displayName: String, val avatar: String)

private val DiscordMessage.sender: Sender
    get() {
        author?.let { return Sender(it.id.toString(), it.name, it.displayName, it.avatar) }
        interaction?.application?.let { return Sender(it.id.toString(), it.name, it.displayName, it.avatarUrl) }
        // e.g. webhook or system messages without an author
        return Sender("Bot", "Bot", "Bot", "default_bot_avatar_url")
    }

private val DiscordApplication.avatarUrl: String
    get() = if (avatar.startsWith("https://")) {
        avatar
    } else {
        "https://cdn.discordapp.com/avatars/$id/$avatar.${if (avatar.startsWith("a_")) "gif" else "png"}"
    }

/**
 * A complete message: optional reply or command reference, author, timestamp, content, attachments and embeds.
 */
fun FlowContent.message(message: DiscordMessage) {
    div("chatlog__message-group") {
        val reference = message.reference
        val interaction = message.interaction
        if (reference != null) {
            replyReference(reference)
        } else if (interaction != null) {
            commandReference(interaction)
        }

        val sender = message.sender

        div("chatlog__author-avatar-container") {
            img(alt = "Avatar", src = sender.avatar, classes = "chatlog__author-avatar") { lazy() }
        }

        div("chatlog__messages") {
            span("chatlog__author-name") {
                title = sender.name
                attributes["data-user-id"] = sender.id
                +sender.displayName
            }

            if (message.author?.isBot == true) {
                span("chatlog__bot-tag") { +"APP" }
            }

            span("chatlog__timestamp") { +message.creationTime.toShortTime() }

            div("chatlog__message") {
                id = "message-${message.id}"
                title = "Message sent: ${message.creationTime.toLongTime()}"
                attributes["data-message-id"] = message.id.toString()

                if (message.content.isNotBlank()) {
                    div("chatlog__content") {
                        div("markdown") {
                            span("preserve-whitespace") { markdownHtml(message.content) }
                        }
                    }
                }

                message.attachments.forEach { attachment(it) }
            }

            message.embeds.filterNotNull().forEach { embed(it) }
        }
    }
}

/**
 * The preview of the message that [referencedMessage] was replied to, shown above a reply.
 */
fun FlowContent.replyReference(referencedMessage: DiscordMessage) {
    div("chatlog__reference-symbol")
    div("chatlog__reference") {
        val author = referencedMessage.author ?: return@div

        referenceAuthor(author)
        div("chatlog__reference-content") {
            span("chatlog__reference-link") {
                onClick = "scrollToMessage(event, '${referencedMessage.id}')"
                em { +referencedMessage.contentPreview() }
            }
        }
    }
}

/**
 * The "used /command" line shown above the response to an application command.
 */
fun FlowContent.commandReference(interaction: DiscordInteraction) {
    div("chatlog__reference-symbol")
    div("chatlog__reference") {
        referenceAuthor(interaction.user)
        div("chatlog__reference-content") {
            span("chatlog__reference-link") {
                +"used "
                span("chatlog__command_name") {
                    icon("chatlog__application-icon", "icon-application-command")
                    +" ${interaction.name}"
                }
            }
        }
    }
}

private fun FlowContent.referenceAuthor(author: DiscordMessageAuthor) {
    img(alt = "Avatar", src = author.avatar, classes = "chatlog__reference-avatar") { lazy() }
    span("chatlog__reference-name") {
        title = author.name
        style = "color: #${Formatter.toHex(author.roleColor)}"
        +author.displayName
    }
}

private fun DiscordMessage.contentPreview(): String = when {
    content.isBlank() -> "Click to see attachment"
    content.length > REFERENCE_PREVIEW_LENGTH -> content.substring(0, REFERENCE_PREVIEW_LENGTH) + "..."
    else -> content
}
