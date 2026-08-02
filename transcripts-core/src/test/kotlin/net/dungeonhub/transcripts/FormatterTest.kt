package net.dungeonhub.transcripts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color

class FormatterTest {
    @Test
    fun `format converts supported Discord markdown`() {
        assertEquals(
            "<strong>bold</strong> <em>italic</em> <s>strike</s> <u>underline</u> " +
                "<span class=\"pre pre--inline\">code</span><br />next",
            Formatter.format("**bold** *italic* ~~strike~~ __underline__ `code`\nnext")
        )
    }

    @Test
    fun `format converts links quotes and multiline code`() {
        assertEquals("<a href=\"https://example.com\">site</a>", Formatter.format("[site](https://example.com)"))
        assertEquals("<span class=\"quote\"> quoted</span>", Formatter.format("> quoted"))
        assertEquals(
            "<div class=\"pre pre--multiline nohighlight\">val answer = 42</div>",
            Formatter.format("```\n\nval answer = 42\n```")
        )
    }

    @Test
    fun `formatBytes selects readable binary units`() {
        assertEquals("512 B", Formatter.formatBytes(512))
        assertEquals("1.0 KB", Formatter.formatBytes(1024))
        assertEquals("1.5 MB", Formatter.formatBytes(1_572_864))
    }

    @Test
    fun `toHex includes leading zeroes`() {
        assertEquals("01020f", Formatter.toHex(Color(1, 2, 15)))
    }
}
