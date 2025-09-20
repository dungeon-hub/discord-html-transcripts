package net.dungeonhub.transcripts.utils.format.impl

import net.dungeonhub.transcripts.utils.format.IFormatHelper

class VideoFormat : IFormatHelper {
    val formats: MutableList<String> = mutableListOf("mp4", "webm", "mkv", "avi", "mov", "flv", "wmv", "mpg", "mpeg")

    override fun formats(): MutableList<String> {
        return formats
    }
}
