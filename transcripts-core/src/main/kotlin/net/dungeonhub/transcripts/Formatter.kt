package net.dungeonhub.transcripts

import net.dungeonhub.wrapper.DiscordServer
import java.awt.Color
import java.util.Arrays
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.math.ln
import kotlin.math.pow

/**
 * Created by Ryzeon
 * Modified by Taubsie, for Kotlin and greater Framework support
 * Project: discord-html-transcripts
 * Date: 2/12/21 @ 00:32
 * Twitter: @Ryzeon_ 😎
 * Github: github.ryzeon.me
 */
object Formatter {
    private val STRONG: Pattern = Pattern.compile("\\*\\*(.+?)\\*\\*")
    private val EM: Pattern = Pattern.compile("\\*(.+?)\\*")
    private val S: Pattern = Pattern.compile("~~(.+?)~~")
    private val U: Pattern = Pattern.compile("__(.+?)__")
    private val CODE: Pattern = Pattern.compile("```(.+?)```", Pattern.DOTALL)
    private val CODE_1: Pattern = Pattern.compile("`([^`]+?)`")
    // matched after html escaping, so the quote marker is "&gt;" instead of ">"
    private val QUOTE: Pattern = Pattern.compile("^(?:&gt;){1,3} (.*)$", Pattern.MULTILINE)
    private val LINK: Pattern = Pattern.compile("\\[([^\\[]+)\\](\\((www|http:|https:)+[^\\s]+[\\w]\\))")
    private val NEW_LINE: Pattern = Pattern.compile("\\n")
    // Discord mention patterns: user (<@id> or <@!id>), role (<@&id>), channel (<#id>)
    private val MENTION_USER: Pattern = Pattern.compile("<@!?(\\d+)>")
    private val MENTION_ROLE: Pattern = Pattern.compile("<@&(\\d+)>")
    private val MENTION_CHANNEL: Pattern = Pattern.compile("<#(\\d+)>")
    // a backslash in front of one of these characters removes its markdown / mention meaning
    private val ESCAPED_CHARACTER: Pattern = Pattern.compile("\\\\([\\\\*_~`<>\\[\\]()#@])")

    /*
     * Code blocks, mentions and escaped characters are rendered first and swapped out for a placeholder,
     * so that the markdown patterns can't modify them. The placeholders are swapped back at the very end.
     * Discord messages can't contain the NUL character, which makes it safe to use as the delimiter.
     */
    private const val PLACEHOLDER_DELIMITER = "\u0000"
    private val PLACEHOLDER: Pattern = Pattern.compile("$PLACEHOLDER_DELIMITER(\\d+)$PLACEHOLDER_DELIMITER")

    fun formatBytes(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }

    private class Placeholders {
        private val replacements = mutableListOf<String>()

        /**
         * Replaces every match of [pattern] in [text] with a placeholder for the html returned by [toHtml].
         */
        suspend fun replace(text: String, pattern: Pattern, toHtml: suspend (Matcher) -> String): String {
            val matcher = pattern.matcher(text)
            val result = StringBuilder()
            while (matcher.find()) {
                replacements.add(toHtml(matcher))
                val placeholder = "$PLACEHOLDER_DELIMITER${replacements.size - 1}$PLACEHOLDER_DELIMITER"
                matcher.appendReplacement(result, Matcher.quoteReplacement(placeholder))
            }
            matcher.appendTail(result)
            return result.toString()
        }

        fun restore(text: String): String {
            return PLACEHOLDER.matcher(text).replaceAll {
                Matcher.quoteReplacement(replacements[it.group(1).toInt()])
            }
        }
    }

    suspend fun format(originalText: String, server: DiscordServer? = null): String {
        val placeholders = Placeholders()

        // the delimiter must not appear in the input, otherwise it could be mistaken for a placeholder
        var newText = originalText.replace(PLACEHOLDER_DELIMITER, "")

        newText = placeholders.replace(newText, CODE) {
            "<div class=\"pre pre--multiline nohighlight\">${formatCodeBlock(escapeHtml(it.group(1)))}</div>"
        }
        newText = placeholders.replace(newText, CODE_1) {
            "<span class=\"pre pre--inline\">${escapeHtml(it.group(1))}</span>"
        }
        newText = placeholders.replace(newText, ESCAPED_CHARACTER) { escapeHtml(it.group(1)) }

        suspend fun resolveMentions(pattern: Pattern, prefix: Char, resolver: suspend (Long) -> String?) {
            newText = placeholders.replace(newText, pattern) {
                val id = it.group(1)
                val name = id.toLongOrNull()?.let { parsedId -> resolver(parsedId) } ?: id
                "<span class=\"mention\">${escapeHtml("$prefix$name")}</span>"
            }
        }

        resolveMentions(MENTION_USER, '@') { server?.getMemberName(it) }
        resolveMentions(MENTION_ROLE, '@') { server?.getRoleName(it) }
        resolveMentions(MENTION_CHANNEL, '#') { server?.getChannelName(it) }

        newText = escapeHtml(newText)

        var matcher = STRONG.matcher(newText)
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                "<strong>" + group.replace("**", "") + "</strong>"
            )
        }
        matcher = EM.matcher(newText)
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                "<em>" + group.replace("*", "") + "</em>"
            )
        }
        matcher = S.matcher(newText)
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                "<s>" + group.replace("~~", "") + "</s>"
            )
        }
        matcher = U.matcher(newText)
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                "<u>" + group.replace("__", "") + "</u>"
            )
        }
        matcher = QUOTE.matcher(newText)
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                "<span class=\"quote\">" + matcher.group(1) + "</span>"
            )
        }
        matcher = LINK.matcher(newText)
        while (matcher.find()) {
            val group = matcher.group(1)
            val link = matcher.group(2)
            val raw = "[" + group + "]" + link

            newText =
                newText.replace(raw, "<a href=\"" + link.replace("(", "").replace(")", "") + "\">" + group + "</a>")
        }

        matcher = NEW_LINE.matcher(newText)
        while (matcher.find()) {
            newText = newText.replace(matcher.group(), "<br />")
        }

        return placeholders.restore(newText)
    }

    fun formatCodeBlock(group: String): String {
        var result = group.replace("```", "").trim()

        val empty = AtomicBoolean(true)
        result = Arrays.stream(result.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .sequential().filter { s: String ->
                if (empty.get()) {
                    if (s.isBlank()) {
                        return@filter false
                    } else {
                        empty.set(false)
                        return@filter true
                    }
                } else {
                    return@filter true
                }
            }.collect(Collectors.joining("\n"))

        return result
    }

    fun toHex(color: Color): String {
        var hex = Integer.toHexString(color.rgb and 0xffffff)
        while (hex.length < 6) {
            hex = "0${hex}"
        }
        return hex
    }
}
