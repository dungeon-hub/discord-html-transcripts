package net.dungeonhub.transcripts

import net.dungeonhub.transcripts.components.embed
import net.dungeonhub.transcripts.components.embedAuthor
import net.dungeonhub.transcripts.components.embedField
import net.dungeonhub.transcripts.components.embedFooter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.awt.Color
import java.time.Instant

class EmbedComponentTest {
    private val timestamp = Instant.parse("2024-05-20T12:34:56Z")

    @Test
    fun `an empty embed only renders its skeleton`() {
        val rendered = render { embed(TestEmbed()) }

        val embed = rendered.child(0)
        assertEquals("chatlog__embed", embed.className())
        assertNotNull(embed.selectFirst(".chatlog__embed-color-pill--default"))
        assertNotNull(embed.selectFirst(".chatlog__embed-content-container > .chatlog__embed-content > .chatlog__embed-text"))
        assertTrue(embed.selectFirst(".chatlog__embed-text")!!.children().isEmpty())
        listOf(
            ".chatlog__embed-author", ".chatlog__embed-title", ".chatlog__embed-description",
            ".chatlog__embed-fields", ".chatlog__embed-thumbnail-container", ".chatlog__embed-image-container",
            ".chatlog__embed-footer"
        ).forEach { assertNull(embed.selectFirst(it), it) }
    }

    @Test
    fun `colored embeds use the color as pill background`() {
        val pill = render { embed(TestEmbed(color = Color(0x12, 0x3A, 0xBC))) }.selectFirst(".chatlog__embed-color-pill")!!

        assertEquals("background-color: #123abc", pill.attr("style"))
        assertFalse(pill.hasClass("chatlog__embed-color-pill--default"))
    }

    @Test
    fun `titles without url are plain markdown`() {
        val title = render { embed(TestEmbed(title = "A **bold** title")) }.selectFirst(".chatlog__embed-title")!!

        assertNull(title.selectFirst("a"))
        assertEquals("bold", title.selectFirst(".markdown.preserve-whitespace strong")!!.text())
        assertEquals("A bold title", title.text())
    }

    @Test
    fun `titles with url are wrapped in a link`() {
        val link = render { embed(TestEmbed(title = "Linked", url = "https://example.com")) }
            .selectFirst(".chatlog__embed-title > a.chatlog__embed-title-link")!!

        assertEquals("https://example.com", link.attr("href"))
        assertEquals("Linked", link.selectFirst(".markdown")!!.text())
    }

    @Test
    fun `urls without title are not rendered`() {
        val rendered = render { embed(TestEmbed(url = "https://example.com")) }

        assertTrue(rendered.select("a").isEmpty())
    }

    @Test
    fun `descriptions are rendered as markdown`() {
        val description = render { embed(TestEmbed(description = "Line *one*\nLine two")) }
            .selectFirst(".chatlog__embed-description > .markdown")!!

        assertEquals("one", description.selectFirst("em")!!.text())
        assertEquals(1, description.select("br").size)
    }

    @Test
    fun `fields keep their order and inline flag`() {
        val fields = render {
            embed(
                TestEmbed(
                    fields = listOf(
                        TestEmbedField("First", "1", true),
                        TestEmbedField("Second", "2", false),
                        TestEmbedField("Third", "3", true)
                    )
                )
            )
        }.selectFirst(".chatlog__embed-fields")!!.children()

        assertEquals(listOf("First", "Second", "Third"), fields.map { it.selectFirst(".chatlog__embed-field-name")!!.text() })
        assertEquals(listOf("1", "2", "3"), fields.map { it.selectFirst(".chatlog__embed-field-value")!!.text() })
        assertEquals(
            listOf("chatlog__embed-field--inline", "chatlog__embed-field", "chatlog__embed-field--inline"),
            fields.map { it.className() }
        )
    }

    @Test
    fun `field names and values support markdown`() {
        val field = render { embedField(TestEmbedField("**Name**", "`value`", false)) }

        assertEquals("Name", field.selectFirst(".chatlog__embed-field-name strong")!!.text())
        assertEquals("value", field.selectFirst(".chatlog__embed-field-value .pre--inline")!!.text())
    }

    @Test
    fun `thumbnails link to themselves`() {
        val container = render { embed(TestEmbed(thumbnail = "https://example.com/thumb.png")) }
            .selectFirst(".chatlog__embed-content > .chatlog__embed-thumbnail-container")!!

        assertEquals("https://example.com/thumb.png", container.selectFirst("a.chatlog__embed-thumbnail-link")!!.attr("href"))
        val image = container.selectFirst("img.chatlog__embed-thumbnail")!!
        assertEquals("https://example.com/thumb.png", image.attr("src"))
        assertEquals("Thumbnail", image.attr("alt"))
        assertEquals("lazy", image.attr("loading"))
    }

    @Test
    fun `images are rendered below the embed content`() {
        val container = render { embed(TestEmbed(image = "https://example.com/image.png")) }
            .selectFirst(".chatlog__embed-content-container > .chatlog__embed-image-container")!!

        assertEquals("https://example.com/image.png", container.selectFirst("a.chatlog__embed-image-link")!!.attr("href"))
        val image = container.selectFirst("img.chatlog__embed-image")!!
        assertEquals("https://example.com/image.png", image.attr("src"))
        assertEquals("Image", image.attr("alt"))
    }

