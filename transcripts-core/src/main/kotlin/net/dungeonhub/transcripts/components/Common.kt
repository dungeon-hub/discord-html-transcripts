package net.dungeonhub.transcripts.components

import kotlinx.html.FlowContent
import kotlinx.html.FlowOrPhrasingContent
import kotlinx.html.HTMLTag
import kotlinx.html.IMG
import kotlinx.html.attributesMapOf
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import kotlinx.html.unsafe
import kotlinx.html.visit
import net.dungeonhub.transcripts.Formatter
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val shortTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val longTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

internal fun Instant.toShortTime(): String = shortTimeFormatter.withZone(ZoneId.systemDefault()).format(this)

internal fun Instant.toLongTime(): String = longTimeFormatter.withZone(ZoneId.systemDefault()).format(this)

/**
 * Renders [text] as Discord markdown. The text is escaped by [Formatter.format] before the markdown is applied.
 */
fun HTMLTag.markdownHtml(text: String) {
    unsafe { +Formatter.format(text) }
}

/**
 * A `div.markdown.preserve-whitespace` holding the rendered markdown of [text].
 */
fun FlowContent.markdown(text: String) {
    div("markdown preserve-whitespace") { markdownHtml(text) }
}

/**
 * Renders an icon that references one of the `<symbol>`s defined in the transcript head.
 */
fun FlowOrPhrasingContent.icon(classes: String, symbol: String) {
    HTMLTag("svg", consumer, attributesMapOf("class", classes), null, inlineTag = true, emptyTag = false).visit {
        HTMLTag("use", consumer, attributesMapOf("xlink:href", "#$symbol"), null, inlineTag = true, emptyTag = false)
            .visit {}
    }
}

internal fun IMG.lazy() {
    attributes["loading"] = "lazy"
}

/**
 * Renders a single component and parses it into a detached jsoup [Element].
 * Used to keep the deprecated `transcriptify()` methods working.
 */
internal fun renderElement(block: FlowContent.() -> Unit): Element {
    val html = createHTML(prettyPrint = false).div { block() }
    val wrapper = Jsoup.parseBodyFragment(html).body().child(0)
    return wrapper.child(0).also { it.remove() }
}
