package net.dungeonhub.transcripts

import kotlin.math.pow

/**
 * Created by Ryzeon
 * Project: discord-html-transcripts
 * Date: 2/12/21 @ 00:32
 * Twitter: @Ryzeon_ 😎
 * Github: github.ryzeon.me
 */
object Formatter {
    private val STRONG: java.util.regex.Pattern = java.util.regex.Pattern.compile("\\*\\*(.+?)\\*\\*")
    private val EM: java.util.regex.Pattern = java.util.regex.Pattern.compile("\\*(.+?)\\*")
    private val S: java.util.regex.Pattern = java.util.regex.Pattern.compile("~~(.+?)~~")
    private val U: java.util.regex.Pattern = java.util.regex.Pattern.compile("__(.+?)__")
    private val CODE: java.util.regex.Pattern = java.util.regex.Pattern.compile("```[\\S\\s]*?```")
    private val CODE_1: java.util.regex.Pattern = java.util.regex.Pattern.compile("`[^`]++`")

    // conver this /(?:\r\n|\r|\n)/g to patter in java
    private val NEW_LINE: java.util.regex.Pattern = java.util.regex.Pattern.compile("\\n")

    fun formatBytes(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (kotlin.math.ln(bytes.toDouble()) / kotlin.math.ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1].toString() + ""
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
        var result = group.replace("```", "")

        val empty = java.util.concurrent.atomic.AtomicBoolean(true)
        result = java.util.Arrays.stream(result.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
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
            }.collect(java.util.stream.Collectors.joining("\n"))

        return result
    }

    fun toHex(color: java.awt.Color): String {
        val hex = java.lang.StringBuilder(Integer.toHexString(color.rgb and 0xffffff))
        while (hex.length < 6) {
            hex.insert(0, "0")
        }
        return hex.toString()
    }
}
