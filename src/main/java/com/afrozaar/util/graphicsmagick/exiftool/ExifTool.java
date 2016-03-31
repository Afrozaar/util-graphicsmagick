package com.afrozaar.util.graphicsmagick.exiftool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.im4java.core.ETOperation;
import org.im4java.core.ETOps;
import org.im4java.core.ExiftoolCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.ImageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

public class ExifTool {

    private static final Logger LOG = LoggerFactory.getLogger(ExifTool.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode getTags(final String... fileLocations) throws ExiftoolException {
        LOG.debug("Retrieving tags for {} files", fileLocations.length);


        final ETOps exifOp = new ETOperation().json().groupHeadings("");

        Arrays.stream(fileLocations).forEach(exifOp::addImage);

        LOG.trace("Running exif ops: {}", exifOp);

        JsonResponseConsumer outputConsumer = new JsonResponseConsumer(objectMapper);
        JsonErrorResponseConsumer errorConsumer = new JsonErrorResponseConsumer(objectMapper);

        final ImageCommand command = new ExiftoolCmd();
        command.setOutputConsumer(outputConsumer);
        command.setErrorConsumer(errorConsumer);

        try {
            command.run(exifOp);
        } catch (IOException | InterruptedException | IM4JavaException e) {
            LOG.error("Error ", e);
        }

        final Optional<JsonNode> jsonNode = outputConsumer.getNode();
        if (jsonNode.isPresent()) {
            return jsonNode.get();
        } else if (errorConsumer.getNode().isPresent()) {
            return errorConsumer.getNode().get();
        } else {
            throw new RuntimeException("Unexpected state. No output or error node available.");
        }

    }

    public Set<String> getProfiles(JsonNode resultNode) {
        return null;
    }

}
