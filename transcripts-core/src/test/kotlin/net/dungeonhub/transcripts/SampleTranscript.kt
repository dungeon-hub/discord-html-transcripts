package net.dungeonhub.transcripts

import java.awt.Color
import java.io.File
import java.time.Duration
import java.time.Instant

/**
 * A transcript with every kind of content the renderer supports plus known edge cases.
 * CI renders it with `./gradlew :transcripts-core:generateSampleTranscript` so reviewers can look at real output.
 */
object SampleTranscript {
    private val start: Instant = Instant.parse("2024-05-20T09:00:00Z")
    private var minutes = 0L
    private var nextId = 0L

    private fun next(): Instant = start.plus(Duration.ofMinutes(minutes++))

    private fun avatar(index: Int) = "https://cdn.discordapp.com/embed/avatars/$index.png"
    private fun picture(seed: String, width: Int = 640, height: Int = 360) =
        "https://picsum.photos/seed/$seed/$width/$height"

    private const val VIDEO = "https://interactive-examples.mdn.mozilla.net/media/cc0-videos/flower.mp4"
    private const val AUDIO = "https://interactive-examples.mdn.mozilla.net/media/cc0-audio/t-rex-roar.mp3"
    private const val XSS = "<img src=x onerror=alert('xss')><script>alert('xss')</script>"

    private val alice = TestAuthor(1001, "alice", "Alice", Color(0x1A, 0xBC, 0x9C), avatar(0))
    private val bob = TestAuthor(1002, "bob", "Bob the Builder", Color(0xE9, 0x1E, 0x63), avatar(1))
    private val plainUser = TestAuthor(1003, "no-role", "No Role Color", Color.WHITE, avatar(2))
    private val darkUser = TestAuthor(1004, "dark", "Black Role Color", Color.BLACK, avatar(3))
    private val bot = TestAuthor(2001, "helper-bot", "Helper Bot", Color(0x58, 0x65, 0xF2), avatar(4), isBot = true)
    private val longName = TestAuthor(
        1005,
        "a-user-with-an-extremely-long-username-that-keeps-going",
        "A display name that is far longer than anything Discord would usually show in one line",
        Color(0xF1, 0xC4, 0x0F),
        avatar(5)
    )
    private val evilUser = TestAuthor(1006, "evil$XSS", "Evil $XSS", Color.RED, avatar(0))

    private val application = TestApplication(3001, "transcript-app", "Transcript App", avatar = "a_animatedhash")
    private val urlApplication = TestApplication(3002, "url-app", "App With URL Avatar", avatar = avatar(1))

    private fun message(
        content: String,
        author: TestAuthor? = alice,
        embeds: List<TestEmbed?> = emptyList(),
        attachments: List<TestAttachment> = emptyList(),
        reference: TestMessage? = null,
        interaction: TestInteraction? = null,
        time: Instant = next()
    ) = TestMessage(++nextId, content, time, author, embeds, attachments, reference, interaction)

