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
    private val CODE_1: Pattern = Pattern.compile("`(.+?)`")
    private val QUOTE: Pattern = Pattern.compile("^>{1,3} (.*)$")
    private val LINK: Pattern = Pattern.compile("\\[([^\\[]+)\\](\\((www|http:|https:)+[^\\s]+[\\w]\\))")
    private val NEW_LINE: Pattern = Pattern.compile("\\n")
    // Discord mention patterns: user (<@id> or <@!id>), role (<@&id>), channel (<#id>)
    private val MENTION_USER: Pattern = Pattern.compile("<@!?(\\d+)>")
    private val MENTION_ROLE: Pattern = Pattern.compile("<@&(\\d+)>")
    private val MENTION_CHANNEL: Pattern = Pattern.compile("<#(\\d+)>")

    fun formatBytes(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    private fun preprocessEscapes(text: String): String {
        val sb = java.lang.StringBuilder()
        var i = 0
        val len = text.length
        while (i < len) {
            val c = text[i]
            if (c == '\\' && i + 1 < len) {
                val next = text[i + 1]
                val escaped = when (next) {
                    '\\' -> "\uE000"
                    '*' -> "\uE001"
                    '_' -> "\uE002"
                    '~' -> "\uE003"
                    '`' -> "\uE004"
                    '>' -> "\uE005"
                    '<' -> "\uE006"
                    '[' -> "\uE007"
                    ']' -> "\uE008"
                    '(' -> "\uE009"
                    ')' -> "\uE00A"
                    '#' -> "\uE00B"
                    '@' -> "\uE00C"
                    else -> null
                }
                if (escaped != null) {
                    sb.append(escaped)
                    i += 2
                    continue
                }
            }
            sb.append(c)
            i++
        }
        return sb.toString()
    }

    private fun escapeHtml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
    }

    suspend fun format(originalText: String, server: DiscordServer? = null): String {
        var t = originalText

        val codeBlocks = mutableListOf<String>()
        var codeIndex = 0

        // Find multiline code blocks first
        val multilineMatcher = Pattern.compile("```([\\s\\S]*?)```").matcher(t)
        val sbMultiline = java.lang.StringBuffer()
        while (multilineMatcher.find()) {
            codeBlocks.add("<div class=\"pre pre--multiline nohighlight\">" + formatCodeBlock(escapeHtml(multilineMatcher.group(1))) + "</div>")
            multilineMatcher.appendReplacement(sbMultiline, Matcher.quoteReplacement("\uE200_CODE_${codeIndex++}\uE201"))
        }
        multilineMatcher.appendTail(sbMultiline)
        t = sbMultiline.toString()

        // Process inline code blocks
        val inlineMatcher = Pattern.compile("`([^`]+?)`").matcher(t)
        val sbInline = java.lang.StringBuffer()
        while (inlineMatcher.find()) {
            codeBlocks.add("<span class=\"pre pre--inline\">${escapeHtml(inlineMatcher.group(1))}</span>")
            inlineMatcher.appendReplacement(sbInline, Matcher.quoteReplacement("\uE200_CODE_${codeIndex++}\uE201"))
        }
        inlineMatcher.appendTail(sbInline)
        t = sbInline.toString()

        // Preprocess escapes on the remaining text
        t = preprocessEscapes(t)

        val mentionsList = mutableListOf<String>()
        var mentionIndex = 0

        suspend fun resolve(pattern: Pattern, prefix: Char, resolver: suspend (Long) -> String?) {
            val matcher = pattern.matcher(t)
            val sb = java.lang.StringBuffer()
            while (matcher.find()) {
                val idStr = matcher.group(1)
                val id = idStr.toLongOrNull()
                val label = if (id != null) {
                    resolver(id)?.let { "$prefix$it" } ?: "$prefix$idStr"
                } else {
                    "$prefix$idStr"
                }
                val resolvedHtml = "<span class=\"mention\">${escapeHtml(label)}</span>"
                mentionsList.add(resolvedHtml)
                matcher.appendReplacement(sb, Matcher.quoteReplacement("\uE100_MENTION_${mentionIndex++}\uE101"))
            }
            matcher.appendTail(sb)
            t = sb.toString()
        }

        resolve(MENTION_USER, '@') { server?.getMemberName(it) }
        resolve(MENTION_ROLE, '@') { server?.getRoleName(it) }
        resolve(MENTION_CHANNEL, '#') { server?.getChannelName(it) }

        // Escape HTML on the remaining text
        t = escapeHtml(t)

        // Apply Markdown formatting
        var matcher = STRONG.matcher(t)
        while (matcher.find()) {
            val group = matcher.group()
            t = t.replace(
                group,
                "<strong>" + group.replace("**", "") + "</strong>"
            )
        }
        matcher = EM.matcher(t)
        while (matcher.find()) {
            val group = matcher.group()
            t = t.replace(
                group,
                "<em>" + group.replace("*", "") + "</em>"
            )
        }
        matcher = S.matcher(t)
        while (matcher.find()) {
            val group = matcher.group()
            t = t.replace(
                group,
                "<s>" + group.replace("~~", "") + "</s>"
            )
        }
        matcher = U.matcher(t)
        while (matcher.find()) {
            val group = matcher.group()
            t = t.replace(
                group,
                "<u>" + group.replace("__", "") + "</u>"
            )
        }
        matcher = QUOTE.matcher(t)
        while (matcher.find()) {
            val group = matcher.group()
            t = t.replace(
                group,
                "<span class=\"quote\">" + group.replaceFirst(">>>".toRegex(), "")
                    .replaceFirst(">".toRegex(), "") + "</span>"
            )
        }
        matcher = LINK.matcher(t)
        while (matcher.find()) {
            val group = matcher.group(1)
            val link = matcher.group(2)
            val raw = "[" + group + "]" + link
            t = t.replace(raw, "<a href=\"" + link.replace("(", "").replace(")", "") + "\">" + group + "</a>")
        }

        matcher = NEW_LINE.matcher(t)
        while (matcher.find()) {
            t = t.replace(matcher.group(), "<br />")
        }

        // Restore code blocks
        for (idx in codeBlocks.indices) {
            t = t.replace("\uE200_CODE_${idx}\uE201", codeBlocks[idx])
        }

        // Restore mentions
        for (idx in mentionsList.indices) {
            t = t.replace("\uE100_MENTION_${idx}\uE101", mentionsList[idx])
        }

        // Restore backslash escapes
        t = t.replace("\uE000", "\\")
            .replace("\uE001", "*")
            .replace("\uE002", "_")
            .replace("\uE003", "~")
            .replace("\uE004", "`")
            .replace("\uE005", "&gt;")
            .replace("\uE006", "&lt;")
            .replace("\uE007", "[")
            .replace("\uE008", "]")
            .replace("\uE009", "(")
            .replace("\uE00A", ")")
            .replace("\uE00B", "#")
            .replace("\uE00C", "@")

        return t
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
