package com.afrozaar.util.graphicsmagick;

import com.afrozaar.util.Regex;
import com.afrozaar.util.graphicsmagick.api.IImageService;

import com.google.common.io.ByteSource;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Random;

import static java.lang.String.format;

public abstract class AbstractImageIO implements IImageService {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    private String tempDir;
    private String tempDirProperty;
    private boolean tempDirSet;

    public AbstractImageIO() {
        super();
    }

    public AbstractImageIO(String tempDir) {
        super();
        this.tempDir = tempDir;
        createTempDir(tempDir);
    }

    @Value("${tempDir}")
    public void setTempDir(String tempDir) {
        this.tempDirProperty = tempDir;
        this.tempDirSet = true;
    }

    @PostConstruct
    public void setTempDir() {
        if (tempDirSet && tempDirProperty != null && !tempDirProperty.equals("${tempDir}")) {
            this.tempDir = tempDirProperty;
        } else {
            this.tempDir = format("%s/media", System.getProperty("java.io.tmpdir"));
        }
        createTempDir(tempDir);
    }

    public void createTempDir(String tempDir) {
        final File dir = new File(tempDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException(format("AbstractImageIO could not prepare the tempDir (Directory did not exist, and could not be created). tempDir=%s",
                                              this.tempDir));
        }
    }

    @Override
    public String downloadResource(final String archiveUrl, ByteSource simpleResource) throws IOException, URISyntaxException {
        return saveImageToTemp(simpleResource, archiveUrl);
    }

    protected String getTempFileName(String sourceName) throws URISyntaxException {
        String suffix = getExtensionFromUrl(sourceName).map(String::toLowerCase).orElse("");
        return format("%s%sresource_%s.%s", tempDir, File.separator, getRandomAlpha(5), suffix);
    }

    protected Optional<String> getExtensionFromFile(String imageName) {
        return Regex.extractMatch(".*?\\.([.\\w]*).*", imageName).filter(x -> x.trim().length() != 0);
    }

    protected Optional<String> getExtensionFromUrl(String imageName) throws URISyntaxException {
        URI uri = new URI(imageName);
        String path = uri.getPath();
        return getExtensionFromFile(path);
    }

    static char[] alphabet = new char[26];

    {
        for (int i = 0; i < 26; i++) {
            alphabet[i] = (char) ('A' + i);
        }
    }

    Random random = new Random();

    protected String getRandomAlpha(int i) {
        char[] letters = new char[i];
        for (int k = 0; k < i; k++) {
            letters[k] = alphabet[random.nextInt(26)];
        }
        return new String(letters);
    }

    @Scheduled(cron = "1 0 * * * *")
    public void cleanupTempDir() {
        try {
            FileUtils.cleanDirectory(new File(tempDir));
        } catch (IOException e) {
            LOG.error("error cleaning directory {}", tempDir);
        }
    }

}
