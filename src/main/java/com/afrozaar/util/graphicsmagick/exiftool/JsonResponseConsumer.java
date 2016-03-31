package com.afrozaar.util.graphicsmagick.exiftool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.BiFunction;

public class JsonResponseConsumer extends AbstractJsonResponseConsumer {

    public JsonResponseConsumer(ObjectMapper objectMapper) {
        super(objectMapper);
    }

    @Override
    public void consumeOutput(InputStream inputStream) throws IOException {
        parseInput.apply(inputStream, objectMapper).ifPresent(result -> this.node = result);
    }

    public Optional<JsonNode> getNode() {
        return Optional.ofNullable(node);
    }

    private BiFunction<InputStream, ObjectMapper, Optional<JsonNode>> parseInput = (inputStream, objectMapper) -> {
        try {
            return Optional.of(objectMapper.readTree(inputStream));
        } catch (IOException e) {
            LOG.error("Error ", e);
            return Optional.empty();
        }
    };
}