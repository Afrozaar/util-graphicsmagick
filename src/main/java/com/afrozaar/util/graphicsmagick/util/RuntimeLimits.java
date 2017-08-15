package com.afrozaar.util.graphicsmagick.util;

public class RuntimeLimits {

    public static final String GM_APPLYLIMITS = "gm.limitresources";

    private RuntimeLimits() {

    }

    public static boolean applyLimits() {
        return Boolean.getBoolean(GM_APPLYLIMITS);
    }
}
