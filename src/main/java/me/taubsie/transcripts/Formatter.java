package me.taubsie.transcripts;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by Ryzeon
 * Project: discord-html-transcripts
 * Date: 2/12/21 @ 00:32
 * Twitter: @Ryzeon_ 😎
 * Github: github.ryzeon.me
 */
@UtilityClass
public class Formatter {

    private final Pattern STRONG = Pattern.compile("\\*\\*(.+?)\\*\\*");
    private final Pattern EM = Pattern.compile("\\*(.+?)\\*");
    private final Pattern S = Pattern.compile("~~(.+?)~~");
    private final Pattern U = Pattern.compile("__(.+?)__");
    private final Pattern CODE = Pattern.compile("```[\\S\\s]*?```");
    private final Pattern CODE_1 = Pattern.compile("`[^`]++`");
    // conver this /(?:\r\n|\r|\n)/g to patter in java
    private final Pattern NEW_LINE = Pattern.compile("\\n");

    public String formatBytes(long bytes) {
        int unit = 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public String format(String originalText) {
        Matcher matcher = STRONG.matcher(originalText);
        String newText = originalText;
        while(matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<strong>" + group.replace("**", "") + "</strong>");
        }
        matcher = EM.matcher(newText);
        while(matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<em>" + group.replace("*", "") + "</em>");
        }
        matcher = S.matcher(newText);
        while(matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<s>" + group.replace("~~", "") + "</s>");
        }
        matcher = U.matcher(newText);
        while(matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<u>" + group.replace("__", "") + "</u>");
        }
        matcher = CODE.matcher(newText);
        boolean findCode = false;
        while(matcher.find()) {
            String group = matcher.group();
            newText = newText.replace(group,
                    "<div class=\"pre pre--multiline nohighlight\">"
                            + formatCodeBlock(group) + "</div>");
            findCode = true;
        }
        if (!findCode) {
            matcher = CODE_1.matcher(newText);
            while(matcher.find()) {
                String group = matcher.group();
                newText = newText.replace(group,
                        "<span class=\"pre pre--inline\">" + group.replace("`", "") + "</span>");
            }
        }
        matcher = NEW_LINE.matcher(newText);
        while(matcher.find()) {
            newText = newText.replace(matcher.group(), "<br />");
        }
        return newText;
    }

    public String formatCodeBlock(String group) {
        String result = group.replace("```", "");

        AtomicBoolean empty = new AtomicBoolean(true);
        result = Arrays.stream(result.split("\n"))
                .sequential().filter(s -> {
                    if(empty.get()) {
                        if(s.isBlank()) {
                            return false;
                        } else {
                            empty.set(false);
                            return true;
                        }
                    } else {
                        return true;
                    }
                }).collect(Collectors.joining("\n"));

        return result;
    }

    public String toHex(Color color) {
        StringBuilder hex = new StringBuilder(Integer.toHexString(color.getRGB() & 0xffffff));
        while(hex.length() < 6) {
            hex.insert(0, "0");
        }
        return hex.toString();
    }
}
