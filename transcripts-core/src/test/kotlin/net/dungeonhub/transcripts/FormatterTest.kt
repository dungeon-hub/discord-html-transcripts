package net.dungeonhub.transcripts

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.awt.Color
import java.util.Locale

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

    @ParameterizedTest
    @CsvSource(
        "'',''",
        "plain text,plain text",
        "**bold**,<strong>bold</strong>",
        "*italic*,<em>italic</em>",
        "~~strike~~,<s>strike</s>",
        "__underline__,<u>underline</u>",
        "`code`,<span class=\"pre pre--inline\">code</span>",
        "**a** and **b**,<strong>a</strong> and <strong>b</strong>",
        "**unclosed,**unclosed",
        "a * b * c,a <em> b </em> c"
    )
    fun `format handles inline markdown`(input: String, expected: String) {
        assertEquals(expected, Formatter.format(input))
    }

    @ParameterizedTest
    @CsvSource(
        "'> one','<span class=\"quote\"> one</span>'",
        "'>> two','<span class=\"quote\"> two</span>'",
        "'>>> three','<span class=\"quote\"> three</span>'",
        "'>>>> four','&gt;&gt;&gt;&gt; four'",
        "'>no space','&gt;no space'",
        "'a > b','a &gt; b'"
    )
    fun `format only treats leading angle brackets followed by a space as quotes`(input: String, expected: String) {
        assertEquals(expected, Formatter.format(input))
    }

    @Test
    fun `format converts newlines to line breaks`() {
        assertEquals("one<br />two<br /><br />three", Formatter.format("one\ntwo\n\nthree"))
    }

    @Test
    fun `format supports http and www links`() {
        assertEquals("<a href=\"http://example.com\">plain</a>", Formatter.format("[plain](http://example.com)"))
        assertEquals("<a href=\"www.example.com\">www</a>", Formatter.format("[www](www.example.com)"))
    }

    @Test
    fun `format does not turn non-web links into anchors`() {
        val formatted = Formatter.format("[click](javascript:alert(1))")

        assertFalse(formatted.contains("<a"))
        assertEquals("[click](javascript:alert(1))", formatted)
    }

    @Test
    fun `format applies markdown inside link text`() {
        assertEquals(
            "<a href=\"https://example.com\"><strong>bold</strong></a>",
            Formatter.format("[**bold**](https://example.com)")
        )
    }

    @Test
    fun `format escapes html in plain text`() {
        assertEquals(
            "&lt;script&gt;alert(&quot;x&quot;)&lt;/script&gt; &amp; more",
            Formatter.format("<script>alert(\"x\")</script> & more")
        )
    }

    @Test
    fun `format escapes html inside markdown`() {
        assertEquals("<strong>&lt;b&gt;</strong>", Formatter.format("**<b>**"))
        assertEquals("<span class=\"pre pre--inline\">&lt;div&gt;</span>", Formatter.format("`<div>`"))
        assertEquals(
            "<div class=\"pre pre--multiline nohighlight\">if (a &lt; b &amp;&amp; c &gt; d) {}</div>",
            Formatter.format("```if (a < b && c > d) {}```")
        )
    }

    @Test
    fun `format cannot break out of link attributes`() {
        val formatted = Formatter.format("[x](https://example.com/\"onmouseover=\"alert(1))")

        assertFalse(formatted.contains("\" onmouseover"))
        assertFalse(formatted.contains("href=\"https://example.com/\""))
    }

    @Test
    fun `format keeps markdown characters inside multiline code blocks`() {
        assertEquals(
            "<div class=\"pre pre--multiline nohighlight\">line one<br />line two</div>",
            Formatter.format("```\nline one\nline two\n```")
        )
    }

    @Test
    fun `format leaves inline code untouched when a code block is present`() {
        assertEquals(
            "<div class=\"pre pre--multiline nohighlight\">block</div> `inline`",
            Formatter.format("```block``` `inline`")
        )
    }

    @Test
    fun `formatCodeBlock strips fences and leading blank lines`() {
        assertEquals("first\n\nsecond", Formatter.formatCodeBlock("```\n\n   \nfirst\n\nsecond\n```"))
        assertEquals("single", Formatter.formatCodeBlock("```single```"))
    }

    @Test
    fun `escapeHtml escapes special characters`() {
        assertEquals("&lt;a href=&quot;x&quot;&gt;&amp;&lt;/a&gt;", Formatter.escapeHtml("<a href=\"x\">&</a>"))
        assertEquals("it's fine", Formatter.escapeHtml("it's fine"))
    }

    @Test
    fun `escapeHtml escapes ampersands only once`() {
        assertEquals("&amp;lt;", Formatter.escapeHtml("&lt;"))
    }

    @Test
    fun `formatBytes selects readable binary units`() {
        assertEquals("512 B", Formatter.formatBytes(512))
        assertEquals("1.0 KB", Formatter.formatBytes(1024))
        assertEquals("1.5 MB", Formatter.formatBytes(1_572_864))
    }

    @ParameterizedTest
    @CsvSource(
        "0,0 B",
        "1,1 B",
        "1023,1023 B",
        "1536,1.5 KB",
        "1073741824,1.0 GB",
        "1099511627776,1.0 TB",
        "1125899906842624,1.0 PB",
        "1152921504606846976,1.0 EB"
    )
    fun `formatBytes covers every unit`(bytes: Long, expected: String) {
        assertEquals(expected, Formatter.formatBytes(bytes))
    }

    @Test
    fun `formatBytes does not depend on the default locale`() {
        val defaultLocale = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            assertEquals("1.5 KB", Formatter.formatBytes(1536))
        } finally {
            Locale.setDefault(defaultLocale)
        }
    }

    @Test
    fun `toHex includes leading zeroes`() {
        assertEquals("01020f", Formatter.toHex(Color(1, 2, 15)))
    }

    @ParameterizedTest
    @CsvSource(
        "0,0,0,000000",
        "255,255,255,ffffff",
        "18,52,86,123456",
        "0,0,1,000001"
    )
    fun `toHex produces lowercase six digit hex`(red: Int, green: Int, blue: Int, expected: String) {
        assertEquals(expected, Formatter.toHex(Color(red, green, blue)))
    }

    @Test
    fun `toHex ignores the alpha channel`() {
        assertEquals("abcdef", Formatter.toHex(Color(0xAB, 0xCD, 0xEF, 0x10)))
    }
}
