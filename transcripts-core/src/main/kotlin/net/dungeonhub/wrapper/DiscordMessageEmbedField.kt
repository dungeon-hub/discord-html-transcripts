package net.dungeonhub.wrapper

import net.dungeonhub.transcripts.Formatter
import org.jsoup.nodes.Element

interface DiscordMessageEmbedField {
    val name: String
    val value: String
    val isInline: Boolean

    fun transcriptify(): Element {
        val embedField = Element("div")
        embedField.addClass(
            if (isInline)
                "chatlog__embed-field-inline"
            else
                "chatlog__embed-field"
        )

        // Field nmae
        val embedFieldName = Element("div")
        embedFieldName.addClass("chatlog__embed-field-name")

        val embedFieldNameMarkdown = Element("div")
        embedFieldNameMarkdown.addClass("markdown preserve-whitespace")
        embedFieldNameMarkdown.html(name)

        embedFieldName.appendChild(embedFieldNameMarkdown)
        embedField.appendChild(embedFieldName)


        // Field value
        val embedFieldValue = Element("div")
        embedFieldValue.addClass("chatlog__embed-field-value")

        val embedFieldValueMarkdown = Element("div")
        embedFieldValueMarkdown.addClass("markdown preserve-whitespace")
        embedFieldValueMarkdown
            .html(Formatter.format(value))

        embedFieldValue.appendChild(embedFieldValueMarkdown)
        embedField.appendChild(embedFieldValue)

        return embedField
    }
}