package com.afrozaar.util.graphicsmagick;

public enum MetaDataFormat {
    RAW, PARSED;

    public static MetaDataFormat fromString(String format) {
        if (format == null) {
            return MetaDataFormat.PARSED;
        } else {
            try {
                return valueOf(format);
            } catch (IllegalArgumentException iae) {
                return MetaDataFormat.PARSED;
            }
        }

    }

}
