package com.afrozaar.util.graphicsmagick.operation;

import org.apache.commons.io.IOUtils;
import org.im4java.process.OutputConsumer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class OutputResult implements OutputConsumer {
        private String output;

        public String getOutput() {
            return output;
        }

        @Override
        public void consumeOutput(InputStream inputStream) throws IOException {
            output = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
        }
    }