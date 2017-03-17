package com.afrozaar.util.graphicsmagick;

import java.io.IOException;

public interface IMimeService {

    /**
     * Simple MIME inspector that interrogates the specified file for its MIME type.
     */
    String getMimeType(String uri) throws IOException;
}
