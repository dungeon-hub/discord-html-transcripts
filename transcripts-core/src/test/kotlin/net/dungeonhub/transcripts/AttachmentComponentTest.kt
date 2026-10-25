package net.dungeonhub.transcripts

import net.dungeonhub.transcripts.components.attachment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class AttachmentComponentTest {
    private fun renderAttachment(fileName: String, isImage: Boolean = false, size: Long = 1024) =
        render { attachment(TestAttachment(fileName, isImage, "https://example.com/$fileName", size)) }

    @Test
    fun `wraps every attachment in a single attachment container`() {
        val rendered = renderAttachment("file.txt")

        assertEquals(1, rendered.childrenSize())
        assertEquals("chatlog__attachment", rendered.child(0).className())
    }

    @Test
    fun `renders images as lazy loaded linked img`() {
        val image = renderAttachment("photo.png", isImage = true, size = 2048)
            .selectFirst(".chatlog__attachment > a > img.chatlog__attachment-media")

        assertNotNull(image)
        assertEquals("https://example.com/photo.png", image!!.attr("src"))
        assertEquals("Image attachment", image.attr("alt"))
        assertEquals("lazy", image.attr("loading"))
        assertEquals("Image: photo.png (2.0 KB)", image.attr("title"))
    }

    @Test
    fun `isImage takes precedence over the file extension`() {
        val rendered = renderAttachment("clip.mp4", isImage = true)

        assertEquals("img", rendered.selectFirst(".chatlog__attachment-media")!!.tagName())
        assertTrue(rendered.select("video").isEmpty())
    }

    @ParameterizedTest
    @ValueSource(strings = ["mp4", "webm", "mkv", "avi", "mov", "flv", "wmv", "mpg", "mpeg", "MP4", "WebM"])
    fun `renders video formats as video player`(extension: String) {
        val video = renderAttachment("clip.$extension").selectFirst(".chatlog__attachment-media")!!

        assertEquals("video", video.tagName())
        assertTrue(video.hasAttr("controls"))
        assertEquals("https://example.com/clip.$extension", video.attr("src"))
        assertEquals("Video attachment", video.attr("alt"))
        assertEquals("Video: clip.$extension (1.0 KB)", video.attr("title"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["mp3", "wav", "ogg", "flac", "MP3", "Flac"])
    fun `renders audio formats as audio player`(extension: String) {
        val audio = renderAttachment("sound.$extension").selectFirst(".chatlog__attachment-media")!!

        assertEquals("audio", audio.tagName())
        assertTrue(audio.hasAttr("controls"))
        assertEquals("https://example.com/sound.$extension", audio.attr("src"))
        assertEquals("Audio attachment", audio.attr("alt"))
        assertEquals("Audio: sound.$extension (1.0 KB)", audio.attr("title"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["notes.txt", "archive.tar.gz", "README", "trailing.", "image.png", "mp4"])
    fun `renders everything else as generic download`(fileName: String) {
        val rendered = renderAttachment(fileName, size = 512)

        assertNull(rendered.selectFirst(".chatlog__attachment-media"))
        val generic = rendered.selectFirst(".chatlog__attachment-generic")!!
        assertEquals(fileName, generic.selectFirst(".chatlog__attachment-generic-name a")!!.text())
        assertEquals("https://example.com/$fileName", generic.selectFirst(".chatlog__attachment-generic-name a")!!.attr("href"))
        assertEquals("512 B", generic.selectFirst(".chatlog__attachment-generic-size")!!.text())
    }

    @Test
    fun `generic attachments reference the attachment icon`() {
        val icon = renderAttachment("notes.txt").selectFirst("svg.chatlog__attachment-generic-icon")!!

        assertEquals("#icon-attachment", icon.selectFirst("use")!!.attr("xlink:href"))
    }

    @Test
    fun `uses only the last extension of a file name`() {
        assertEquals("video", renderAttachment("archive.txt.mp4").selectFirst(".chatlog__attachment-media")!!.tagName())
        assertNull(renderAttachment("clip.mp4.txt").selectFirst(".chatlog__attachment-media"))
    }

    @Test
    fun `escapes file names`() {
        val fileName = "<img src=x onerror=alert(1)>.txt"
        val rendered = renderAttachment(fileName)

        assertTrue(rendered.select("img").isEmpty())
        assertEquals(fileName, rendered.selectFirst(".chatlog__attachment-generic-name a")!!.text())
    }

    @Test
    fun `escapes urls in attributes`() {
        val rendered = render {
            attachment(TestAttachment("a.png", true, "https://example.com/\" onerror=\"alert(1)", 1))
        }
        val image = rendered.selectFirst("img")!!

        assertEquals("https://example.com/\" onerror=\"alert(1)", image.attr("src"))
        assertTrue(!image.hasAttr("onerror"))
    }
}
