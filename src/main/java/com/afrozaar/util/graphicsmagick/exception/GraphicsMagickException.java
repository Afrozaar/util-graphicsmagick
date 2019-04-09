package com.afrozaar.util.graphicsmagick.exception;

import java.io.IOException;

public class GraphicsMagickException extends IOException {

    /**
     *
     */
    private static final long serialVersionUID = -4089890120474045526L;
    private String temporaryFileLocation;

    public GraphicsMagickException(String temporaryFileLocation, String message, Exception exception) {
        super(message, exception);
        this.temporaryFileLocation = temporaryFileLocation;
    }

    public String getTemporaryFileLocation() {
        return temporaryFileLocation;
    }
}
