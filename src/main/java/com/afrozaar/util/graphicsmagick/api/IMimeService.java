package com.afrozaar.util.graphicsmagick.api;

import java.io.IOException;
import java.util.function.Function;

public interface IMimeService {

    /**
     * Simple MIME inspector that interrogates the specified file for its MIME type.
     */
    String getMimeType(String uri) throws IOException;

    Function<String, String> resolveFor(String resourceUri);

    String suffixFromMimeType(String mimeType);

    String supportedMimeType(String mimeType);
}
