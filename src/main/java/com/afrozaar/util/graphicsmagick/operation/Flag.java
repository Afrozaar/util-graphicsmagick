package com.afrozaar.util.graphicsmagick.operation;

public enum Flag {

    NO_STRIP, AUTO_CONVERT;

    public long bit() {
        return 1 << ordinal();
    }
}
