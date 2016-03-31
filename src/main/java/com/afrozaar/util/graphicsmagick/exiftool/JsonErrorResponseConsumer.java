package com.afrozaar.util.graphicsmagick.exiftool;

import com.google.common.io.ByteSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.BiFunction;

public class JsonErrorResponseConsumer extends AbstractJsonResponseConsumer {

    public JsonErrorResponseConsumer(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void consumeError(InputStream inputStream) throws IOException {
        parseInput.apply(inputStream, objectMapper).ifPresent(result -> this.node = result);
    }

    public Optional<JsonNode> getNode() {
        return Optional.ofNullable(node);
    }

    private BiFunction<InputStream, ObjectMapper, Optional<JsonNode>> parseInput = (inputStream, objectMapper) -> {
        try {
            final String errString = new String(new ByteSource() {
                @Override
                public InputStream openStream() throws IOException {
                    return inputStream;
                }
            }.read());

            if (errString.startsWith("[")) {
                return Optional.of(objectMapper.readTree(errString));
            } else {
                return Optional.of(objectMapper.createObjectNode().put("error", errString));
            }
        } catch (IOException e) {
            LOG.error("Error ", e);
            return Optional.empty();
        }
    };
}