package net.dungeonhub.transcripts

import org.jsoup.Jsoup
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Instant

class DiscordHtmlTranscriptsTest {
    private val author = TestAuthor(42, "example-user", "Example User", Color(0x12, 0x34, 0x56))

    @Test
    fun `createTranscript generates a complete example transcript`() {
        val firstMessage = TestMessage(
            id = 100,
            content = "Hello **Discord**!",
            creationTime = Instant.parse("2024-01-01T10:00:00Z"),
            author = author,
            attachments = listOf(
                TestAttachment("example.png", true, "https://example.com/example.png", 2048)
            ),
            embeds = listOf(
                TestEmbed(
                    color = Color(0xAB, 0xCD, 0xEF),
                    title = "Example embed",
                    description = "An *example* description",
                    fields = listOf(TestEmbedField("Status", "**Ready**", true)),
                    url = "https://example.com/embed",
                    thumbnail = "https://example.com/thumbnail.png",
                    image = "https://example.com/embed.png",
                    author = TestEmbedAuthor("Example author", null, "https://example.com/author"),
                    footer = TestEmbedFooter("Example footer", null)
                )
            )
        )
        val secondMessage = TestMessage(
            id = 200,
            content = "This message replies to the first.",
            creationTime = Instant.parse("2024-01-01T10:01:00Z"),
            author = author,
            reference = firstMessage
        )
        val channel = TestChannel("example-channel", TestServer("Example Server", "https://example.com/icon.png"), listOf(firstMessage, secondMessage))

        val document = Jsoup.parse(DiscordHtmlTranscripts.createTranscript(channel))

        assertEquals("#example-channel | 2 messages", document.getElementById("transcriptTitle")!!.text())
        assertEquals("Example Server", document.getElementById("guildname")!!.text())
        assertEquals("https://example.com/icon.png", document.selectFirst(".preamble__guild-icon")!!.attr("src"))
        assertEquals(listOf("100", "200"), document.select(".chatlog__message").map { it.attr("data-message-id") })
        assertEquals("Discord", document.selectFirst("#message-100 strong")!!.text())
        assertEquals("https://example.com/example.png", document.selectFirst("#message-100 .chatlog__attachment-media")!!.attr("src"))
        assertEquals("Example embed", document.selectFirst(".chatlog__embed-title")!!.text())
        assertEquals("Example User", document.selectFirst(".chatlog__reference-name")!!.text())
    }

    @Test
    fun `generateFromMessages uses the supplied messages and sorts them chronologically`() {
        val channelMessage = TestMessage(1, "not included", Instant.EPOCH, author)
        val later = TestMessage(3, "later", Instant.parse("2024-01-03T00:00:00Z"), author)
        val earlier = TestMessage(2, "earlier", Instant.parse("2024-01-02T00:00:00Z"), author)
        val channel = TestChannel("sorting", TestServer("Server"), listOf(channelMessage))

        val document = Jsoup.parse(DiscordHtmlTranscripts.generateFromMessages(channel, listOf(later, earlier)))

        assertEquals("#sorting | 2 messages", document.title())
        assertEquals(listOf("2", "3"), document.select(".chatlog__message").map { it.attr("data-message-id") })
        assertTrue(document.select("[data-message-id=1]").isEmpty())
    }
}
