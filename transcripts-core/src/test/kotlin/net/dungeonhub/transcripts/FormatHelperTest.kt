package net.dungeonhub.transcripts

import net.dungeonhub.transcripts.utils.format.impl.AudioFormat
import net.dungeonhub.transcripts.utils.format.impl.ImageFormat
import net.dungeonhub.transcripts.utils.format.impl.VideoFormat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FormatHelperTest {
    @Test
    fun `format helpers recognize extensions without regard to case`() {
        assertTrue(ImageFormat().isFormat("JPEG"))
        assertTrue(VideoFormat().isFormat("WebM"))
        assertTrue(AudioFormat().isFormat("FLAC"))
        assertFalse(AudioFormat().isFormat("txt"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["png", "jpg", "jpeg", "gif"])
    fun `image formats`(extension: String) {
        assertTrue(ImageFormat().isFormat(extension))
        assertFalse(VideoFormat().isFormat(extension))
        assertFalse(AudioFormat().isFormat(extension))
    }

    @ParameterizedTest
    @ValueSource(strings = ["mp4", "webm", "mkv", "avi", "mov", "flv", "wmv", "mpg", "mpeg"])
    fun `video formats`(extension: String) {
        assertTrue(VideoFormat().isFormat(extension))
        assertFalse(ImageFormat().isFormat(extension))
        assertFalse(AudioFormat().isFormat(extension))
    }

    @ParameterizedTest
    @ValueSource(strings = ["mp3", "wav", "ogg", "flac"])
    fun `audio formats`(extension: String) {
        assertTrue(AudioFormat().isFormat(extension))
        assertFalse(ImageFormat().isFormat(extension))
        assertFalse(VideoFormat().isFormat(extension))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "txt", ".png", "png ", "mp", "mp44"])
    fun `unknown or malformed extensions match nothing`(extension: String) {
        assertFalse(ImageFormat().isFormat(extension))
        assertFalse(VideoFormat().isFormat(extension))
        assertFalse(AudioFormat().isFormat(extension))
    }
}
