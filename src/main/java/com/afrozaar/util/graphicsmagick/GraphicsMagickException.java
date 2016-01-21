package com.afrozaar.util.graphicsmagick;

import java.io.IOException;

public class GraphicsMagickException extends IOException {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GraphicsMagickException.class);
    private String temporaryFileLocation;

    public GraphicsMagickException(String temporaryFileLocation, String message, Exception exception) {
        super(message, exception);
        this.temporaryFileLocation = temporaryFileLocation;
    }

    public String getTemporaryFileLocation() {
        return temporaryFileLocation;
    }
}
