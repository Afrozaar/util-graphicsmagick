package com.afrozaar.util.graphicsmagick.mime;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.afrozaar.util.graphicsmagick.api.IMimeService;

import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class MimeService implements IMimeService {

    @Override
    public Function<String, String> resolveFromBaseTypeOrInterrogate(String resourceUri) {
        return type ->
                ofNullable(MimeResolver.forExtension(type))
                        .orElseGet(() -> {
                            try {
                                return getMimeType(resourceUri);
                            } catch (IOException e) {
                                return String.format("image/%s", type.toLowerCase());
                            }
                        });
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

    @Override
    public String suffixFromMimeType(String mimeType) {
        return MimeResolver.forMime(mimeType);
    }

    @Override
    public String supportedMimeType(String mimeType) {
        return ofNullable(MimeResolver.forMime(mimeType))
                .map(x -> mimeType)
                .orElse("image/png");
    }
}
