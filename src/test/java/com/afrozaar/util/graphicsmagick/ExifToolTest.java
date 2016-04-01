package com.afrozaar.util.graphicsmagick;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.Assert.fail;

import com.afrozaar.util.graphicsmagick.exiftool.ExifTool;
import com.afrozaar.util.graphicsmagick.exiftool.ExiftoolException;
import com.afrozaar.util.graphicsmagick.exiftool.KnownProfile;
import com.afrozaar.util.graphicsmagick.exiftool.SupportedTag;
import com.afrozaar.util.test.TestUtil;

import com.google.common.collect.ImmutableMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
import java.util.Map;
import java.util.Set;

/**
 * @author johan
 */
public class ExifToolTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExifToolTest.class);

    private ExifTool exifTool = new ExifTool();

    @Test
    public void getAllTags() throws URISyntaxException, ExiftoolException {
        final String location = new File(ExifToolTest.class.getResource("/bin/Picture_600x400.jpg").toURI()).getAbsolutePath();

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
        final URI source = ExifToolTest.class.getResource("/bin/Picture_600x400.jpg").toURI();

        final String location = copyToTemp(source);

        final String copyright = "Baobab";
        final String artist = "some-artist";
        final String sourceS = "hot-off-the-press";

        final ImmutableMap.Builder<SupportedTag, Object> builder = ImmutableMap.<SupportedTag, Object>builder()
                .put(SupportedTag.Creator, artist)
                .put(SupportedTag.Description, copyright)
                .put(SupportedTag.Rights, copyright)
                .put(SupportedTag.Source, sourceS)
                .put(SupportedTag.Credit, sourceS);

        final JsonNode jsonNode = exifTool.setTags(location, builder.build());

        LOG.info("updated jsonNode: {}", jsonNode);

    }

    private String copyToTemp(URI source) throws IOException {
        final Path tempFile = Files.createTempFile("junit-pic-", ".jpg");

        Files.copy(Paths.get(source), tempFile, StandardCopyOption.REPLACE_EXISTING);

        return tempFile.toAbsolutePath().toString();
    }

}