    fun messages(): List<TestMessage> {
        minutes = 0
        nextId = 0
        val messages = mutableListOf<TestMessage>()
        fun add(message: TestMessage) = message.also { messages += it }

        // --- Plain text and markdown ---
        add(message("Welcome to the sample transcript! Every section below exercises a different part of the renderer."))
        add(message("**bold** *italic* ~~strikethrough~~ __underline__ `inline code` and [a link](https://github.com/dungeon-hub/discord-html-transcripts)", bob))
        add(message("> single quote", plainUser))
        add(message(">> double quote marker", plainUser))
        add(message(">>> triple quote marker", plainUser))
        add(message("```\nfun main() {\n    println(\"Hello, transcript!\")\n}\n```", bob))
        add(message("```kotlin\nval list = listOf(1, 2, 3) // language hint on first line\n```", bob))
        add(message("First line\nSecond line\n\nAfter an empty line", darkUser))
        add(message("Nested ***bold italic*** markers (known limitation of the regex formatter)", alice))
        add(message("Unclosed **bold and *italic markers stay as text", alice))
        add(message("Characters that need escaping: < > & \" ' and an HTML-looking <b>not bold</b> tag", alice))
        add(message("Unicode: Grüße, こんにちは, Привет, مرحبا, emoji 👋🎉🔥 and flags 🇩🇪🇯🇵", plainUser))
        add(message("A_very_long_word_without_any_spaces_" + "x".repeat(200), darkUser))
        add(message(("Lorem ipsum dolor sit amet, consectetur adipiscing elit. ").repeat(30).trim(), longName))

        // --- Authors ---
        add(message("I am a bot, so I get an APP tag.", bot))
        add(message("This message has no author, so it falls back to the bot placeholder.", null))
        add(message("Role colors: this author has a white role color.", plainUser))

        // --- Replies ---
        val original = add(message("This is the message that gets replied to.", alice))
        add(message("A normal reply.", bob, reference = original))
        val longOriginal = add(message("This message is long enough that its reply preview has to be truncated after a few words.", alice))
        add(message("A reply to a long message.", bob, reference = longOriginal))
        val attachmentOnly = add(
            message("", alice, attachments = listOf(TestAttachment("screenshot.png", true, picture("screenshot"), 245_760)))
        )
        add(message("A reply to an attachment-only message.", bob, reference = attachmentOnly))
        val authorless = add(message("A message without author.", null))
        add(message("A reply to a message without author (empty reference).", bob, reference = authorless))
        add(
            message(
                "A reply to a message that is not part of this transcript.", bob,
                reference = TestMessage(9_999_999, "Deleted or older message", start.minus(Duration.ofDays(3)), plainUser)
            )
        )

        // --- Application commands ---
        add(
            message(
                "Transcript created!", null,
                interaction = TestInteraction(1, "/transcript", alice, application)
            )
        )
        add(
            message(
                "Response from an application whose avatar is already a URL.", null,
                interaction = TestInteraction(2, "/ping", bob, urlApplication)
            )
        )
        add(message("Command response sent by a bot user.", bot, interaction = TestInteraction(3, "/help", plainUser)))

        // --- Attachments ---
        add(message("An image attachment:", alice, attachments = listOf(TestAttachment("cat.jpg", true, picture("cat"), 1_572_864))))
        add(message("A video attachment:", bob, attachments = listOf(TestAttachment("flower.MP4", false, VIDEO, 1_101_004))))
        add(message("An audio attachment:", bob, attachments = listOf(TestAttachment("roar.mp3", false, AUDIO, 70_243))))
        add(
            message(
                "Generic files of every size unit:", plainUser,
                attachments = listOf(
                    TestAttachment("empty.txt", false, "https://example.com/empty.txt", 0),
                    TestAttachment("notes.txt", false, "https://example.com/notes.txt", 512),
                    TestAttachment("report.pdf", false, "https://example.com/report.pdf", 2_621_440),
                    TestAttachment("backup.tar.gz", false, "https://example.com/backup.tar.gz", 5_368_709_120),
                    TestAttachment("LICENSE", false, "https://example.com/LICENSE", 35_149),
                    TestAttachment("trailing-dot.", false, "https://example.com/trailing-dot.", 1)
                )
            )
        )
        add(
            message(
                "", darkUser,
                attachments = listOf(
                    TestAttachment("first.png", true, picture("first", 320, 240), 1024),
                    TestAttachment("second.png", true, picture("second", 240, 320), 2048)
                )
            )
        )

        // --- Embeds ---
        add(
            message(
                "A complete embed:", bot,
                embeds = listOf(
                    TestEmbed(
                        color = Color(0x58, 0x65, 0xF2),
                        title = "Complete **embed** title",
                        description = "Description with *markdown*, `code` and a [link](https://example.com).\nSecond line.",
                        fields = listOf(
                            TestEmbedField("Inline 1", "Value 1", true),
                            TestEmbedField("Inline 2", "**Bold** value", true),
                            TestEmbedField("Inline 3", "`code`", true),
                            TestEmbedField("Full width", "This field is not inline and takes the whole row.", false)
                        ),
                        url = "https://example.com/embed",
                        thumbnail = picture("thumbnail", 160, 160),
                        image = picture("embed-image"),
                        footer = TestEmbedFooter("Footer text", avatar(4)),
                        author = TestEmbedAuthor("Embed Author", avatar(0), "https://example.com/author"),
                        timestamp = Instant.parse("2024-05-20T12:34:56Z")
                    )
                )
            )
        )
        add(
            message(
                "", bot,
                embeds = listOf(
                    TestEmbed(description = "An embed without color, title or anything else."),
                    null,
                    TestEmbed(color = Color(0xED, 0x42, 0x45), title = "Title only, without URL"),
                    TestEmbed(color = Color(0x57, 0xF2, 0x87), author = TestEmbedAuthor("Author without icon or URL", null, null)),
                    TestEmbed(color = Color(0xFE, 0xE7, 0x5C), footer = TestEmbedFooter(null, null), timestamp = start),
                    TestEmbed(color = Color.BLACK, image = picture("image-only"))
                )
            )
        )
        add(
            message(
                "An embed with many fields:", bot,
                embeds = listOf(
                    TestEmbed(
                        color = Color(0xEB, 0x45, 0x9E),
                        title = "Many fields",
                        fields = (1..12).map { TestEmbedField("Field $it", "Value $it", it % 4 != 0) }
                    )
                )
            )
        )

        // --- Injection attempts: everything below must show up as plain text ---
        add(message("Content: $XSS", evilUser))
        add(message("Markdown: **$XSS** `$XSS`\n```\n$XSS\n```", evilUser))
        add(message("Link: [click me](https://example.com/\"onmouseover=\"alert('xss')) and [js](javascript:alert('xss'))", evilUser))
        add(message("Reply preview", bob, reference = message(XSS, evilUser, time = start)))
        add(message("", evilUser, attachments = listOf(TestAttachment("$XSS.txt", false, "https://example.com/\" onclick=\"alert('xss')", 1))))
        add(
            message(
                "", evilUser,
                embeds = listOf(
                    TestEmbed(
                        title = XSS,
                        description = XSS,
                        fields = listOf(TestEmbedField(XSS, XSS, false)),
                        footer = TestEmbedFooter(XSS, null),
                        author = TestEmbedAuthor(XSS, null, null)
                    )
                )
            )
        )
        add(message("Command name", null, interaction = TestInteraction(4, XSS, evilUser, application)))

        // --- Ordering ---
        val sameTime = next()
        add(message("Two messages with the same timestamp keep their order: first", alice, time = sameTime))
        add(message("Two messages with the same timestamp keep their order: second", bob, time = sameTime))
        add(message("This message is passed in first but sent the next day, so it is rendered last.", alice, time = start.plus(Duration.ofDays(1))))
            .also { messages.remove(it); messages.add(0, it) }

        return messages
    }

    fun channel() = TestChannel(
        "sample-transcript",
        TestServer("Sample Server", "https://cdn.discordapp.com/embed/avatars/2.png"),
        messages()
    )

    fun render(): String = DiscordHtmlTranscripts.createTranscript(channel())
}

fun main(args: Array<String>) {
    val output = File(args.firstOrNull() ?: "sample-transcript.html")
    output.parentFile?.mkdirs()
    output.writeText(SampleTranscript.render())
    println("Wrote sample transcript to ${output.absolutePath}")
}
