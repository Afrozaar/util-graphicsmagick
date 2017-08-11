package com.afrozaar.util.graphicsmagick.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class GMInfo {

    private GMInfo() {

    }

    public static String getEnvironmentInfo() throws IOException {
        try {
            final Process exec = Runtime.getRuntime().exec("gm convert -list resource");

            if (exec.waitFor(100, TimeUnit.MILLISECONDS)) {
                return IOUtils.toString(exec.getInputStream(), StandardCharsets.UTF_8);
            } else {
                return IOUtils.toString(exec.getErrorStream(), StandardCharsets.UTF_8);
            }
        } catch (InterruptedException e) {
            return null;
        }
    }
}
