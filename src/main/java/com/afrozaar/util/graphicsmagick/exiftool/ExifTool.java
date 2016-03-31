package com.afrozaar.util.graphicsmagick.exiftool;

import com.afrozaar.util.graphicsmagick.exiftool.AbstractJsonResponseConsumer.Builder;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.im4java.core.ETOperation;
import org.im4java.core.ETOps;
import org.im4java.core.ExiftoolCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.ImageCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ExifTool {

    private static final Logger LOG = LoggerFactory.getLogger(ExifTool.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode getTags(final String... fileLocations) throws ExiftoolException {
        final ETOps exifOp = new ETOperation().json().groupHeadings("");

        final List<String> usableLocations = Arrays.stream(fileLocations)
                .filter(location -> !location.isEmpty()).collect(Collectors.toList());

        LOG.debug("Retrieving tags for {} files", usableLocations.size());
        usableLocations.forEach(exifOp::addImage);

        Builder consumerBuilder = Builder.using(objectMapper);

        final JsonResponseConsumer outputConsumer = consumerBuilder.newConsumer();
        final JsonErrorResponseConsumer errorConsumer = consumerBuilder.newErrorConsumer();

        try {
            final ImageCommand command = new ExiftoolCmd();
            command.setOutputConsumer(outputConsumer);
            command.setErrorConsumer(errorConsumer);
            LOG.trace("Running exif ops: {}", exifOp);
            command.run(exifOp);

            final Optional<JsonNode> jsonNode = outputConsumer.getNode();
            if (jsonNode.isPresent()) {
                return jsonNode.get();
            } else {
                LOG.warn("No output after successful processing of exifOp '{}'", exifOp);
            }
        } catch (IOException | InterruptedException | IM4JavaException e) {
            throw new ExiftoolException(e);
        }

        final Optional<JsonNode> errorNode = errorConsumer.getNode();
        if (errorNode.isPresent()) {
            return errorNode.get();
        } else {
            throw new RuntimeException("Unexpected state. No output or error node available.");
        }
    }

    public Set<String> getProfiles(ObjectNode node) {
        final Set<String> profiles = Sets.newHashSet(node.fieldNames());
        profiles.removeAll(Arrays.asList("SourceFile", "ExifTool"));
        return ImmutableSet.copyOf(profiles);
    }

    public Set<String> getSupportedProfiles() {
        return Arrays.stream(KnownProfile.values())
                .map(KnownProfile::name)
                .collect(Collectors.toSet());
    }

    public Map<String, Object> getEntriesForProfile(ObjectNode node, KnownProfile profile) {
        final JsonNode profileNode = node.path(profile.name());
        final ImmutableMap.Builder<String, Object> map = ImmutableMap.builder();
        profileNode.fields().forEachRemaining(entry -> map.put(entry.getKey(), entry.getValue()));
        return map.build();
    }
}
