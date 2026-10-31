package net.dungeonhub.transcripts

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Instant

class TranscriptDocumentTest {
    private val author = TestAuthor(1, "user", "User", Color.BLUE)
    private val time = Instant.parse("2024-01-01T00:00:00Z")

    private fun message(id: Long, offsetSeconds: Long = id, content: String = "Message $id") =
        TestMessage(id, content, time.plusSeconds(offsetSeconds), author)

    private fun transcriptOf(
        messages: List<TestMessage>,
        channelName: String = "channel",
        server: TestServer = TestServer("Server", "https://example.com/icon.png")
    ): String = DiscordHtmlTranscripts.createTranscript(TestChannel(channelName, server, messages))

    private fun documentOf(messages: List<TestMessage> = emptyList()): Document = Jsoup.parse(transcriptOf(messages))

    private fun Document.messageIds() = select(".chatlog__message").map { it.attr("data-message-id") }

    @Test
    fun `starts with an html5 doctype`() {
        assertTrue(transcriptOf(emptyList()).startsWith("<!DOCTYPE html>"))
    }

    @Test
    fun `sets document metadata`() {
        val document = documentOf()

        assertEquals("en", document.selectFirst("html")!!.attr("lang"))
        assertEquals("utf-8", document.selectFirst("meta[charset]")!!.attr("charset"))
        assertEquals("width=device-width", document.selectFirst("meta[name=viewport]")!!.attr("content"))
        assertEquals("transcriptTitle", document.selectFirst("title")!!.id())
    }

    @Test
    fun `includes the stylesheet`() {
        val style = documentOf().selectFirst("head > style")!!.data()

        assertTrue(style.contains(".chatlog__message-group"))
        assertTrue(style.contains("@font-face"))
    }

    @Test
    fun `includes scripts and icon symbols`() {
        val document = documentOf()

        assertTrue(document.select("script[src]").any { it.attr("src").contains("highlight") })
        assertTrue(document.select("script[src]").any { it.attr("src").contains("twemoji") })
        assertTrue(document.select("script").any { it.data().contains("function scrollToMessage") })
        assertNotNull(document.getElementById("icon-attachment"))
        assertNotNull(document.getElementById("icon-application-command"))
    }

    @Test
    fun `external resources are pinned and integrity checked`() {
        val document = documentOf()
        val resources = document.select("script[src]") + document.select("link[rel=stylesheet]")

        assertEquals(3, resources.size)
        resources.forEach { resource ->
            val url = resource.attr("src").ifEmpty { resource.attr("href") }
            assertTrue(Regex("@\\d+\\.\\d+\\.\\d+/").containsMatchIn(url), url)
            assertTrue(resource.attr("integrity").matches(Regex("sha384-[A-Za-z0-9+/]{64}")), url)
            assertEquals("anonymous", resource.attr("crossorigin"), url)
        }
    }

    @Test
    fun `renders the preamble`() {
        val document = documentOf()

        assertEquals("https://example.com/icon.png", document.selectFirst(".preamble__guild-icon")!!.attr("src"))
        assertEquals("Guild icon", document.selectFirst(".preamble__guild-icon")!!.attr("alt"))
        assertEquals("Server", document.getElementById("guildname")!!.text())
        assertEquals("#channel", document.getElementById("ticketname")!!.text())
    }

    @Test
    fun `servers without icon get an empty icon source`() {
        val document = Jsoup.parse(transcriptOf(emptyList(), server = TestServer("Server")))

        assertEquals("", document.selectFirst(".preamble__guild-icon")!!.attr("src"))
    }

    @Test
    fun `title counts the messages`() {
        assertEquals("#channel | 0 messages", documentOf().title())
        assertEquals("#channel | 3 messages", documentOf(listOf(message(1), message(2), message(3))).title())
    }

    @Test
    fun `messages are rendered into the chatlog`() {
        val document = documentOf(listOf(message(1), message(2)))

        assertEquals(2, document.select("#chatlog > .chatlog__message-group").size)
    }

    @Test
    fun `messages are sorted by creation time`() {
        val document = documentOf(listOf(message(3, 30), message(1, 10), message(2, 20)))

        assertEquals(listOf("1", "2", "3"), document.messageIds())
    }

    @Test
    fun `messages with the same creation time keep their order`() {
        val document = documentOf(listOf(message(5, 0), message(3, 0), message(4, 0)))

        assertEquals(listOf("5", "3", "4"), document.messageIds())
    }

    @Test
    fun `sorting does not modify the input list`() {
        val messages = mutableListOf(message(2), message(1))
        DiscordHtmlTranscripts.createTranscript(TestChannel("channel", TestServer("Server"), messages))

        assertEquals(listOf(2L, 1L), messages.map { it.id })
    }

    @Test
    fun `writeTranscript streams the same document`() {
        val messages = listOf(message(2), message(1))
        val channel = TestChannel("channel", TestServer("Server"), messages)
        val output = StringBuilder()

        DiscordHtmlTranscripts.writeTranscript(channel, messages, output)

        assertEquals(DiscordHtmlTranscripts.generateFromMessages(channel, messages), output.toString())
    }

    @Test
    fun `createTranscript uses the channel messages`() {
        val messages = listOf(message(1), message(2))
        val channel = TestChannel("channel", TestServer("Server"), messages)

        assertEquals(
            DiscordHtmlTranscripts.generateFromMessages(channel, messages),
            DiscordHtmlTranscripts.createTranscript(channel)
        )
    }

    @Test
    fun `output is deterministic`() {
        val messages = listOf(message(1), message(2))

        assertEquals(transcriptOf(messages), transcriptOf(messages))
    }

    @Test
    fun `handles many messages`() {
        val messages = (1L..2_000L).map { message(it) }
        val document = documentOf(messages.reversed())

        assertEquals("#channel | 2000 messages", document.title())
        assertEquals(messages.map { it.id.toString() }, document.messageIds())
    }

    @Test
    fun `escapes server and channel names`() {
        val payload = "</title><script>alert(1)</script>"
        val document = Jsoup.parse(transcriptOf(emptyList(), channelName = payload, server = TestServer(payload)))

        assertEquals("#$payload | 0 messages", document.title())
        assertEquals(payload, document.getElementById("guildname")!!.text())
        assertEquals("#$payload", document.getElementById("ticketname")!!.text())
        assertTrue(document.select("script").none { it.data().contains("alert(1)") })
    }

    @Test
    fun `preserves unicode content`() {
        val document = documentOf(listOf(message(1, content = "Grüße 👋 — こんにちは")))

        assertEquals("Grüße 👋 — こんにちは", document.selectFirst(".chatlog__content")!!.text())
    }
}
