package net.dungeonhub.transcripts

import java.awt.Color
import java.util.Arrays
import java.util.concurrent.atomic.AtomicBoolean
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
    private val CODE: Pattern = Pattern.compile("```(.+?)```")
    private val CODE_1: Pattern = Pattern.compile("`(.+?)`")
    private val QUOTE: Pattern = Pattern.compile("^>{1,3} (.*)$")
    private val LINK: Pattern = Pattern.compile("\\[([^\\[]+)\\](\\((www|http:|https:)+[^\\s]+[\\w]\\))")
    private val NEW_LINE: Pattern = Pattern.compile("\\n")

    fun formatBytes(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString()
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }

    fun format(originalText: String): String {
        var matcher = STRONG.matcher(originalText)
        var newText = originalText
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
                "<span class=\"quote\">" + group.replaceFirst(">>>".toRegex(), "")
                    .replaceFirst(">".toRegex(), "") + "</span>"
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

        matcher = CODE.matcher(newText)
        var findCode = false
        while (matcher.find()) {
            val group = matcher.group()
            newText = newText.replace(
                group,
                ("<div class=\"pre pre--multiline nohighlight\">"
                        + formatCodeBlock(group) + "</div>")
            )
            findCode = true
        }
        if (!findCode) {
            matcher = CODE_1.matcher(newText)
            while (matcher.find()) {
                val group = matcher.group()
                newText = newText.replace(
                    group,
                    "<span class=\"pre pre--inline\">" + group.replace("`", "") + "</span>"
                )
            }
        }
        matcher = NEW_LINE.matcher(newText)
        while (matcher.find()) {
            newText = newText.replace(matcher.group(), "<br />")
        }
        return newText
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
