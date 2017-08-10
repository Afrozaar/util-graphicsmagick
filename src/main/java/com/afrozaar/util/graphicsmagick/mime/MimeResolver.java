package com.afrozaar.util.graphicsmagick.mime;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class MimeResolver {
    private static List<Map.Entry<String, String>> MIME_MAP = Arrays.asList(
            new AbstractMap.SimpleEntry<>("application/postscript", "ps"),
            new AbstractMap.SimpleEntry<>("application/pdf", "pdf"),
            new AbstractMap.SimpleEntry<>("image/png", "png"),
            new AbstractMap.SimpleEntry<>("image/jpeg", "jpg"),
            new AbstractMap.SimpleEntry<>("image/jpeg", "jpeg"),
            new AbstractMap.SimpleEntry<>("image/gif", "gif"),
            new AbstractMap.SimpleEntry<>("image/tiff", "tiff")
    );

    static String forMime(String type) {
        return MIME_MAP.stream()
                .filter(entry -> type.equalsIgnoreCase(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    static String forExtension(String ext) {
        return MIME_MAP.stream()
                .filter(entry -> ext.equalsIgnoreCase(entry.getValue()))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}