    @Test
    fun `embed parts are rendered in discord order`() {
        val embed = render {
            embed(
                TestEmbed(
                    title = "Title",
                    description = "Description",
                    fields = listOf(TestEmbedField("Field", "Value", false)),
                    thumbnail = "https://example.com/thumb.png",
                    image = "https://example.com/image.png",
                    footer = TestEmbedFooter("Footer", null),
                    author = TestEmbedAuthor("Author", null, null)
                )
            )
        }

        assertEquals(
            listOf("chatlog__embed-author", "chatlog__embed-title", "chatlog__embed-description", "chatlog__embed-fields"),
            embed.selectFirst(".chatlog__embed-text")!!.children().map { it.className() }
        )
        assertEquals(
            listOf("chatlog__embed-text", "chatlog__embed-thumbnail-container"),
            embed.selectFirst(".chatlog__embed-content")!!.children().map { it.className() }
        )
        assertEquals(
            listOf("chatlog__embed-content", "chatlog__embed-image-container", "chatlog__embed-footer"),
            embed.selectFirst(".chatlog__embed-content-container")!!.children().map { it.className() }
        )
    }

    @Test
    fun `authors without name are skipped`() {
        val rendered = render { embed(TestEmbed(author = TestEmbedAuthor(null, "https://example.com/icon.png", null))) }

        assertNull(rendered.selectFirst(".chatlog__embed-author"))
    }

    @Test
    fun `authors render icon and plain name`() {
        val author = render { embedAuthor(TestEmbedAuthor("Author", "https://example.com/icon.png", null)) }
            .selectFirst(".chatlog__embed-author")!!

        val icon = author.selectFirst("img.chatlog__embed-author-icon")!!
        assertEquals("https://example.com/icon.png", icon.attr("src"))
        assertEquals("Author icon", icon.attr("alt"))
        assertNull(author.selectFirst("a"))
        assertEquals("Author", author.selectFirst(".chatlog__embed-author-name")!!.text())
    }

    @Test
    fun `authors with url link their name`() {
        val author = render { embedAuthor(TestEmbedAuthor("Author", null, "https://example.com/author")) }

        assertNull(author.selectFirst("img"))
        val link = author.selectFirst(".chatlog__embed-author-name > a.chatlog__embed-author-name-link")!!
        assertEquals("https://example.com/author", link.attr("href"))
        assertEquals("Author", link.text())
    }

    @Test
    fun `footers show text and icon`() {
        val footer = render { embedFooter(TestEmbedFooter("Footer", "https://example.com/footer.png"), null) }

        assertEquals("https://example.com/footer.png", footer.selectFirst("img.chatlog__embed-footer-icon")!!.attr("src"))
        assertEquals("Footer icon", footer.selectFirst("img.chatlog__embed-footer-icon")!!.attr("alt"))
        assertEquals("Footer", footer.selectFirst(".chatlog__embed-footer-text")!!.text())
    }

    @Test
    fun `footers append the embed timestamp`() {
        val footer = render { embedFooter(TestEmbedFooter("Footer", null), timestamp) }

        assertNull(footer.selectFirst("img"))
        assertEquals("Footer • ${longTime(timestamp)}", footer.selectFirst(".chatlog__embed-footer-text")!!.text())
    }

    @Test
    fun `footers without text still show the timestamp`() {
        val footer = render { embedFooter(TestEmbedFooter(null, null), timestamp) }

        assertEquals("• ${longTime(timestamp)}", footer.selectFirst(".chatlog__embed-footer-text")!!.text())
    }

    @Test
    fun `footers without text and timestamp are empty`() {
        val footer = render { embedFooter(TestEmbedFooter(null, null), null) }

        assertEquals("", footer.selectFirst(".chatlog__embed-footer-text")!!.text())
    }

    @Test
    fun `embeds use their own timestamp for the footer`() {
        val rendered = render { embed(TestEmbed(footer = TestEmbedFooter("Footer", null), timestamp = timestamp)) }

        assertEquals("Footer • ${longTime(timestamp)}", rendered.selectFirst(".chatlog__embed-footer-text")!!.text())
    }

    @Test
    fun `timestamps without footer are not rendered`() {
        val rendered = render { embed(TestEmbed(timestamp = timestamp)) }

        assertNull(rendered.selectFirst(".chatlog__embed-footer"))
    }

    @Test
    fun `escapes user supplied text`() {
        val payload = "<script>alert(1)</script>"
        val rendered = render {
            embed(
                TestEmbed(
                    title = payload,
                    description = payload,
                    fields = listOf(TestEmbedField(payload, payload, false)),
                    footer = TestEmbedFooter(payload, null),
                    author = TestEmbedAuthor(payload, null, null)
                )
            )
        }

        assertTrue(rendered.select("script").isEmpty())
        assertEquals(6, rendered.getElementsContainingOwnText(payload).size)
    }
}
