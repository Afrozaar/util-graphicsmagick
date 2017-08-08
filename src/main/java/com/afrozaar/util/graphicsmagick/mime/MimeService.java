package com.afrozaar.util.graphicsmagick.mime;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.afrozaar.util.graphicsmagick.api.IMimeService;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class MimeService implements IMimeService {

    private static final Map<String, String> MIME_TYPE_MAP = ImmutableMap.<String, String>builder()
            .put("ps", "application/postscript")
            .put("pdf", "application/pdf")
            .put("png", "image/png")
            .put("jpg", "image/jpeg")
            .put("jpeg", "image/jpeg")
            .put("gif", "image/gif")
            .put("tiff", "image/tiff")
            .build();

    /**
     * Function producer that takes a resourceUri to fall back to when a type can not be resolved from {@link #MIME_TYPE_MAP}.
     * The produced function takes a type:String input to retrieve from the {@link #MIME_TYPE_MAP}.
     */
    private final Function<String, Function<String, String>> MIME_TYPE_RESOLVER = resourceUri ->
            type ->
                    ofNullable(MIME_TYPE_MAP.get(type.toLowerCase()))
                            .orElseGet(() -> {
                                try {
                                    return getMimeType(resourceUri);
                                } catch (IOException e) {
                                    return format("image/%s", type.toLowerCase());
                                }
                            });

    @Override
    public Function<String, String> resolveFor(String resourceUri) {
        return MIME_TYPE_RESOLVER.apply(resourceUri);
    }

    /**
     * {@inheritDoc}
     *
     * This Implementation makes use of the Unix Command 'file' to determine the MIME type.
     */
    @Override
    public String getMimeType(final String uri) throws IOException {

        // $ file -i gm.tar.bz2
        final String[] fileCmd = { "file", "-i", uri };

        try {
            final Process process = Runtime.getRuntime().exec(fileCmd);

            if (process.waitFor(3, TimeUnit.SECONDS)) {
                //        gm.tar.bz2: application/x-bzip2; charset=binary
                String output = new String(new ByteSource() {
                    @Override
                    public InputStream openStream() throws IOException {
                        return process.getInputStream();
                    }
                }.read(), StandardCharsets.UTF_8);
                return output.split(":")[1].split(";")[0].trim();
            } else {

                String error = new String(new ByteSource() {
                    @Override
                    public InputStream openStream() throws IOException {
                        return process.getErrorStream();
                    }
                }.read(), StandardCharsets.UTF_8);

                throw new IOException(format("Failed to determine MIME Type from %s (errout: %s)", uri, error));
            }
        } catch (IOException | InterruptedException e) {
            throw new IOException(format("Error inspecting MIME type using command %s", Arrays.asList(fileCmd)), e);
        }
    }

    // TODO: Use a bi-map for mime-extension-map and mime-type-map
    private static final Map<String, String> MIME_EXTENSION_MAP = ImmutableMap.of(
            "image/jpg", "jpg",
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/tiff", "tiff",
            "image/gif", "gif"
    );

    @Override
    public String suffixFromMimeType(String mimeType) {
        return MIME_EXTENSION_MAP.get(mimeType);
    }

    @Override
    public String supportedMimeType(String mimeType) {

        if (MIME_EXTENSION_MAP.get(mimeType) == null) {
            return "image/png";
        } else {
            return mimeType;
        }
    }
}
