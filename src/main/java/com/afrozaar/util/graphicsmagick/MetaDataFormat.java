package com.afrozaar.util.graphicsmagick;

import java.util.Optional;

public enum MetaDataFormat {
    RAW, PARSED;

    public static MetaDataFormat fromString(String format0) {
        final Optional<String> format = Optional.ofNullable(format0);
        if (format.isPresent()) {
            try {
                return valueOf(format.get().toUpperCase());
            } catch (IllegalArgumentException e) {
                return MetaDataFormat.PARSED;
            }
        } else {
            return MetaDataFormat.PARSED;
        }
    }

}
