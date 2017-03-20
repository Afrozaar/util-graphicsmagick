package com.afrozaar.util.graphicsmagick;

import static java.lang.String.format;

import com.google.common.io.ByteSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MimeService implements IMimeService {

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
}
