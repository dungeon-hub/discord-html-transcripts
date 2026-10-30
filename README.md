# Discord HTML Transcripts

Discord HTML Transcripts is a Kotlin module to generate nice looking HTML transcripts for JDA, Javacord and Kord (Kotlin-based Discord Framework). Processes discord markdown like **bold**, *italics*, ~~strikethroughs~~, and more. Nicely formats attachments and embeds. Built in XSS protection, preventing users from inserting html tags. 

**This module is designed to work with [Kord](https://github.com/kordlib/kord), [Javacord](https://github.com/Javacord/Javacord) and [JDA](https://github.com/discord-jda/JDA).**

HTML Template stolen from [DiscordChatExporter](https://github.com/Tyrrrz/DiscordChatExporter).

## Installation

```xml
<dependency>
    <groupId>net.dungeon-hub</groupId>
    <artifactId>transcripts-{framework}</artifactId>
    <version>0.1</version>
</dependency>
```

## Example Output
![output](https://img.derock.dev/5f5q0a.png)

## Usage
### Example usage using the built-in message fetcher.
This is probably not true anymore, the syntax should be fairly similar tho - check [this class](https://github.com/dungeon-hub/dungeon-hub-application/blob/2f6e16e3dbc47b1d94b9b7d3204c5afd04b1456a/src/main/kotlin/me/taubsie/dungeonhub/application/commands/TestTranscriptCommand.kt) for an example usage for Kord, the other languages should be able to be used similarily.
```java
DiscordHtmlTranscripts transcript = DiscordHtmlTranscripts.getInstance();
textChannel.sendFiles(transcript.createTranscript(textChannel)).queue()
```

### Or if you prefer, you can pass in your own messages.
```java
DiscordHtmlTranscripts transcript = DiscordHtmlTranscripts.getInstance();
transcript.generateFromMessages(messages); // return to InputStream
```

### You can also put the transcript into a variable
```java
DiscordHtmlTranscripts transcripts = new DiscordHtmlTranscripts();
try {
   testChannel.sendFiles(transcripts.createTranscript(testChannel, "test.html")).queue();
} catch (IOException e) {
   throw new RuntimeException(e);
}
```

### Streaming into a file
`writeTranscript` writes the document directly into any `Appendable` (e.g. a `Writer`) instead of building a `String` first.
```kotlin
File("transcript.html").bufferedWriter().use { writer ->
    DiscordHtmlTranscripts.writeTranscript(channel, channel.messages, writer)
}
```

### Rendering single components
The transcript is built from [kotlinx.html](https://github.com/Kotlin/kotlinx.html) components in `net.dungeonhub.transcripts.components`
(`message`, `embed`, `attachment`, `preamble`, ...), which can also be used to build custom pages:
```kotlin
val html = createHTML().div("chatlog") {
    messages.forEach { message(it) }
}
```
All user supplied text is escaped. The old jsoup based `transcriptify()` methods are deprecated and only kept for compatibility.

## Sample transcript
`./gradlew :transcripts-core:generateSampleTranscript` renders a transcript with every kind of content and many edge cases
(see `SampleTranscript.kt` in the core tests) to `transcripts-core/build/sample-transcripts/sample-transcript.html`.
CI renders it for every pull request and uploads it as the `sample-transcript` artifact.
