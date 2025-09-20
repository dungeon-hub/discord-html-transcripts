package me.ryzeon.transcripts.utils.format.impl;

import me.ryzeon.transcripts.utils.format.IFormatHelper;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ImageFormat implements IFormatHelper {

    final List<String> formats = Arrays.asList("png", "jpg", "jpeg", "gif");
    @Override
    public List<String> formats() {
        return formats;
    }
}
