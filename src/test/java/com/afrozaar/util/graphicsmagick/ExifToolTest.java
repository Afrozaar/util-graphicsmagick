package com.afrozaar.util.graphicsmagick;

import static org.assertj.core.api.Assertions.assertThat;

import com.afrozaar.util.graphicsmagick.exiftool.ExifTool;
import com.afrozaar.util.graphicsmagick.exiftool.ExiftoolException;
import com.afrozaar.util.graphicsmagick.exiftool.JsonResponseConsumer;

import com.google.common.io.ByteSource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.im4java.core.ETOperation;
import org.im4java.core.ExiftoolCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.ImageCommand;
import org.im4java.process.OutputConsumer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author johan
 */
public class ExifToolTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExifToolTest.class);

    private ExifTool exifTool = new ExifTool();

    @Test
    public void getAllTags() throws URISyntaxException, ExiftoolException {

        final URL resource = ExifToolTest.class.getResource("/bin/Picture_600x400.jpg");

        final String location = new File(resource.toURI()).getAbsolutePath();

        final JsonNode results = exifTool.getTags(location, location);

        assertThat(results).isNotNull();

        if (results.isArray()) {
            results.forEach(result -> {
                System.out.println("result");
                result.fieldNames().forEachRemaining(body -> {
                    System.out.println("field = " + body);
                });
            });
        }

        final JsonNode exif = results.path("EXIF");

        assertThat(exif).isNotNull();
    }






}
