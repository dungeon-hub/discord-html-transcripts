package net.dungeonhub.transcripts

import net.dungeonhub.wrapper.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.jsoup.Jsoup
import java.time.Instant
import java.awt.Color

class TranscriptTest {

    private val mockServer = object : DiscordServer {
        override val name: String = "Test Guild"
        override val icon: String? = "http://example.com/icon.png"

        override fun getMemberName(id: Long): String? = if (id == 123L) "TestUser" else null
        override fun getRoleName(id: Long): String? = if (id == 456L) "TestRole" else null
        override fun getChannelName(id: Long): String? = if (id == 789L) "test-channel" else null
    }

    @Test
    fun testMentionFormatting() {
        val rawText = "Hello <@123>, check <@&456> in <#789> and unknown <@999>"
        val formatted = Formatter.format(rawText, mockServer)
        
        assertTrue(formatted.contains("<span class=\"mention\">@TestUser</span>"))
        assertTrue(formatted.contains("<span class=\"mention\">@TestRole</span>"))
        assertTrue(formatted.contains("<span class=\"mention\">#test-channel</span>"))
        assertTrue(formatted.contains("<span class=\"mention\">@999</span>"))
    }

    @Test
    fun testBackslashEscaping() {
        // Escaped asterisks, underscores, and mentions should not be formatted, and backslashes should be removed.
        val text1 = "This is \\*\\*not bold\\*\\*"
        val formatted1 = Formatter.format(text1, mockServer)
        assertEquals("This is **not bold**", formatted1)

        val text2 = "This is \\_not italic\\_"
        val formatted2 = Formatter.format(text2, mockServer)
        assertEquals("This is _not italic_", formatted2)

        val text3 = "Do not resolve \\<@123\\>"
        val formatted3 = Formatter.format(text3, mockServer)
        assertEquals("Do not resolve &lt;@123&gt;", formatted3)

        val text4 = "Escaped backslash: \\\\\\*"
        val formatted4 = Formatter.format(text4, mockServer)
        assertEquals("Escaped backslash: \\*", formatted4)
    }

    @Test
    fun testHtmlSafetyAndInjection() {
        // Raw HTML should be escaped to prevent injection/XSS.
        val maliciousText = "<script>alert('xss')</script> and <b>bold HTML</b>"
        val formatted = Formatter.format(maliciousText, mockServer)
        
        assertFalse(formatted.contains("<script>"))
        assertFalse(formatted.contains("<b>"))
        assertTrue(formatted.contains("&lt;script&gt;"))
        assertTrue(formatted.contains("&lt;b&gt;"))
    }

    @Test
    fun testCodeBlockIsolation() {
        // Backslashes and markdown characters inside code blocks should not be formatted.
        val codeText = "```\n\\*\\*not formatted\\*\\*\n```"
        val formatted = Formatter.format(codeText, mockServer)
        
        assertTrue(formatted.contains("\\*\\*not formatted\\*\\*"))
        assertFalse(formatted.contains("<strong>"))
    }

    @Test
    fun testDateDividersAndTimestamps() {
        val channel = object : DiscordChannel<DiscordFramework> {
            override val framework: DiscordFramework = object : DiscordFramework {}
            override val name: String = "general"
            override val server: DiscordServer = mockServer
            override val messages: List<DiscordMessage> = listOf(
                createMockMessage(1L, "Message 1", Instant.parse("2026-07-29T10:00:00Z")),
                createMockMessage(2L, "Message 2", Instant.parse("2026-07-30T10:00:00Z")),
                createMockMessage(3L, "Message 3", Instant.parse("2026-07-30T11:00:00Z"))
            )
        }

        val html = DiscordHtmlTranscripts.createTranscript(channel)
        val doc = Jsoup.parse(html)

        // Check for date dividers
        val dateDividers = doc.select(".chatlog__date-divider")
        assertEquals(2, dateDividers.size)
        // Check formatting structure
        assertTrue(dateDividers[0].text().matches(Regex("\\d+ \\w+ \\d{4}")))

        // Check for timestamp tooltips
        val timestamps = doc.select(".chatlog__timestamp")
        assertTrue(timestamps.size >= 3)
        for (ts in timestamps) {
            val title = ts.attr("title")
            assertNotNull(title)
            assertTrue(title.contains("at"))
        }
    }

    private fun createMockMessage(messageId: Long, msgContent: String, msgTime: Instant): DiscordMessage {
        val mockAuthor = object : DiscordMessageAuthor {
            override val id: Long = 1000L + messageId
            override val name: String = "Author $messageId"
            override val displayName: String = "Author $messageId"
            override val avatar: String = "http://example.com/avatar.png"
            override val isBot: Boolean = false
            override val roleColor: Color = Color.WHITE
        }

        return object : DiscordMessage {
            override val id: Long = messageId
            override val content: String = msgContent
            override val embeds: List<DiscordMessageEmbed?> = emptyList()
            override val attachments: List<DiscordMessageAttachment> = emptyList()
            override val creationTime: Instant = msgTime
            override val reference: DiscordMessage? = null
            override val author: DiscordMessageAuthor = mockAuthor
            override val interaction: DiscordInteraction? = null
        }
    }
}
