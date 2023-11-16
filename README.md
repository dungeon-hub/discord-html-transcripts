# Discord (JDA) HTML Transcripts

Discord HTML Transcripts is a node.js module (recode on JDA) (again recoded on Javacord) to generate nice looking HTML transcripts. Processes discord markdown like **bold**, *italics*, ~~strikethroughs~~, and more. Nicely formats attachments and embeds. Built in XSS protection, preventing users from inserting html tags. 

**This module is designed to work with [Javacord](https://github.com/Javacord/Javacord).**

HTML Template stolen from [DiscordChatExporter](https://github.com/Tyrrrz/DiscordChatExporter).

## Usage
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

