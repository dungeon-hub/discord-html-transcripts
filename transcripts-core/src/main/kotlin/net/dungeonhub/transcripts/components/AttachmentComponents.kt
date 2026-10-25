package net.dungeonhub.transcripts.components

import kotlinx.html.FlowContent
import kotlinx.html.a
import kotlinx.html.audio
import kotlinx.html.div
import kotlinx.html.img
import kotlinx.html.title
import kotlinx.html.video
import net.dungeonhub.transcripts.Formatter
import net.dungeonhub.transcripts.utils.format.impl.AudioFormat
import net.dungeonhub.transcripts.utils.format.impl.VideoFormat
import net.dungeonhub.wrapper.DiscordMessageAttachment

private val videoFormats = VideoFormat()
private val audioFormats = AudioFormat()

/**
 * An attachment of a message, rendered as image, video, audio or generic file download.
 */
fun FlowContent.attachment(attachment: DiscordMessageAttachment) {
    val extension = attachment.fileName.substringAfterLast('.', "").takeIf { it.isNotEmpty() }
    val description = "${attachment.fileName} (${Formatter.formatBytes(attachment.size)})"

    div("chatlog__attachment") {
        when {
            attachment.isImage -> a {
                img(alt = "Image attachment", src = attachment.url, classes = "chatlog__attachment-media") {
                    lazy()
                    title = "Image: $description"
                }
            }

            extension != null && videoFormats.isFormat(extension) ->
                video("chatlog__attachment-media") {
                    src = attachment.url
                    controls = true
                    attributes["alt"] = "Video attachment"
                    title = "Video: $description"
                }

            extension != null && audioFormats.isFormat(extension) ->
                audio("chatlog__attachment-media") {
                    src = attachment.url
                    controls = true
                    attributes["alt"] = "Audio attachment"
                    title = "Audio: $description"
                }

            else -> div("chatlog__attachment-generic") {
                icon("chatlog__attachment-generic-icon", "icon-attachment")
                div("chatlog__attachment-generic-name") {
                    a(href = attachment.url) { +attachment.fileName }
                }
                div("chatlog__attachment-generic-size") { +Formatter.formatBytes(attachment.size) }
            }
        }
    }
}
