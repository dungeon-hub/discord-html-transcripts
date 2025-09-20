package net.dungeonhub.transcripts.utils.format.impl

import net.dungeonhub.transcripts.utils.format.IFormatHelper

class AudioFormat : IFormatHelper {
    val formats: MutableList<String> = mutableListOf("mp3", "wav", "ogg", "flac")

    override fun formats(): MutableList<String> {
        return formats
    }
}
