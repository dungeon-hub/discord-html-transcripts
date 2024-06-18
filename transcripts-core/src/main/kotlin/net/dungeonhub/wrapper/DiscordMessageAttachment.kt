package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.Formatter
import org.jsoup.nodes.Element

private val videoFormats: List<String> = mutableListOf(
    "mp4", "webm", "mkv", "avi", "mov", "flv", "wmv", "mpg",
    "mpeg"
)
private val audioFormats: List<String> = mutableListOf("mp3", "wav", "ogg", "flac")

interface DiscordMessageAttachment {
    val fileName: String?
    val isImage: Boolean
    val url: String
    val size: Long

    fun transcriptify(): Element {
        val attachmentsDiv = Element("div")
        attachmentsDiv.addClass("chatlog__attachment")

        val attachmentType = fileName
            ?.let { if (it.contains(".")) it else null }
            ?.let { it.substring(it.lastIndexOf(".") + 1) }
        if (isImage) {
            val attachmentLink = Element("a")

            val attachmentImage = Element("img")
            attachmentImage.addClass("chatlog__attachment-media")
            attachmentImage.attr("src", url)
            attachmentImage.attr("alt", "Image attachment")
            attachmentImage.attr("loading", "lazy")
            attachmentImage.attr(
                "title",
                ("Image: $fileName${Formatter.formatBytes(size)}")
            )

            attachmentLink.appendChild(attachmentImage)
            attachmentsDiv.appendChild(attachmentLink)
        } else if (attachmentType != null && videoFormats.contains(attachmentType)) {
            val attachmentVideo = Element("video")
            attachmentVideo.addClass("chatlog__attachment-media")
            attachmentVideo.attr("src", url)
            attachmentVideo.attr("alt", "Video attachment")
            attachmentVideo.attr("controls", true)
            attachmentVideo.attr(
                "title",
                ("Video: $fileName${Formatter.formatBytes(size)}")
            )

            attachmentsDiv.appendChild(attachmentVideo)
        } else if (attachmentType != null && audioFormats.contains(attachmentType)) {
            val attachmentAudio = Element("audio")
            attachmentAudio.addClass("chatlog__attachment-media")
            attachmentAudio.attr("src", url)
            attachmentAudio.attr("alt", "Audio attachment")
            attachmentAudio.attr("controls", true)
            attachmentAudio.attr(
                "title",
                ("Audio: $fileName${Formatter.formatBytes(size)}")
            )

            attachmentsDiv.appendChild(attachmentAudio)
        } else {
            val attachmentGeneric = Element("div")
            attachmentGeneric.addClass("chatlog__attachment-generic")

            val attachmentGenericIcon = Element("svg")
            attachmentGenericIcon.addClass("chatlog__attachment-generic-icon")

            val attachmentGenericIconUse = Element("use")
            attachmentGenericIconUse.attr("xlink:href", "#icon-attachment")

            attachmentGenericIcon.appendChild(attachmentGenericIconUse)
            attachmentGeneric.appendChild(attachmentGenericIcon)

            val attachmentGenericName = Element("div")
            attachmentGenericName.addClass("chatlog__attachment-generic-name")

            val attachmentGenericNameLink = Element("a")
            attachmentGenericNameLink.attr("href", url)
            attachmentGenericNameLink.text(fileName ?: "Unknown name")

            attachmentGenericName.appendChild(attachmentGenericNameLink)
            attachmentGeneric.appendChild(attachmentGenericName)

            val attachmentGenericSize = Element("div")
            attachmentGenericSize.addClass("chatlog__attachment-generic-size")

            attachmentGenericSize.text(Formatter.formatBytes(size))
            attachmentGeneric.appendChild(attachmentGenericSize)

            attachmentsDiv.appendChild(attachmentGeneric)
        }

        return attachmentsDiv
    }
}