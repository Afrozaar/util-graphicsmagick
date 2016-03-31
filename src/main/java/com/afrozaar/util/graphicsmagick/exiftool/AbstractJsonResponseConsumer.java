package com.afrozaar.util.graphicsmagick.exiftool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.im4java.process.ErrorConsumer;
import org.im4java.process.OutputConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author johan
 */
public abstract class AbstractJsonResponseConsumer implements OutputConsumer, ErrorConsumer {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    protected JsonNode node;
    protected final ObjectMapper objectMapper;

    protected AbstractJsonResponseConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void consumeError(InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("Method not overridden.");
    }

    @Override
    public void consumeOutput(InputStream inputStream) throws IOException {
        throw new UnsupportedOperationException("Method not overridden.");
    }

    static class Builder {
        private final ObjectMapper mapper;

        private Builder(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        public static Builder using(ObjectMapper mapper) {
            return new Builder(mapper);
        }

        JsonErrorResponseConsumer newErrorConsumer() {
            return new JsonErrorResponseConsumer(mapper);
        }

        JsonResponseConsumer newConsumer() {
            return new JsonResponseConsumer(mapper);
        }
    }
}
