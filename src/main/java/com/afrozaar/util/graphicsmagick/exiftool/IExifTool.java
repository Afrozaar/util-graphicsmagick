package com.afrozaar.util.graphicsmagick.exiftool;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;
import java.util.Set;

/**
 * @author johan
 */
public interface IExifTool {
    JsonNode getTags(String... fileLocations) throws ExiftoolException;

    Set<String> getProfiles(ObjectNode node);

    Set<String> getSupportedProfiles();

    Map<String, Object> getEntriesForProfile(ObjectNode node, KnownProfile profile);

    JsonNode setTags(String fileLocation, Map<SupportedTag, Object> tagMap) throws ExiftoolException;
}
