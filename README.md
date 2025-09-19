# Discord HTML Transcripts

Discord HTML Transcripts is a Kotlin module to generate nice looking HTML transcripts for JDA, Javacord and Kord (Kotlin-based Discord Framework). Processes discord markdown like **bold**, *italics*, ~~strikethroughs~~, and more. Nicely formats attachments and embeds. Built in XSS protection, preventing users from inserting html tags. 

**This module is designed to work with [Kord](https://github.com/kordlib/kord), [Javacord](https://github.com/Javacord/Javacord) and [JDA](https://github.com/discord-jda/JDA).**

HTML Template stolen from [DiscordChatExporter](https://github.com/Tyrrrz/DiscordChatExporter).

## Usage
This is probably not true anymore, the syntax should be fairly similar tho - check [this class](https://github.com/dungeon-hub/dungeon-hub-application/blob/2f6e16e3dbc47b1d94b9b7d3204c5afd04b1456a/src/main/kotlin/me/taubsie/dungeonhub/application/commands/TestTranscriptCommand.kt) for an example usage for Kord, the other languages should be able to be used similarily.
### Example usage using the built in message fetcher.
```java
DiscordHtmlTranscripts transcript = DiscordHtmlTranscripts.getInstance();

transcript.createTranscript(textChannel);
```

### Or if you prefer, you can pass in your own messages.
```java
DiscordHtmlTranscripts transcript = DiscordHtmlTranscripts.getInstance();

transcript.generateFromMessages(messages); // return to InputStream
```

