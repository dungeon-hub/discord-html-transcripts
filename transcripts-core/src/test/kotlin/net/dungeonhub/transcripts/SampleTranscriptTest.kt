package net.dungeonhub.transcripts

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SampleTranscriptTest {
    private val document = Jsoup.parse(SampleTranscript.render())

    @Test
    fun `renders every sample message`() {
        val expectedIds = SampleTranscript.messages().map { it.id.toString() }.sorted()
        val renderedIds = document.select(".chatlog__message").map { it.attr("data-message-id") }.sorted()

        assertEquals(expectedIds, renderedIds)
    }

    @Test
    fun `message ids are unique`() {
        val ids = document.select(".chatlog__message").map { it.id() }

        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `covers every kind of content`() {
        listOf(
            ".chatlog__bot-tag", ".chatlog__reference-link[onclick]", ".chatlog__command_name",
            "img.chatlog__attachment-media", "video.chatlog__attachment-media", "audio.chatlog__attachment-media",
            ".chatlog__attachment-generic", ".chatlog__embed-author", ".chatlog__embed-field--inline",
            ".chatlog__embed-field", ".chatlog__embed-thumbnail", ".chatlog__embed-image", ".chatlog__embed-footer",
            ".chatlog__embed-color-pill--default", "strong", "em", "s", "u", ".pre--inline", ".pre--multiline", ".quote"
        ).forEach { assertTrue(document.select(it).isNotEmpty(), it) }
    }

    @Test
    fun `injection attempts stay inert`() {
        assertTrue(document.select("script").none { it.data().contains("xss") })
        assertTrue(document.select("[onerror], [onmouseover]").isEmpty())
        assertTrue(document.select("[onclick]").all { it.attr("onclick").startsWith("scrollToMessage(") })
        assertTrue(document.select("a[href^=javascript]").isEmpty())
    }

    @Test
    fun `messages are sorted chronologically`() {
        val expectedIds = SampleTranscript.messages().sortedBy { it.creationTime }.map { it.id.toString() }
        val renderedIds = document.select(".chatlog__message").map { it.attr("data-message-id") }
        val last = document.select(".chatlog__content").last()!!

        assertEquals(expectedIds, renderedIds)
        assertTrue(last.text().startsWith("This message is passed in first"))
    }
}
