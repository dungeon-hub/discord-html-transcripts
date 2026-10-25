package net.dungeonhub.transcripts

import net.dungeonhub.transcripts.components.commandReference
import net.dungeonhub.transcripts.components.message
import net.dungeonhub.transcripts.components.replyReference
import org.jsoup.nodes.Element
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.awt.Color
import java.time.Instant

class MessageComponentTest {
    private val time = Instant.parse("2024-05-20T12:34:56Z")
    private val user = TestAuthor(1, "user", "Display User", Color(0x33, 0x66, 0x99), "https://example.com/user.png")
    private val bot = TestAuthor(2, "helper", "Helper Bot", isBot = true)

    private fun renderMessage(message: TestMessage): Element = render { message(message) }.child(0)

    private fun message(
        content: String = "Hello",
        author: TestAuthor? = user,
        id: Long = 100,
        embeds: List<TestEmbed?> = emptyList(),
        attachments: List<TestAttachment> = emptyList(),
        reference: TestMessage? = null,
        interaction: TestInteraction? = null
    ) = TestMessage(id, content, time, author, embeds, attachments, reference, interaction)

    @Nested
    inner class Structure {
        @Test
        fun `renders a message group with avatar and message list`() {
            val group = renderMessage(message())

            assertEquals("chatlog__message-group", group.className())
            assertEquals(
                listOf("chatlog__author-avatar-container", "chatlog__messages"),
                group.children().map { it.className() }
            )
        }

        @Test
        fun `renders header elements before the message`() {
            val messages = renderMessage(message(author = bot)).selectFirst(".chatlog__messages")!!

            assertEquals(
                listOf("chatlog__author-name", "chatlog__bot-tag", "chatlog__timestamp", "chatlog__message"),
                messages.children().map { it.className() }
            )
        }

        @Test
        fun `message element carries id and full timestamp`() {
            val message = renderMessage(message(id = 1234)).selectFirst(".chatlog__message")!!

            assertEquals("message-1234", message.id())
            assertEquals("1234", message.attr("data-message-id"))
            assertEquals("Message sent: ${longTime(time)}", message.attr("title"))
        }

        @Test
        fun `timestamp shows hours and minutes`() {
            assertEquals(shortTime(time), renderMessage(message()).selectFirst(".chatlog__timestamp")!!.text())
        }
    }

    @Nested
    inner class Author {
        @Test
        fun `shows author name avatar and id`() {
            val group = renderMessage(message())
            val name = group.selectFirst(".chatlog__author-name")!!
            val avatar = group.selectFirst(".chatlog__author-avatar-container > img.chatlog__author-avatar")!!

            assertEquals("Display User", name.text())
            assertEquals("user", name.attr("title"))
            assertEquals("1", name.attr("data-user-id"))
            assertEquals("https://example.com/user.png", avatar.attr("src"))
            assertEquals("Avatar", avatar.attr("alt"))
            assertEquals("lazy", avatar.attr("loading"))
        }

        @Test
        fun `bots get an app tag`() {
            assertEquals("APP", renderMessage(message(author = bot)).selectFirst(".chatlog__bot-tag")!!.text())
        }

        @Test
        fun `users do not get an app tag`() {
            assertNull(renderMessage(message()).selectFirst(".chatlog__bot-tag"))
        }

        @Test
        fun `messages without author fall back to a bot placeholder`() {
            val group = renderMessage(message(author = null))
            val name = group.selectFirst(".chatlog__author-name")!!

            assertEquals("Bot", name.text())
            assertEquals("Bot", name.attr("title"))
            assertEquals("Bot", name.attr("data-user-id"))
            assertEquals("default_bot_avatar_url", group.selectFirst(".chatlog__author-avatar")!!.attr("src"))
            assertNull(group.selectFirst(".chatlog__bot-tag"))
        }

        @Test
        fun `interaction without application falls back to the bot placeholder`() {
            val group = renderMessage(message(author = null, interaction = TestInteraction(1, "/cmd", user)))

            assertEquals("Bot", group.selectFirst(".chatlog__author-name")!!.text())
        }

        @Test
        fun `application responses are shown as sent by the application`() {
            val application = TestApplication(99, "transcript-app", "Transcript App", avatar = "hash")
            val group = renderMessage(
                message(author = null, interaction = TestInteraction(1, "/cmd", user, application))
            )
            val name = group.selectFirst(".chatlog__author-name")!!

            assertEquals("Transcript App", name.text())
            assertEquals("transcript-app", name.attr("title"))
            assertEquals("99", name.attr("data-user-id"))
            assertEquals("https://cdn.discordapp.com/avatars/99/hash.png", group.selectFirst(".chatlog__author-avatar")!!.attr("src"))
        }

        @Test
        fun `animated application avatars use gif`() {
            val application = TestApplication(99, "app", avatar = "a_animated")
            val group = renderMessage(message(author = null, interaction = TestInteraction(1, "/cmd", user, application)))

            assertEquals(
                "https://cdn.discordapp.com/avatars/99/a_animated.gif",
                group.selectFirst(".chatlog__author-avatar")!!.attr("src")
            )
        }

        @Test
        fun `full application avatar urls are used as is`() {
            val application = TestApplication(99, "app", avatar = "https://example.com/app.png")
            val group = renderMessage(message(author = null, interaction = TestInteraction(1, "/cmd", user, application)))

            assertEquals("https://example.com/app.png", group.selectFirst(".chatlog__author-avatar")!!.attr("src"))
        }

        @Test
        fun `the message author takes precedence over the application`() {
            val application = TestApplication(99, "app", "Application")
            val group = renderMessage(message(author = bot, interaction = TestInteraction(1, "/cmd", user, application)))

            assertEquals("Helper Bot", group.selectFirst(".chatlog__author-name")!!.text())
        }
    }

