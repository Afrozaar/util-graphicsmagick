package com.afrozaar.util.graphicsmagick.operation;

public enum Flag {

    NO_STRIP, AUTO_CONVERT, FLATTEN, NO_FLATTEN;

    public long bit() {
        return 1 << ordinal();
    }
}
