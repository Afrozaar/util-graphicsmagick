package com.afrozaar.util.graphicsmagick;

import java.util.Optional;

public enum MetaDataFormat {
    RAW, PARSED;

    public static MetaDataFormat fromString(String format0) {
        try {
            return Optional.ofNullable(format0).map(e -> valueOf(e.toUpperCase())).orElse(PARSED);
        } catch (IllegalArgumentException e) {
            return MetaDataFormat.PARSED;
        }
    }
}
