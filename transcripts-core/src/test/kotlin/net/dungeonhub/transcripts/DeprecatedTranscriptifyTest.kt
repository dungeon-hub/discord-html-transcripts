@file:Suppress("DEPRECATION")

package net.dungeonhub.transcripts

import net.dungeonhub.transcripts.components.attachment
import net.dungeonhub.transcripts.components.embed
import net.dungeonhub.transcripts.components.embedAuthor
import net.dungeonhub.transcripts.components.embedField
import net.dungeonhub.transcripts.components.embedFooter
import net.dungeonhub.transcripts.components.message
import org.jsoup.nodes.Element
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Instant

/**
 * The jsoup based `transcriptify()` methods are deprecated, but must keep returning the same markup as the components.
 */
class DeprecatedTranscriptifyTest {
    private val time = Instant.parse("2024-05-20T12:00:00Z")
    private val author = TestAuthor(1, "user", "User", Color.RED)

    private fun assertSameAsComponent(element: Element, rootClass: String, component: Element) {
        assertNull(element.parent())
        assertEquals(rootClass, element.className())
        assertEquals(component.child(0).outerHtml(), element.outerHtml())
    }

    @Test
    fun `message transcriptify matches the message component`() {
        val reference = TestMessage(1, "Original", time, author)
        val message = TestMessage(
            2, "Hello **there**", time, author,
            embeds = listOf(TestEmbed(title = "Embed")),
            attachments = listOf(TestAttachment("a.png", true, "https://example.com/a.png", 1)),
            reference = reference
        )

        assertSameAsComponent(message.transcriptify(), "chatlog__message-group", render { message(message) })
    }

    @Test
    fun `embed transcriptify matches the embed component`() {
        val embed = TestEmbed(
            color = Color.GREEN,
            title = "Title",
            fields = listOf(TestEmbedField("Name", "Value", true)),
            footer = TestEmbedFooter("Footer", null),
            timestamp = time
        )

        assertSameAsComponent(embed.transcriptify(), "chatlog__embed", render { embed(embed) })
    }

    @Test
    fun `attachment transcriptify matches the attachment component`() {
        listOf(
            TestAttachment("photo.PNG", true, "https://example.com/photo.png", 10),
            TestAttachment("clip.MP4", false, "https://example.com/clip.mp4", 20),
            TestAttachment("sound.ogg", false, "https://example.com/sound.ogg", 30),
            TestAttachment("notes.txt", false, "https://example.com/notes.txt", 1024)
        ).forEach { attachment ->
            assertSameAsComponent(attachment.transcriptify(), "chatlog__attachment", render { attachment(attachment) })
        }
    }

    @Test
    fun `embed parts transcriptify match their components`() {
        val author = TestEmbedAuthor("Author", "https://example.com/icon.png", "https://example.com")
        val field = TestEmbedField("Name", "Value", false)
        val footer = TestEmbedFooter("Footer", "https://example.com/footer.png")

        assertSameAsComponent(author.transcriptify(), "chatlog__embed-author", render { embedAuthor(author) })
        assertSameAsComponent(field.transcriptify(), "chatlog__embed-field", render { embedField(field) })
        assertSameAsComponent(
            footer.transcriptify(time),
            "chatlog__embed-footer",
            render { embedFooter(footer, time) }
        )
    }

    @Test
    fun `returned elements are mutable jsoup elements`() {
        val element = TestAttachment("notes.txt", false, "https://example.com/notes.txt", 1).transcriptify()

        element.addClass("custom")

        assertEquals("chatlog__attachment custom", element.className())
    }
}