    @Nested
    inner class Content {
        @Test
        fun `content is rendered as markdown`() {
            val content = renderMessage(message("Hello **world**")).selectFirst(
                ".chatlog__message > .chatlog__content > .markdown > span.preserve-whitespace"
            )!!

            assertEquals("Hello world", content.text())
            assertEquals("world", content.selectFirst("strong")!!.text())
        }

        @ParameterizedTest
        @ValueSource(strings = ["", " ", "\n", "\t "])
        fun `blank content is omitted`(content: String) {
            assertNull(renderMessage(message(content)).selectFirst(".chatlog__content"))
        }

        @Test
        fun `multiline content keeps line breaks`() {
            val content = renderMessage(message("one\ntwo\nthree")).selectFirst(".chatlog__content")!!

            assertEquals(2, content.select("br").size)
        }

        @Test
        fun `attachments are rendered inside the message in order`() {
            val message = renderMessage(
                message(
                    attachments = listOf(
                        TestAttachment("first.png", true, "https://example.com/first.png", 1),
                        TestAttachment("second.txt", false, "https://example.com/second.txt", 1)
                    )
                )
            ).selectFirst(".chatlog__message")!!

            val attachments = message.select("> .chatlog__attachment")
            assertEquals(2, attachments.size)
            assertNotNull(attachments[0].selectFirst("img"))
            assertNotNull(attachments[1].selectFirst(".chatlog__attachment-generic"))
            assertEquals("chatlog__content", message.child(0).className())
        }

        @Test
        fun `embeds follow the message and skip null entries`() {
            val messages = renderMessage(
                message(embeds = listOf(TestEmbed(title = "First"), null, TestEmbed(title = "Second")))
            ).selectFirst(".chatlog__messages")!!

            val embeds = messages.select("> .chatlog__embed")
            assertEquals(listOf("First", "Second"), embeds.map { it.selectFirst(".chatlog__embed-title")!!.text() })
            assertTrue(messages.select(".chatlog__message .chatlog__embed").isEmpty())
        }

        @Test
        fun `messages with only attachments have no content`() {
            val message = renderMessage(
                message("", attachments = listOf(TestAttachment("a.png", true, "https://example.com/a.png", 1)))
            ).selectFirst(".chatlog__message")!!

            assertNull(message.selectFirst(".chatlog__content"))
            assertEquals(1, message.select(".chatlog__attachment").size)
        }
    }

