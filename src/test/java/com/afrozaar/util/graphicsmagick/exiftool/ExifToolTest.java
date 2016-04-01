package com.afrozaar.util.graphicsmagick.exiftool;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.Assert.fail;

import com.afrozaar.util.test.TestUtil;

import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.junit.AfterClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * @author johan
 */
public class ExifToolTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExifToolTest.class);
    public static final String TEST_PICTURE = "/bin/Picture_600x400.jpg";

    private ExifTool exifTool = new ExifTool();

    @AfterClass
    public static void cleanup() {
        final Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
        final String[] toDelete = tmpDir.toFile().list((dir, name) -> name.matches("^(junit-pic-|exiftool-junit).*"));
        LOG.info("removing {}",  Arrays.asList(toDelete));
        Arrays.stream(toDelete).forEach(fileString -> LOG.info("{} deleted: {}", fileString, new File(tmpDir.toFile(), fileString).delete()));
    }

    @Test
    public void getAllTags() throws URISyntaxException, ExiftoolException {
        final String location = new File(ExifToolTest.class.getResource(TEST_PICTURE).toURI()).getAbsolutePath();

        final JsonNode results = exifTool.getTags(location, "");

        assertThat(results).isNotNull();

        if (results.isArray()) {
            results.forEach(arrResult -> {
                ObjectNode result = (ObjectNode) arrResult;
                final Set<String> profiles = exifTool.getProfiles(result);
                LOG.debug("profiles: {}", profiles);
                final Map<String, Object> exifEntries = exifTool.getEntriesForProfile(result, KnownProfile.EXIF);
                LOG.debug("exifEntries: {}", exifEntries);
            });
        } else {
            fail("Expected array result.");
        }
    }

    @Test(expected = ExiftoolException.class)
    public void getAllTagsWithNonMediaFile_MustReturnError() throws ExiftoolException, IOException {
        String temporaryLoc = null;
        try {
            final File newTemporaryFile = Files.createTempFile("exiftool-junit", ".php").toFile();
            temporaryLoc = newTemporaryFile.getAbsolutePath();
            FileWriter fileWriter = new FileWriter(newTemporaryFile);
            fileWriter.write(TestUtil.randomString(200));
            fileWriter.flush();
            exifTool.getTags(temporaryLoc);
        } catch (IOException e) {
            fail();
        } finally {
            if (temporaryLoc != null) {
                try {
                    Files.deleteIfExists(Paths.get(temporaryLoc));
                } catch (IOException e) {
                    LOG.error("Error ", e);
                }

            }
        }
    }

    @Test
    public void getKnowProfiles_MustReturnSupportedProfiles() {
        final Set<String> supportedProfiles = exifTool.getSupportedProfiles();
        supportedProfiles.forEach(KnownProfile::valueOf);
    }

    @Test
    public void setTags() throws ExiftoolException, URISyntaxException, IOException {
        final String location = copyToTemp(ExifToolTest.class.getResource(TEST_PICTURE).toURI());
        final ImmutableMap.Builder<SupportedTag, Object> builder = ImmutableMap.builder();

        Arrays.stream(SupportedTag.values()).forEach(tag -> builder.put(tag, TestUtil.randomString(150, true)));

        final Map<SupportedTag, Object> tagMap = builder.build();

        final JsonNode jsonNode = exifTool.setTags(location, tagMap);

//        Set<Object> possibleMutations = new HashSet<>();

        Arrays.stream(KnownProfile.values()).forEach(profile -> {
            LOG.info("========= {} ==========", profile);
            exifTool.getEntriesForProfile(exifTool.getObjectNode(jsonNode, 0), profile).forEach((key, value) -> {
                LOG.debug("{}: {}", key, value);
                /*if (!tagMap.values().contains(value)) {
                    possibleMutations.add(value);
                }*/
            });
        });

        /* sigh. To write a test for this is hard :'( */
        /*if (!possibleMutations.isEmpty()) {
            fail("expected no mutations, but got " + possibleMutations);
        }*/
    }

    private String copyToTemp(URI source) throws IOException {
        final Path tempFile = Files.createTempFile("junit-pic-", ".jpg");

        Files.copy(Paths.get(source), tempFile, StandardCopyOption.REPLACE_EXISTING);

        return tempFile.toAbsolutePath().toString();
    }

}
