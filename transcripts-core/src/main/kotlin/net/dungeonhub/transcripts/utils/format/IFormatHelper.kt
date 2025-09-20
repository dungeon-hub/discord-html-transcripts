package net.dungeonhub.transcripts.utils.format

interface IFormatHelper {
    fun formats(): MutableList<String>

    fun isFormat(format: String): Boolean {
        return formats().any { it.equals(format, ignoreCase = true) }
    }
}