    @Nested
    inner class Replies {
        private val original = message("Original message", id = 1)

        @Test
        fun `replies show the referenced author and content`() {
            val group = renderMessage(message("Reply", id = 2, reference = original))
            val reference = group.selectFirst(".chatlog__reference")!!

            assertEquals("chatlog__reference-symbol", group.child(0).className())
            assertEquals("chatlog__reference", group.child(1).className())
            assertEquals("https://example.com/user.png", reference.selectFirst("img.chatlog__reference-avatar")!!.attr("src"))
            assertEquals("Display User", reference.selectFirst(".chatlog__reference-name")!!.text())
            assertEquals("user", reference.selectFirst(".chatlog__reference-name")!!.attr("title"))
            assertEquals("Original message", reference.selectFirst(".chatlog__reference-link em")!!.text())
        }

        @Test
        fun `reply author names use the role color`() {
            val reference = render { replyReference(original) }

            assertEquals("color: #336699", reference.selectFirst(".chatlog__reference-name")!!.attr("style"))
        }

        @Test
        fun `clicking a reply scrolls to the referenced message`() {
            val link = render { replyReference(original) }.selectFirst(".chatlog__reference-link")!!

            assertEquals("scrollToMessage(event, '1')", link.attr("onclick"))
        }

        @Test
        fun `long reply previews are truncated`() {
            val content = "a".repeat(50)
            val preview = render { replyReference(message(content)) }.selectFirst(".chatlog__reference-link em")!!

            assertEquals("a".repeat(42) + "...", preview.text())
        }

        @Test
        fun `previews of exactly the maximum length are not truncated`() {
            val content = "b".repeat(42)
            val preview = render { replyReference(message(content)) }.selectFirst(".chatlog__reference-link em")!!

            assertEquals(content, preview.text())
        }

        @Test
        fun `replies to messages without content mention the attachment`() {
            val preview = render { replyReference(message("")) }.selectFirst(".chatlog__reference-link em")!!

            assertEquals("Click to see attachment", preview.text())
        }

        @Test
        fun `reply previews show raw text instead of markdown`() {
            val preview = render { replyReference(message("**bold** <b>tag</b>")) }.selectFirst(".chatlog__reference-link em")!!

            assertEquals("**bold** <b>tag</b>", preview.text())
            assertTrue(preview.children().isEmpty())
        }

        @Test
        fun `replies to messages without author render an empty reference`() {
            val group = renderMessage(message(reference = message(author = null)))

            assertNotNull(group.selectFirst(".chatlog__reference-symbol"))
            assertTrue(group.selectFirst(".chatlog__reference")!!.children().isEmpty())
        }

        @Test
        fun `messages without reference have no reference elements`() {
            val group = renderMessage(message())

            assertNull(group.selectFirst(".chatlog__reference-symbol"))
            assertNull(group.selectFirst(".chatlog__reference"))
        }

        @Test
        fun `replies take precedence over command references`() {
            val group = renderMessage(message(reference = original, interaction = TestInteraction(1, "/cmd", bot)))

            assertEquals(1, group.select(".chatlog__reference").size)
            assertNull(group.selectFirst(".chatlog__command_name"))
            assertEquals("Display User", group.selectFirst(".chatlog__reference-name")!!.text())
        }
    }

    @Nested
    inner class Commands {
        private val interaction = TestInteraction(5, "/transcript", user, TestApplication(99, "app"))

        @Test
        fun `command responses show who used which command`() {
            val group = renderMessage(message(author = null, interaction = interaction))
            val reference = group.selectFirst(".chatlog__reference")!!

            assertEquals("chatlog__reference-symbol", group.child(0).className())
            assertEquals("Display User", reference.selectFirst(".chatlog__reference-name")!!.text())
            assertEquals("color: #336699", reference.selectFirst(".chatlog__reference-name")!!.attr("style"))
            assertEquals("used /transcript", reference.selectFirst(".chatlog__reference-content")!!.text())
            assertEquals("/transcript", reference.selectFirst(".chatlog__command_name")!!.text())
        }

        @Test
        fun `command references are not clickable`() {
            val link = render { commandReference(interaction) }.selectFirst(".chatlog__reference-link")!!

            assertTrue(!link.hasAttr("onclick"))
        }

        @Test
        fun `command references show the application command icon`() {
            val icon = render { commandReference(interaction) }.selectFirst(".chatlog__command_name > svg.chatlog__application-icon")!!

            assertEquals("#icon-application-command", icon.selectFirst("use")!!.attr("xlink:href"))
        }
    }

    @Nested
    inner class Escaping {
        private val payload = "<img src=x onerror=alert(1)>"

        @Test
        fun `escapes message content`() {
            val group = renderMessage(message(payload))

            assertTrue(group.select("img[onerror]").isEmpty())
            assertEquals(payload, group.selectFirst(".chatlog__content")!!.text())
        }

        @Test
        fun `escapes author names`() {
            val author = TestAuthor(1, payload, payload)
            val group = renderMessage(message(author = author))

            assertTrue(group.select("img[onerror]").isEmpty())
            assertEquals(payload, group.selectFirst(".chatlog__author-name")!!.text())
            assertEquals(payload, group.selectFirst(".chatlog__author-name")!!.attr("title"))
        }

        @Test
        fun `escapes reply previews and authors`() {
            val author = TestAuthor(1, payload, payload)
            val group = renderMessage(message(reference = message(payload, author = author)))

            assertTrue(group.select("img[onerror]").isEmpty())
            assertEquals(payload, group.selectFirst(".chatlog__reference-name")!!.text())
            assertEquals(payload, group.selectFirst(".chatlog__reference-link em")!!.text())
        }

        @Test
        fun `escapes command names`() {
            val group = renderMessage(message(interaction = TestInteraction(1, payload, user)))

            assertTrue(group.select("img[onerror]").isEmpty())
            assertEquals(payload, group.selectFirst(".chatlog__command_name")!!.text())
        }

        @Test
        fun `escapes avatar urls`() {
            val author = TestAuthor(1, "user", avatar = "\" onerror=\"alert(1)")
            val avatar = renderMessage(message(author = author)).selectFirst(".chatlog__author-avatar")!!

            assertTrue(!avatar.hasAttr("onerror"))
            assertEquals("\" onerror=\"alert(1)", avatar.attr("src"))
        }
    }
}
