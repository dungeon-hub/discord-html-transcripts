package net.dungeonhub.transcripts

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.awt.Color
import java.time.Instant

class TranscriptGenerationScenariosTest {
    private val user = TestAuthor(1, "user", "Display User", Color(0x33, 0x66, 0x99))
    private val bot = TestAuthor(2, "helper", "Helper Bot", isBot = true)
    private val time = Instant.parse("2024-05-20T12:00:00Z")

    @TestFactory
    fun `generates transcripts for supported message variants`(): List<DynamicTest> {
        return listOf(
            scenario("empty channel", emptyList()) { document ->
                assertEquals("#scenario | 0 messages", document.title())
                assertTrue(document.select(".chatlog__message-group").isEmpty())
            },
            scenario("plain user message", listOf(message(1, "Hello world", author = user))) { document ->
                assertEquals("Hello world", document.selectFirst(".chatlog__content")!!.text())
                assertEquals("Display User", document.selectFirst(".chatlog__author-name")!!.text())
            },
            scenario("bot message", listOf(message(2, "Automated response", author = bot))) { document ->
                assertEquals("APP", document.selectFirst(".chatlog__bot-tag")!!.text())
                assertEquals("Helper Bot", document.selectFirst(".chatlog__author-name")!!.text())
            },
            scenario("message without author", listOf(message(3, "System response"))) { document ->
                assertEquals("Bot", document.selectFirst(".chatlog__author-name")!!.text())
                assertEquals("default_bot_avatar_url", document.selectFirst(".chatlog__author-avatar")!!.attr("src"))
            },
            scenario(
                "image attachment",
                listOf(message(4, "", attachments = listOf(attachment("photo.png", true))))
            ) { document ->
                assertEquals("img", document.selectFirst(".chatlog__attachment-media")!!.tagName())
                assertEquals("Image attachment", document.selectFirst(".chatlog__attachment-media")!!.attr("alt"))
            },
            scenario(
                "video and audio attachments",
                listOf(
                    message(
                        5,
                        "Media",
                        attachments = listOf(attachment("clip.mp4"), attachment("recording.mp3"))
                    )
                )
            ) { document ->
                assertEquals(1, document.select("video[controls]").size)
                assertEquals(1, document.select("audio[controls]").size)
            },
            scenario(
                "generic attachment",
                listOf(message(6, "File", attachments = listOf(attachment("report.pdf", size = 2048))))
            ) { document ->
                assertEquals("report.pdf", document.selectFirst(".chatlog__attachment-generic-name")!!.text())
                assertEquals("2.0 KB", document.selectFirst(".chatlog__attachment-generic-size")!!.text())
            },
            scenario("complete embed", listOf(message(7, "", author = user, embeds = listOf(completeEmbed())))) { document ->
                assertEquals("Embed title", document.selectFirst(".chatlog__embed-title")!!.text())
                assertEquals(2, document.select(".chatlog__embed-field--inline").size)
                assertEquals("#123abc", document.selectFirst(".chatlog__embed-color-pill")!!.attr("style").substringAfter("background-color: "))
                assertTrue(document.selectFirst(".chatlog__embed-footer")!!.text().startsWith("Footer • "))
            },
            scenario("reply", listOf(replyMessage())) { document ->
                assertEquals("Display User", document.selectFirst(".chatlog__reference-name")!!.text())
                assertEquals("Original message", document.selectFirst(".chatlog__reference-content em")!!.text())
            },
            scenario("application command", listOf(applicationMessage())) { document ->
                assertEquals("Transcript App", document.selectFirst(".chatlog__author-name")!!.text())
                assertEquals("used /transcript", document.selectFirst(".chatlog__reference-content")!!.text())
                assertEquals("https://cdn.discordapp.com/avatars/99/hash.png", document.selectFirst(".chatlog__author-avatar")!!.attr("src"))
            }
        ).map { test -> DynamicTest.dynamicTest(test.name) { test.verify(generate(test.messages)) } }
    }

    private fun generate(messages: List<TestMessage>): Document {
        val channel = TestChannel("scenario", TestServer("Scenario Server"), messages)
        return Jsoup.parse(DiscordHtmlTranscripts.createTranscript(channel))
    }

    private fun message(
        id: Long,
        content: String,
        author: TestAuthor? = null,
        attachments: List<TestAttachment> = emptyList(),
        embeds: List<TestEmbed> = emptyList()
    ) = TestMessage(id, content, time.plusSeconds(id), author, embeds, attachments)

    private fun attachment(fileName: String, isImage: Boolean = false, size: Long = 1024) =
        TestAttachment(fileName, isImage, "https://example.com/$fileName", size)

    private fun completeEmbed() = TestEmbed(
        color = Color(0x12, 0x3A, 0xBC),
        title = "Embed title",
        description = "Embed **description**",
        fields = listOf(TestEmbedField("One", "First", true), TestEmbedField("Two", "Second", true)),
        url = "https://example.com/embed",
        thumbnail = "https://example.com/thumbnail.png",
        image = "https://example.com/image.png",
        footer = TestEmbedFooter("Footer", "https://example.com/footer.png"),
        author = TestEmbedAuthor("Embed author", "https://example.com/author.png", "https://example.com/author"),
        timestamp = time
    )

    private fun replyMessage(): TestMessage {
        val original = message(8, "Original message", author = user)
        return TestMessage(9, "Reply", time.plusSeconds(9), user, reference = original)
    }

    private fun applicationMessage(): TestMessage {
        val application = TestApplication(99, "transcript-app", "Transcript App", avatar = "hash")
        val interaction = TestInteraction(10, "/transcript", user, application)
        return TestMessage(10, "Created transcript", time.plusSeconds(10), interaction = interaction)
    }

    private data class Scenario(
        val name: String,
        val messages: List<TestMessage>,
        val verify: (Document) -> Unit
    )

    private fun scenario(name: String, messages: List<TestMessage>, verify: (Document) -> Unit) =
        Scenario(name, messages, verify)
}
