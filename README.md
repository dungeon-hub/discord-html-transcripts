# Discord (JDA) HTML Transcripts

Discord HTML Transcripts is a node.js module (recode on JDA) (again recoded on Javacord) to generate nice looking HTML transcripts. Processes discord markdown like **bold**, *italics*, ~~strikethroughs~~, and more. Nicely formats attachments and embeds. Built in XSS protection, preventing users from inserting html tags. 

**This module is designed to work with [Javacord](https://github.com/Javacord/Javacord).**

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


