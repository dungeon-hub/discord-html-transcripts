package net.dungeonhub.transcripts

import net.dungeonhub.transcripts.utils.format.impl.AudioFormat
import net.dungeonhub.transcripts.utils.format.impl.ImageFormat
import net.dungeonhub.transcripts.utils.format.impl.VideoFormat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class TranscriptComponentsTest {
    @Test
    fun `attachments render image video audio and generic variants`() {
        val image = TestAttachment("photo.PNG", true, "https://example.com/photo.png", 10).transcriptify()
        val video = TestAttachment("clip.MP4", false, "https://example.com/clip.mp4", 20).transcriptify()
        val audio = TestAttachment("sound.ogg", false, "https://example.com/sound.ogg", 30).transcriptify()
        val generic = TestAttachment("notes.txt", false, "https://example.com/notes.txt", 1024).transcriptify()

        assertEquals("img", image.selectFirst(".chatlog__attachment-media")!!.tagName())
        assertEquals("video", video.selectFirst(".chatlog__attachment-media")!!.tagName())
        assertTrue(video.selectFirst("video")!!.hasAttr("controls"))
        assertEquals("audio", audio.selectFirst(".chatlog__attachment-media")!!.tagName())
        assertEquals("notes.txt", generic.selectFirst(".chatlog__attachment-generic-name a")!!.text())
        assertEquals("1.0 KB", generic.selectFirst(".chatlog__attachment-generic-size")!!.text())
    }

    @Test
    fun `format helpers recognize extensions without regard to case`() {
        assertTrue(ImageFormat().isFormat("JPEG"))
        assertTrue(VideoFormat().isFormat("WebM"))
        assertTrue(AudioFormat().isFormat("FLAC"))
        assertFalse(AudioFormat().isFormat("txt"))
    }
}
