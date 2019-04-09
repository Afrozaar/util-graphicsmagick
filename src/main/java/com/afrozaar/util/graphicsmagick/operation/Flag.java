package com.afrozaar.util.graphicsmagick.operation;

public enum Flag {

    NO_STRIP, AUTO_CONVERT, FLATTEN, NO_FLATTEN, WEBP_NOT_SUPPORTED;

    public long bit() {
        return 1 << ordinal();
    }
}
