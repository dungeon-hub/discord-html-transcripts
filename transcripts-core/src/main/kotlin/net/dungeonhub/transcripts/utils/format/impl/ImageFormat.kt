package net.dungeonhub.transcripts.utils.format.impl

import net.dungeonhub.transcripts.utils.format.IFormatHelper

class ImageFormat : IFormatHelper {
    val formats: MutableList<String> = mutableListOf("png", "jpg", "jpeg", "gif")

    override fun formats(): MutableList<String> {
        return formats
    }
}
