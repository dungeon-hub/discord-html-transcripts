package me.taubsie.transcripts;

import lombok.Getter;
import org.javacord.api.entity.Attachment;
import org.javacord.api.entity.DiscordEntity;
import org.javacord.api.entity.Icon;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.message.embed.Embed;
import org.javacord.api.entity.message.embed.EmbedField;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by Ryzeon
 * Project: discord-html-transcripts
 * Date: 2/12/21 @ 00:32
 * Twitter: @Ryzeon_ 😎
 * Github: github.ryzeon.me
 */
public class DiscordHtmlTranscripts {
    @Getter
    private static final DiscordHtmlTranscripts instance = new DiscordHtmlTranscripts();
    private final List<String> videoFormats = Arrays.asList("mp4", "webm", "mkv", "avi", "mov", "flv", "wmv", "mpg",
            "mpeg");
    private final List<String> audioFormats = Arrays.asList("mp3", "wav", "ogg", "flac");

    public String createTranscript(ServerTextChannel channel) throws IOException {
        return generateFromMessages(channel, channel.getMessagesAsStream());
    }

    public String generateFromMessages(ServerTextChannel channel, Stream<Message> messages) throws IOException {
        InputStream htmlTemplate = getClass().getClassLoader().getResourceAsStream("template.html");

        Document document = Jsoup.parse(htmlTemplate, "UTF-8", "");
        document.outputSettings().indentAmount(0).prettyPrint(true);
        document.getElementsByClass("preamble__guild-icon").first()
                .attr("src", String.valueOf(channel.getServer().getIcon().map(Icon::getUrl).orElse(null)));

        document.getElementById("transcriptTitle").text(channel.getName()); // set title
        document.getElementById("guildname").text(channel.getServer().getName()); // set guild name
        document.getElementById("ticketname").text(channel.getName()); // set channel name

        Element chatLog = document.getElementById("chatlog"); // chat log


        for(Message message : messages.sorted(Comparator.comparing(DiscordEntity::getCreationTimestamp)).toList()) {
            // create message group
            Element messageGroup = document.createElement("div");
            messageGroup.addClass("chatlog__message-group");

            // message reference
            if (message.getReferencedMessage().isPresent()) { // preguntar si es eso
                // message.reference?.messageId
                // create symbol
                Element referenceSymbol = document.createElement("div");
                referenceSymbol.addClass("chatlog__reference-symbol");

                // create reference
                Element reference = document.createElement("div");
                reference.addClass("chatlog__reference");

                var referenceMessage = message.getReferencedMessage().get();
                MessageAuthor author = referenceMessage.getAuthor();
                var color = Formatter.toHex(Objects.requireNonNull(author.getRoleColor().orElse(Color.WHITE)));

                reference.html("<img class=\"chatlog__reference-avatar\" src=\""
                        + author.getAvatar().getUrl() + "\" alt=\"Avatar\" loading=\"lazy\">" +
                        "<span class=\"chatlog__reference-name\" title=\"" + author.getName()
                        + "\" style=\"color: " + color + "\">" + author.getName() + "\"</span>" +
                        "<div class=\"chatlog__reference-content\">" +
                        " <span class=\"chatlog__reference-link\" onclick=\"scrollToMessage(event, '"
                        + referenceMessage.getId() + "')\">" +
                        "<em>" +
                        (!referenceMessage.getReadableContent().isBlank()
                                ? referenceMessage.getReadableContent().length() > 42
                                ? referenceMessage.getReadableContent().substring(0, 42)
                                + "..."
                                : referenceMessage.getReadableContent()
                                : "Click to see attachment") +
                        "</em>" +
                        "</span>" +
                        "</div>");

                messageGroup.appendChild(referenceSymbol);
                messageGroup.appendChild(reference);
            }

            var author = message.getAuthor();

            Element authorElement = document.createElement("div");
            authorElement.addClass("chatlog__author-avatar-container");

            Element authorAvatar = document.createElement("img");
            authorAvatar.addClass("chatlog__author-avatar");
            authorAvatar.attr("src", String.valueOf(author.getAvatar().getUrl()));
            authorAvatar.attr("alt", "Avatar");
            authorAvatar.attr("loading", "lazy");

            authorElement.appendChild(authorAvatar);
            messageGroup.appendChild(authorElement);

            // message content
            Element content = document.createElement("div");
            content.addClass("chatlog__messages");
            // message author name
            Element authorName = document.createElement("span");
            authorName.addClass("chatlog__author-name");
            // authorName.attr("title", author.getName()); // author.name
            authorName.attr("title", author.getDisplayName());
            authorName.text(author.getName());
            authorName.attr("data-user-id", String.valueOf(author.getId()));
            content.appendChild(authorName);

            if (author.isBotUser()) {
                Element botTag = document.createElement("span");
                botTag.addClass("chatlog__bot-tag").text("BOT");
                content.appendChild(botTag);
            }

            // timestamp
            Element timestamp = document.createElement("span");
            timestamp.addClass("chatlog__timestamp");

            timestamp.text(DateTimeFormatter
                    .ofPattern("HH:mm:ss")
                    .withZone(ZoneId.systemDefault())
                    .format(message.getCreationTimestamp()));

            content.appendChild(timestamp);

            Element messageContent = document.createElement("div");
            messageContent.addClass("chatlog__message");
            messageContent.attr("data-message-id", String.valueOf(message.getId()));
            messageContent.attr("id", "message-" + message.getId());
            messageContent.attr("title", "Message sent: "
                    + DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault()).format(message.getCreationTimestamp()));

            if (!message.getReadableContent().isBlank()) {
                Element messageContentContent = document.createElement("div");
                messageContentContent.addClass("chatlog__content");

                Element messageContentContentMarkdown = document.createElement("div");
                messageContentContentMarkdown.addClass("markdown");

                Element messageContentContentMarkdownSpan = document.createElement("span");
                messageContentContentMarkdownSpan.addClass("preserve-whitespace");
//                System.out.println(message.getContentDisplay());
//                System.out.println(message.getContentDisplay().length());
//                System.out.println(message.getContentStripped());
//                System.out.println(message.getContentRaw());
//                System.out.println(message.getContentDisplay().contains("\n"));
//                System.out.println(message.getContentDisplay().contains("\r"));
//                System.out.println(message.getContentDisplay().contains("\r\n"));
                document.outputSettings().prettyPrint(false);
                messageContentContentMarkdownSpan
                        .html(Formatter.format(message.getReadableContent()));

                messageContentContentMarkdown.appendChild(messageContentContentMarkdownSpan);
                messageContentContent.appendChild(messageContentContentMarkdown);
                messageContent.appendChild(messageContentContent);
            }

            // messsage attachments
            if (!message.getAttachments().isEmpty()) {
                for(Attachment attachment : message.getAttachments()) {
                    Element attachmentsDiv = document.createElement("div");
                    attachmentsDiv.addClass("chatlog__attachment");

                    Optional<String> attachmentType = Optional.of(attachment.getFileName())
                            .filter(s -> s.contains("."))
                            .map(s -> s.substring(s.lastIndexOf(".") + 1));
                    if (attachment.isImage()) {
                        Element attachmentLink = document.createElement("a");

                        Element attachmentImage = document.createElement("img");
                        attachmentImage.addClass("chatlog__attachment-media");
                        attachmentImage.attr("src", String.valueOf(attachment.getUrl()));
                        attachmentImage.attr("alt", "Image attachment");
                        attachmentImage.attr("loading", "lazy");
                        attachmentImage.attr("title",
                                "Image: " + attachment.getFileName() + Formatter.formatBytes(attachment.getSize()));

                        attachmentLink.appendChild(attachmentImage);
                        attachmentsDiv.appendChild(attachmentLink);
                    } else if (attachmentType.isPresent() && videoFormats.contains(attachmentType.get())) {
                        Element attachmentVideo = document.createElement("video");
                        attachmentVideo.addClass("chatlog__attachment-media");
                        attachmentVideo.attr("src", String.valueOf(attachment.getUrl()));
                        attachmentVideo.attr("alt", "Video attachment");
                        attachmentVideo.attr("controls", true);
                        attachmentVideo.attr("title",
                                "Video: " + attachment.getFileName() + Formatter.formatBytes(attachment.getSize()));

                        attachmentsDiv.appendChild(attachmentVideo);
                    } else if (attachmentType.isPresent() && audioFormats.contains(attachmentType.get())) {
                        Element attachmentAudio = document.createElement("audio");
                        attachmentAudio.addClass("chatlog__attachment-media");
                        attachmentAudio.attr("src", String.valueOf(attachment.getUrl()));
                        attachmentAudio.attr("alt", "Audio attachment");
                        attachmentAudio.attr("controls", true);
                        attachmentAudio.attr("title",
                                "Audio: " + attachment.getFileName() + Formatter.formatBytes(attachment.getSize()));

                        attachmentsDiv.appendChild(attachmentAudio);
                    } else {
                        Element attachmentGeneric = document.createElement("div");
                        attachmentGeneric.addClass("chatlog__attachment-generic");

                        Element attachmentGenericIcon = document.createElement("svg");
                        attachmentGenericIcon.addClass("chatlog__attachment-generic-icon");

                        Element attachmentGenericIconUse = document.createElement("use");
                        attachmentGenericIconUse.attr("xlink:href", "#icon-attachment");

                        attachmentGenericIcon.appendChild(attachmentGenericIconUse);
                        attachmentGeneric.appendChild(attachmentGenericIcon);

                        Element attachmentGenericName = document.createElement("div");
                        attachmentGenericName.addClass("chatlog__attachment-generic-name");

                        Element attachmentGenericNameLink = document.createElement("a");
                        attachmentGenericNameLink.attr("href", String.valueOf(attachment.getUrl()));
                        attachmentGenericNameLink.text(attachment.getFileName());

                        attachmentGenericName.appendChild(attachmentGenericNameLink);
                        attachmentGeneric.appendChild(attachmentGenericName);

                        Element attachmentGenericSize = document.createElement("div");
                        attachmentGenericSize.addClass("chatlog__attachment-generic-size");

                        attachmentGenericSize.text(Formatter.formatBytes(attachment.getSize()));
                        attachmentGeneric.appendChild(attachmentGenericSize);

                        attachmentsDiv.appendChild(attachmentGeneric);
                    }

                    messageContent.appendChild(attachmentsDiv);
                }
            }

            content.appendChild(messageContent);

            if (!message.getEmbeds().isEmpty()) {
                for(Embed embed : message.getEmbeds()) {
                    if (embed == null) {
                        continue;
                    }
                    Element embedDiv = document.createElement("div");
                    embedDiv.addClass("chatlog__embed");

                    // embed color
                    if (embed.getColor().isPresent()) {
                        Element embedColorPill = document.createElement("div");
                        embedColorPill.addClass("chatlog__embed-color-pill");
                        embedColorPill.attr("style",
                                "background-color: #" + Formatter.toHex(embed.getColor().get()));

                        embedDiv.appendChild(embedColorPill);
                    }

                    Element embedContentContainer = document.createElement("div");
                    embedContentContainer.addClass("chatlog__embed-content-container");

                    Element embedContent = document.createElement("div");
                    embedContent.addClass("chatlog__embed-content");

                    Element embedText = document.createElement("div");
                    embedText.addClass("chatlog__embed-text");

                    // embed author
                    if (embed.getAuthor().isPresent() && embed.getAuthor().get().getName() != null) {
                        Element embedAuthor = document.createElement("div");
                        embedAuthor.addClass("chatlog__embed-author");

                        if (embed.getAuthor().get().getIconUrl().isPresent()) {
                            Element embedAuthorIcon = document.createElement("img");
                            embedAuthorIcon.addClass("chatlog__embed-author-icon");
                            embedAuthorIcon.attr("src", String.valueOf(embed.getAuthor().get().getIconUrl().get()));
                            embedAuthorIcon.attr("alt", "Author icon");
                            embedAuthorIcon.attr("loading", "lazy");

                            embedAuthor.appendChild(embedAuthorIcon);
                        }

                        Element embedAuthorName = document.createElement("span");
                        embedAuthorName.addClass("chatlog__embed-author-name");

                        if (embed.getAuthor().get().getUrl().isPresent()) {
                            Element embedAuthorNameLink = document.createElement("a");
                            embedAuthorNameLink.addClass("chatlog__embed-author-name-link");
                            embedAuthorNameLink.attr("href", String.valueOf(embed.getAuthor().get().getUrl().get()));
                            embedAuthorNameLink.text(embed.getAuthor().get().getName());

                            embedAuthorName.appendChild(embedAuthorNameLink);
                        } else {
                            embedAuthorName.text(embed.getAuthor().get().getName());
                        }

                        embedAuthor.appendChild(embedAuthorName);
                        embedText.appendChild(embedAuthor);
                    }

                    // embed title
                    if (embed.getTitle().isPresent()) {
                        Element embedTitle = document.createElement("div");
                        embedTitle.addClass("chatlog__embed-title");

                        if (embed.getUrl().isPresent()) {
                            Element embedTitleLink = document.createElement("a");
                            embedTitleLink.addClass("chatlog__embed-title-link");
                            embedTitleLink.attr("href", String.valueOf(embed.getUrl().get()));

                            Element embedTitleMarkdown = document.createElement("div");
                            embedTitleMarkdown.addClass("markdown preserve-whitespace")
                                    .html(Formatter.format(embed.getTitle().get()));

                            embedTitleLink.appendChild(embedTitleMarkdown);
                            embedTitle.appendChild(embedTitleLink);
                        } else {
                            Element embedTitleMarkdown = document.createElement("div");
                            embedTitleMarkdown.addClass("markdown preserve-whitespace")
                                    .html(Formatter.format(embed.getTitle().get()));

                            embedTitle.appendChild(embedTitleMarkdown);
                        }
                        embedText.appendChild(embedTitle);
                    }

                    // embed description
                    if (embed.getDescription().isPresent()) {
                        Element embedDescription = document.createElement("div");
                        embedDescription.addClass("chatlog__embed-description");

                        Element embedDescriptionMarkdown = document.createElement("div");
                        embedDescriptionMarkdown.addClass("markdown preserve-whitespace");
                        embedDescriptionMarkdown
                                .html(Formatter.format(embed.getDescription().orElse("")));

                        embedDescription.appendChild(embedDescriptionMarkdown);
                        embedText.appendChild(embedDescription);
                    }

                    // embed fields
                    if (!embed.getFields().isEmpty()) {
                        Element embedFields = document.createElement("div");
                        embedFields.addClass("chatlog__embed-fields");

                        for(EmbedField field : embed.getFields()) {
                            Element embedField = document.createElement("div");
                            embedField.addClass(field.isInline() ? "chatlog__embed-field-inline"
                                    : "chatlog__embed-field");

                            // Field nmae
                            Element embedFieldName = document.createElement("div");
                            embedFieldName.addClass("chatlog__embed-field-name");

                            Element embedFieldNameMarkdown = document.createElement("div");
                            embedFieldNameMarkdown.addClass("markdown preserve-whitespace");
                            embedFieldNameMarkdown.html(field.getName());

                            embedFieldName.appendChild(embedFieldNameMarkdown);
                            embedField.appendChild(embedFieldName);


                            // Field value
                            Element embedFieldValue = document.createElement("div");
                            embedFieldValue.addClass("chatlog__embed-field-value");

                            Element embedFieldValueMarkdown = document.createElement("div");
                            embedFieldValueMarkdown.addClass("markdown preserve-whitespace");
                            embedFieldValueMarkdown
                                    .html(Formatter.format(field.getValue()));

                            embedFieldValue.appendChild(embedFieldValueMarkdown);
                            embedField.appendChild(embedFieldValue);

                            embedFields.appendChild(embedField);
                        }

                        embedText.appendChild(embedFields);
                    }

                    embedContent.appendChild(embedText);

                    // embed thumbnail
                    if (embed.getThumbnail().isPresent()) {
                        Element embedThumbnail = document.createElement("div");
                        embedThumbnail.addClass("chatlog__embed-thumbnail-container");

                        Element embedThumbnailLink = document.createElement("a");
                        embedThumbnailLink.addClass("chatlog__embed-thumbnail-link");
                        embedThumbnailLink.attr("href", String.valueOf(embed.getThumbnail().get().getUrl()));

                        Element embedThumbnailImage = document.createElement("img");
                        embedThumbnailImage.addClass("chatlog__embed-thumbnail");
                        embedThumbnailImage.attr("src", String.valueOf(embed.getThumbnail().get().getUrl()));
                        embedThumbnailImage.attr("alt", "Thumbnail");
                        embedThumbnailImage.attr("loading", "lazy");

                        embedThumbnailLink.appendChild(embedThumbnailImage);
                        embedThumbnail.appendChild(embedThumbnailLink);

                        embedContent.appendChild(embedThumbnail);
                    }

                    embedContentContainer.appendChild(embedContent);

                    // embed image
                    if (embed.getImage().isPresent()) {
                        Element embedImage = document.createElement("div");
                        embedImage.addClass("chatlog__embed-image-container");

                        Element embedImageLink = document.createElement("a");
                        embedImageLink.addClass("chatlog__embed-image-link");
                        embedImageLink.attr("href", String.valueOf(embed.getImage().get().getUrl()));

                        Element embedImageImage = document.createElement("img");
                        embedImageImage.addClass("chatlog__embed-image");
                        embedImageImage.attr("src", String.valueOf(embed.getImage().get().getUrl()));
                        embedImageImage.attr("alt", "Image");
                        embedImageImage.attr("loading", "lazy");

                        embedImageLink.appendChild(embedImageImage);
                        embedImage.appendChild(embedImageLink);

                        embedContentContainer.appendChild(embedImage);
                    }

                    // embed footer
                    if (embed.getFooter().isPresent()) {
                        Element embedFooter = document.createElement("div");
                        embedFooter.addClass("chatlog__embed-footer");

                        if (embed.getFooter().get().getIconUrl().isPresent()) {
                            Element embedFooterIcon = document.createElement("img");
                            embedFooterIcon.addClass("chatlog__embed-footer-icon");
                            embedFooterIcon.attr("src", String.valueOf(embed.getFooter().get().getIconUrl().get()));
                            embedFooterIcon.attr("alt", "Footer icon");
                            embedFooterIcon.attr("loading", "lazy");

                            embedFooter.appendChild(embedFooterIcon);
                        }

                        Element embedFooterText = document.createElement("span");
                        embedFooterText.addClass("chatlog__embed-footer-text");

                        embedFooterText.text(embed.getTimestamp().isPresent()
                                ? embed.getFooter().get().getText().orElse("") + " • "
                                + DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault()).format(embed.getTimestamp().get())
                                : embed.getFooter().get().getText().orElse(""));

                        embedFooter.appendChild(embedFooterText);

                        embedContentContainer.appendChild(embedFooter);
                    }

                    embedDiv.appendChild(embedContentContainer);
                    content.appendChild(embedDiv);
                }
            }

            messageGroup.appendChild(content);
            chatLog.appendChild(messageGroup);
        }
        return document.outerHtml();
    }
}
